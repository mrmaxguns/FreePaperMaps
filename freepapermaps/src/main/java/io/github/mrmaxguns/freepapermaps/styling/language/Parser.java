package io.github.mrmaxguns.freepapermaps.styling.language;

import io.github.mrmaxguns.freepapermaps.UserInputException;

import java.awt.*;
import java.util.ArrayList;


public class Parser {
    private final Lexer lexer;
    private Lexer.Token currentToken;

    public Parser(Lexer lexer) throws UserInputException {
        this.lexer = lexer;
        this.currentToken = lexer.getNextToken();
    }

    public AST parse() throws UserInputException {
        AST result = parseParameter();
        if (currentToken.type != Lexer.TokenType.EOF) {
            throw error("Found unexpected token of type " + currentToken.type.name() + ".");
        }
        return result;
    }


    public Lexer.Token eat(Lexer.TokenType expectedTokenType) throws UserInputException {
        Lexer.Token t = currentToken;
        if (t.type == expectedTokenType) {
            currentToken = lexer.getNextToken();
            return t;
        } else {
            throw error("Expected token of type " + expectedTokenType.name() + ", but got token of type " +
                        currentToken.type.name());
        }
    }

    private AST parseParameter() throws UserInputException {
        if (currentToken.type == Lexer.TokenType.RECIPE_MARKER) {
            eat(Lexer.TokenType.RECIPE_MARKER);
            Lexer.Token recipeName = eat(Lexer.TokenType.STRING_LITERAL);
            return new RecipeNode(((Lexer.StringToken) recipeName).value);
        }

        try {
            return parseExpression();
        } catch (UserInputException e) {
            throw error("Expected recipe or valid expression.");
        }
    }

    private AST parseExpression() throws UserInputException {
        AST node = parsePrimaryExpression();

        while (currentToken.type == Lexer.TokenType.FALLBACK_OPERATOR) {
            eat(Lexer.TokenType.FALLBACK_OPERATOR);
            node = new BinaryOperatorNode(node, Lexer.TokenType.FALLBACK_OPERATOR, parseExpression());
        }

        return node;
    }

    private AST parsePrimaryExpression() throws UserInputException {
        switch (currentToken.type) {
            case HEX_COLOR_LITERAL -> {
                eat(Lexer.TokenType.HEX_COLOR_LITERAL);
                return new ColorNode(((Lexer.ColorToken) currentToken).value);
            }
            case STRING_LITERAL -> {
                eat(Lexer.TokenType.STRING_LITERAL);
                return new StringNode(((Lexer.StringToken) currentToken).value);
            }
            case GLOBAL_MARKER -> {
                return parseGlobal();
            }
            case VALUE_VARIABLE_MARKER -> {
                return parseValueVariable();
            }
            case FUNCTION_IDENTIFIER -> {
                return parseFunctionCall();
            }
            default -> {
                try {
                    return parseNumericExpression();
                } catch (UserInputException e) {
                    throw new UserInputException("Expected valid primary expression.");
                }
            }
        }
    }

    private AST parseGlobal() throws UserInputException {
        eat(Lexer.TokenType.GLOBAL_MARKER);
        eat(Lexer.TokenType.L_BRACKET);
        AST result = new GlobalNode(((Lexer.StringToken) eat(Lexer.TokenType.STRING_LITERAL)).value);
        eat(Lexer.TokenType.R_BRACKET);
        return result;
    }

    private AST parseValueVariable() throws UserInputException {
        eat(Lexer.TokenType.VALUE_VARIABLE_MARKER);
        eat(Lexer.TokenType.L_BRACKET);
        AST result = new ValueVariableNode(parseExpression());
        eat(Lexer.TokenType.R_BRACKET);
        return result;
    }

    private AST parseFunctionCall() throws UserInputException {
        String identifier = ((Lexer.FunctionIdentifierToken) eat(Lexer.TokenType.FUNCTION_IDENTIFIER)).value;
        eat(Lexer.TokenType.L_PAREN);
        FunctionCallNode node = new FunctionCallNode(identifier);
        node.arguments.add(parseExpression());

        while (currentToken.type != Lexer.TokenType.R_PAREN) {
            eat(Lexer.TokenType.COMMA);
            node.arguments.add(parseExpression());
        }

        return node;
    }

    private AST parseNumericExpression() throws UserInputException {
        AST node = parseNumericTerm();

        while (currentToken.type == Lexer.TokenType.PLUS_OPERATOR ||
               currentToken.type == Lexer.TokenType.MINUS_OR_NEG_OPERATOR) {
            if (currentToken.type == Lexer.TokenType.PLUS_OPERATOR) {
                eat(Lexer.TokenType.PLUS_OPERATOR);
                node = new BinaryOperatorNode(node, Lexer.TokenType.PLUS_OPERATOR, parseNumericTerm());
            } else {
                eat(Lexer.TokenType.MINUS_OR_NEG_OPERATOR);
                node = new BinaryOperatorNode(node, Lexer.TokenType.MINUS_OR_NEG_OPERATOR, parseNumericTerm());
            }
        }

        return node;
    }

    private AST parseNumericTerm() throws UserInputException {
        AST node = parseNumericFactor();

        while (currentToken.type == Lexer.TokenType.MUL_OPERATOR || currentToken.type == Lexer.TokenType.DIV_OPERATOR) {
            if (currentToken.type == Lexer.TokenType.MUL_OPERATOR) {
                eat(Lexer.TokenType.MUL_OPERATOR);
                node = new BinaryOperatorNode(node, Lexer.TokenType.MUL_OPERATOR, parseNumericFactor());
            } else {
                eat(Lexer.TokenType.DIV_OPERATOR);
                node = new BinaryOperatorNode(node, Lexer.TokenType.DIV_OPERATOR, parseNumericFactor());
            }
        }

        return node;
    }

    private AST parseNumericFactor() throws UserInputException {
        switch (currentToken.type) {
            case MINUS_OR_NEG_OPERATOR -> {
                eat(Lexer.TokenType.MINUS_OR_NEG_OPERATOR);
                return new UnaryOperatorNode(Lexer.TokenType.MINUS_OR_NEG_OPERATOR, parseNumericFactor());
            }
            case POSITIVE_NUMERIC_LITERAL -> {
                Lexer.PositiveNumericToken token = (Lexer.PositiveNumericToken) eat(
                        Lexer.TokenType.POSITIVE_NUMERIC_LITERAL);
                return new NumberNode(token.value, token.unit);
            }
            case FUNCTION_IDENTIFIER -> {
                return parseFunctionCall();
            }
            default -> {
                try {
                    return parseNumericExpression();
                } catch (UserInputException e) {
                    throw new UserInputException("Expected valid numeric factor.");
                }
            }
        }
    }

    private UserInputException error(String message) {
        return lexer.error(message);
    }

    public static class AST {}


    public static class RecipeNode extends AST {
        public final String recipeName;

        public RecipeNode(String recipeName) { this.recipeName = recipeName; }
    }


    public static class BinaryOperatorNode extends AST {
        public final AST left;
        public final Lexer.TokenType op;
        public final AST right;

        public BinaryOperatorNode(AST left, Lexer.TokenType op, AST right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }
    }


    public static class UnaryOperatorNode extends AST {
        public final Lexer.TokenType op;
        public final AST operand;

        public UnaryOperatorNode(Lexer.TokenType op, AST operand) {
            this.op = op;
            this.operand = operand;
        }
    }


    public static class ColorNode extends AST {
        public final Color color;

        public ColorNode(Color color) { this.color = color; }
    }


    public static class StringNode extends AST {
        public final String string;

        public StringNode(String string) { this.string = string; }
    }


    public static class GlobalNode extends AST {
        public final String name;

        public GlobalNode(String name) { this.name = name; }
    }


    public static class FunctionCallNode extends AST {
        public final String identifier;
        public final ArrayList<AST> arguments = new ArrayList<>();

        public FunctionCallNode(String identifier) {
            this.identifier = identifier;
        }
    }


    public static class NumberNode extends AST {
        public final double number;
        public final String unit;

        public NumberNode(double number, String unit) {
            this.number = number;
            this.unit = unit;
        }
    }


    public static class ValueVariableNode extends AST {
        public final AST nameExpression;

        public ValueVariableNode(AST nameExpression) {
            this.nameExpression = nameExpression;
        }
    }
}
