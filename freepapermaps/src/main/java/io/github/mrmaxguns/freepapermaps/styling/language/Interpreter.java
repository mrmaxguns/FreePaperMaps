package io.github.mrmaxguns.freepapermaps.styling.language;

import io.github.mrmaxguns.freepapermaps.UserInputException;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;


public class Interpreter {
    private final Parser.AST ast;

    public Interpreter(Parser parser) throws UserInputException {
        this.ast = parser.parse();
    }

    private static double applyNumericBinaryOperation(Parser.BinaryOperatorNode ast, NumericPrimitive left,
                                                      NumericPrimitive right) {
        double leftVal = left.value;
        double rightVal = right.value;

        double result;
        if (ast.op == Lexer.TokenType.PLUS_OPERATOR) {
            result = leftVal + rightVal;
        } else if (ast.op == Lexer.TokenType.MINUS_OR_NEG_OPERATOR) {
            result = leftVal - rightVal;
        } else if (ast.op == Lexer.TokenType.MUL_OPERATOR) {
            result = leftVal * rightVal;
        } else if (ast.op == Lexer.TokenType.DIV_OPERATOR) {
            result = leftVal / rightVal;
        } else {
            throw new RuntimeException("Invalid control flow");
        }

        return result;
    }

    public double interpretExpectingUnitless(Context context) throws UserInputException {
        return ((NumericPrimitive) interpretExpectingType(context, Primitive.Type.Unitless)).value;
    }

    public double interpretExpectingDistance(Context context) throws UserInputException {
        return ((NumericPrimitive) interpretExpectingType(context, Primitive.Type.Distance)).value;
    }

    public double interpretExpectingAngle(Context context) throws UserInputException {
        return ((NumericPrimitive) interpretExpectingType(context, Primitive.Type.Angle)).value;
    }

    public Color interpretExpectingColor(Context context) throws UserInputException {
        return ((ColorPrimitive) interpretExpectingType(context, Primitive.Type.Color)).value;
    }

    public String interpretExpectingString(Context context) throws UserInputException {
        return ((StringPrimitive) interpretExpectingType(context, Primitive.Type.String)).value;
    }

    private Primitive interpretExpectingType(Context context, Primitive.Type type) throws UserInputException {
        Primitive result = interpret(context);
        if (result.type != type) {
            throw new UserInputException(
                    "Expected expression of type " + type.name() + ", but got expression of type " +
                    result.type.name() + ".");
        }
        return result;
    }

    public Primitive interpret(Context context) throws UserInputException {
        return visit(ast, context);
    }

    private Primitive visit(Parser.AST ast, Context context) throws UserInputException {
        if (ast instanceof Parser.RecipeNode) {
            return visitRecipeNode((Parser.RecipeNode) ast, context);
        }

        if (ast instanceof Parser.BinaryOperatorNode) {
            return visitBinaryOperatorNode((Parser.BinaryOperatorNode) ast, context);
        }

        if (ast instanceof Parser.UnaryOperatorNode) {
            return visitUnaryOperatorNode((Parser.UnaryOperatorNode) ast, context);
        }

        if (ast instanceof Parser.ColorNode) {
            return visitColorNode((Parser.ColorNode) ast, context);
        }

        if (ast instanceof Parser.StringNode) {
            return visitStringNode((Parser.StringNode) ast, context);
        }

        if (ast instanceof Parser.GlobalNode) {
            return visitGlobalNode((Parser.GlobalNode) ast, context);
        }

        if (ast instanceof Parser.FunctionCallNode) {
            return visitFunctionCallNode((Parser.FunctionCallNode) ast, context);
        }

        if (ast instanceof Parser.NumberNode) {
            return visitNumberNode((Parser.NumberNode) ast, context);
        }

        if (ast instanceof Parser.ValueVariableNode) {
            return visitValueVariableNode((Parser.ValueVariableNode) ast, context);
        }

        throw new RuntimeException("Encountered unsupported AST node.");
    }

    private Primitive visitRecipeNode(Parser.RecipeNode ast, Context context) throws UserInputException {
        Function<Context, Primitive> recipe = context.recipes.get(ast.recipeName);
        if (recipe == null) {
            throw new UserInputException("Could not find recipe '" + ast.recipeName + ".");
        }
        return recipe.apply(context);
    }

    private Primitive visitBinaryOperatorNode(Parser.BinaryOperatorNode ast, Context context) throws
            UserInputException {

        Primitive left = visit(ast.left, context);
        Primitive right = visit(ast.right, context);

        if (ast.op == Lexer.TokenType.FALLBACK_OPERATOR) {
            if (left.isTruthy()) {
                return left;
            } else {
                return right;
            }
        }

        if (ast.op == Lexer.TokenType.PLUS_OPERATOR || ast.op == Lexer.TokenType.MINUS_OR_NEG_OPERATOR ||
            ast.op == Lexer.TokenType.MUL_OPERATOR || ast.op == Lexer.TokenType.DIV_OPERATOR) {
            Primitive.Type resultType = getNumericResultType(left, right);
            double result = applyNumericBinaryOperation(ast, (NumericPrimitive) left, (NumericPrimitive) right);
            return new NumericPrimitive(resultType, result);
        }

        throw new RuntimeException("Found unsupported binary operator.");
    }

    private Primitive visitUnaryOperatorNode(Parser.UnaryOperatorNode ast, Context context) throws UserInputException {
        Primitive val = visit(ast.operand, context);

        if (ast.op == Lexer.TokenType.MINUS_OR_NEG_OPERATOR) {
            if (!(val instanceof NumericPrimitive)) {
                throw new UserInputException("Numeric operation on non-numeric type.");
            }
            return new NumericPrimitive(val.type, ((NumericPrimitive) val).value * -1);
        }

        throw new RuntimeException("Found unsupported unary operator.");
    }

    private Primitive visitColorNode(Parser.ColorNode ast, Context context) {
        return new ColorPrimitive(ast.color);
    }

    private Primitive visitStringNode(Parser.StringNode ast, Context context) {
        return new StringPrimitive(ast.string);
    }

    private Primitive visitGlobalNode(Parser.GlobalNode ast, Context context) throws UserInputException {
        Primitive val = context.globals.get(ast.name);
        if (val == null) {
            val = new StringPrimitive("");
        }
        return val;
    }

    private Primitive visitFunctionCallNode(Parser.FunctionCallNode ast, Context context) throws UserInputException {
        BiFunction<Context, java.util.List<Primitive>, Interpreter.Primitive> function = context.functions()
                .get(ast.identifier);

        if (function == null) {
            throw new UserInputException("Could not find function '" + ast.identifier + "'.");
        }

        java.util.List<Primitive> arguments = new ArrayList<>();
        for (Parser.AST p : ast.arguments) {
            arguments.add(visit(p, context));
        }

        try {
            return function.apply(context, arguments);
        } catch (IllegalArgumentException e) {
            throw new UserInputException(e.getMessage());
        }
    }

    private Primitive visitNumberNode(Parser.NumberNode ast, Context context) throws UserInputException {
        UnitManager mgr = context.unitManager;
        if (ast.unit.isBlank()) {
            return new NumericPrimitive(Primitive.Type.Unitless, ast.number);
        } else if (mgr.isDistanceUnit(ast.unit)) {
            return new NumericPrimitive(Primitive.Type.Distance, mgr.parseDistanceWithUnit(ast.number, ast.unit));
        } else if (mgr.isAngleUnit(ast.unit)) {
            return new NumericPrimitive(Primitive.Type.Angle, mgr.parseAngleWithUnit(ast.number, ast.unit));
        } else {
            throw new UserInputException("Invalid unit '" + ast.unit + "'.");
        }
    }

    private Primitive visitValueVariableNode(Parser.ValueVariableNode ast, Context context) throws UserInputException {
        String name = visit(ast.nameExpression, context).stringify();
        String value = context.valueVariables.get(name);

        if (value == null) {
            value = "";
        }

        return new StringPrimitive(value);
    }

    private Primitive.Type getNumericResultType(Primitive a, Primitive b) throws UserInputException {
        if (!(a instanceof NumericPrimitive) || !(b instanceof NumericPrimitive)) {
            throw new UserInputException("Numeric operation on non-numeric type.");
        }

        if (a.type == Primitive.Type.Unitless && b.type == Primitive.Type.Unitless) {
            return Primitive.Type.Unitless;
        }

        if (a.type == Primitive.Type.Unitless) {
            return b.type;
        }

        if (b.type == Primitive.Type.Unitless) {
            return a.type;
        }

        if (a.type == b.type) {
            return a.type;
        }

        throw new UserInputException("Mixing of different numeric types is not supported.");
    }

    public record Context(Map<String, Function<Context, Primitive>> recipes, Map<String, Primitive> globals,
                          Map<String, String> valueVariables,
                          Map<String, BiFunction<Context, java.util.List<Primitive>, Interpreter.Primitive>> functions,
                          UnitManager unitManager) {}


    public static abstract class Primitive {
        public final Type type;


        public Primitive(Type type) { this.type = type; }


        ;

        public abstract boolean isTruthy();

        public abstract String stringify();

        public enum Type {Unitless, Distance, Angle, Color, String}
    }


    public static class NumericPrimitive extends Primitive {
        public final double value;

        public NumericPrimitive(Type type, double value) {
            super(type);
            this.value = value;
        }

        @Override
        public boolean isTruthy() {
            return value == 0;
        }

        @Override
        public String stringify() {
            return Double.toString(value);
        }
    }


    public static class ColorPrimitive extends Primitive {
        Color value;

        public ColorPrimitive(Color value) {
            super(Type.Color);
            this.value = value;
        }

        @Override
        public boolean isTruthy() {
            return true;
        }

        @Override
        public String stringify() {
            return String.format("#%02x%02x%02x", value.getRed(), value.getGreen(), value.getBlue());
        }
    }


    public static class StringPrimitive extends Primitive {
        String value;

        public StringPrimitive(String value) {
            super(Type.String);
            this.value = value;
        }

        @Override
        public boolean isTruthy() {
            return !value.isEmpty();
        }

        @Override
        public String stringify() {
            return value;
        }
    }
}
