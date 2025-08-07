package io.github.mrmaxguns.freepapermaps.styling.language;

import io.github.mrmaxguns.freepapermaps.UserInputException;

import java.awt.*;


public class Lexer {
    private final String text;
    private int pos = 0;
    private char currentChar;

    public Lexer(String text) throws UserInputException {
        if (text.isEmpty()) {
            throw new UserInputException("Unexpected end of input.");
        }

        this.text = text;
        this.currentChar = text.charAt(pos);
    }

    private static boolean isHexDigit(char c) {
        c = Character.toLowerCase(c);
        return Character.isDigit(c) || c == 'a' || c == 'b' || c == 'd' || c == 'e' || c == 'f';
    }

    public Token getNextToken() throws UserInputException {
        while (currentChar != 0) {
            if (Character.isWhitespace(currentChar)) {
                skipWhitespace();
                continue;
            }

            if (Character.isDigit(currentChar)) {
                return handlePositiveNumericLiteral();
            }

            if (currentChar == '#') {
                return handleHexColorLiteral();
            }

            if (currentChar == '\'' || currentChar == '"') {
                return handleStringLiteral();
            }

            if (currentChar == '+') {
                advance();
                return new Token(TokenType.PLUS_OPERATOR);
            }

            if (currentChar == '-') {
                advance();
                return new Token(TokenType.MINUS_OR_NEG_OPERATOR);
            }

            if (currentChar == '*') {
                advance();
                return new Token(TokenType.MUL_OPERATOR);
            }

            if (currentChar == '/') {
                advance();
                return new Token(TokenType.DIV_OPERATOR);
            }

            if (currentChar == '|') {
                advance();
                return new Token(TokenType.FALLBACK_OPERATOR);
            }

            if (currentChar == '%') {
                advance();
                return new Token(TokenType.GLOBAL_MARKER);
            }

            if (currentChar == '$') {
                advance();
                return new Token(TokenType.VALUE_VARIABLE_MARKER);
            }

            if (currentChar == '{') {
                advance();
                return new Token(TokenType.L_BRACKET);
            }

            if (currentChar == '}') {
                advance();
                return new Token(TokenType.R_BRACKET);
            }

            if (Character.isAlphabetic(currentChar) || currentChar == '_') {
                return handleFunctionIdentifier();
            }

            if (currentChar == '(') {
                advance();
                return new Token(TokenType.L_PAREN);
            }

            if (currentChar == ')') {
                advance();
                return new Token(TokenType.R_PAREN);
            }

            if (currentChar == '!') {
                advance();
                return new Token(TokenType.RECIPE_MARKER);
            }

            if (currentChar == ',') {
                advance();
                return new Token(TokenType.COMMA);
            }

            throw error("Invalid character '" + currentChar + "'.");
        }

        return new Token(TokenType.EOF);
    }

    private void advance() {
        if (++pos > text.length() - 1) {
            currentChar = 0;
        } else {
            currentChar = text.charAt(pos);
        }
    }

    private char peek() {
        int peekPos = pos + 1;
        if (peekPos > text.length() - 1) {
            return 0;
        }
        return text.charAt(peekPos);
    }

    public UserInputException error(String message) {
        String context = "~~~\n";
        context += text + "\n";
        context += " ".repeat(pos - 1) + "^" + "\n";
        context += "~~~\n";
        return new UserInputException(context + "Parse error at character " + pos + ": " + message);
    }

    private void advanceAndExpectChar() throws UserInputException {
        advance();
        if (currentChar == 0) {
            throw error("Unexpected end of input.");
        }
    }

    private void skipWhitespace() {
        while (currentChar != 0 && Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    private Token handlePositiveNumericLiteral() throws UserInputException {
        StringBuilder number = new StringBuilder();
        StringBuilder unit = new StringBuilder();

        while (currentChar != 0 && (Character.isDigit(currentChar))) {
            number.append(currentChar);
            advance();
        }

        if (currentChar == '.') {
            number.append(currentChar);
            advanceAndExpectChar();

            while (currentChar != 0 && (Character.isDigit(currentChar))) {
                number.append(currentChar);
                advance();
            }
        }

        // No space between number and unit is permitted
        while (currentChar != 0 && (Character.isAlphabetic(currentChar))) {
            unit.append(currentChar);
            advance();
        }

        return new PositiveNumericToken(Double.parseDouble(number.toString()), unit.toString());
    }

    private Token handleHexColorLiteral() throws UserInputException {
        // No space between '#' and first number is permitted
        advanceAndExpectChar();

        StringBuilder hexLiteral = new StringBuilder("#");

        int digitCount = 0;
        while (currentChar != 0 && isHexDigit(currentChar) && digitCount < 6) {
            hexLiteral.append(currentChar);
            advance();
            ++digitCount;
        }

        String normalizedHexLiteral;
        if (digitCount < 3) {
            throw error("Expected 3 or 6 digits in hex literal, but got less than 3.");
        } else if (digitCount == 3) {
            String raw = hexLiteral.toString();
            normalizedHexLiteral =
                    "#" + raw.charAt(1) + raw.charAt(1) + raw.charAt(2) + raw.charAt(2) + raw.charAt(3) + raw.charAt(3);
        } else if (digitCount < 6) {
            throw error("Expected 3 or 6 digits in hex literal, but got " + digitCount + ".");
        } else {
            normalizedHexLiteral = hexLiteral.toString();
        }

        return new ColorToken(Color.decode(normalizedHexLiteral));
    }

    private Token handleStringLiteral() throws UserInputException {
        char quote = currentChar;

        StringBuilder string = new StringBuilder();

        advance();
        while (currentChar != quote) {
            if (currentChar == 0) {
                throw error("Unexpected end to string literal.");
            } else if (currentChar == '\\') {
                advanceAndExpectChar();
                if (currentChar == '\\') {
                    string.append('\\');
                } else if (currentChar == quote) {
                    string.append(quote);
                } else {
                    throw error("Backslash '\\' must be escaped.");
                }
            } else {
                string.append(currentChar);
            }
            advance();
        }
        advance();

        return new StringToken(string.toString());
    }

    private Token handleFunctionIdentifier() {
        StringBuilder result = new StringBuilder();

        while (currentChar != 0 && isFunctionIdentifierCharacter(currentChar)) {
            result.append(currentChar);
            advance();
        }

        return new FunctionIdentifierToken(result.toString());
    }

    private boolean isFunctionIdentifierCharacter(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c) || c == '_';
    }

    public enum TokenType {
        EOF,

        POSITIVE_NUMERIC_LITERAL, HEX_COLOR_LITERAL, STRING_LITERAL,

        PLUS_OPERATOR, MINUS_OR_NEG_OPERATOR, MUL_OPERATOR, DIV_OPERATOR, FALLBACK_OPERATOR,

        GLOBAL_MARKER, VALUE_VARIABLE_MARKER, L_BRACKET, R_BRACKET,

        FUNCTION_IDENTIFIER, L_PAREN, R_PAREN, COMMA,

        RECIPE_MARKER,
    }


    public static class Token {
        public final TokenType type;

        public Token(TokenType type) {
            this.type = type;
        }

        public String toString() {
            return type.toString();
        }
    }


    public static class PositiveNumericToken extends Token {
        double value;
        String unit;

        public PositiveNumericToken(double value, String unit) {
            super(TokenType.POSITIVE_NUMERIC_LITERAL);
            this.value = value;
            this.unit = unit;
        }

        public String toString() {
            return value + unit;
        }
    }


    public static class ColorToken extends Token {
        Color value;

        public ColorToken(Color value) {
            super(TokenType.HEX_COLOR_LITERAL);
            this.value = value;
        }

        public String toString() {
            return value.toString();
        }
    }


    public static class StringToken extends Token {
        String value;

        public StringToken(String value) {
            super(TokenType.STRING_LITERAL);
            this.value = value;
        }

        public String toString() {
            return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
    }


    public static class FunctionIdentifierToken extends Token {
        String value;

        public FunctionIdentifierToken(String value) {
            super(TokenType.FUNCTION_IDENTIFIER);
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }
}
