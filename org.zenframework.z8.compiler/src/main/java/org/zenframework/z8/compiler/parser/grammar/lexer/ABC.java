package org.zenframework.z8.compiler.parser.grammar.lexer;

public class ABC {
    public static int TabulatorLength = 4;

    public static boolean isAllowed(char chr) {
        return chr != '\\' && chr != '^' && chr != '`' && chr != '~' && chr != '$' && chr != '#' && chr != '@'
                && Character.isJavaIdentifierPart(chr);
    }

    public static boolean isPrintable(char chr) {
        return chr > ' ';
    }

    public static boolean isWhiteSpace(char chr) {
        return chr == ' ' || chr == '\t';
    }

    public static boolean isTabulator(char chr) {
        return chr == '\t';
    }

    public static boolean isDigit(char chr) {
        return '0' <= chr && chr <= '9';
    }

    public static boolean isXDigit(char chr) {
        return isDigit(chr) || ('a' <= chr && chr <= 'f') || ('A' <= chr && chr <= 'F');
    }

    public static boolean isDigit(char chr, int base) {
        assert (base == 10 || base == 16);
        return base == 10 ? isDigit(chr) : isXDigit(chr);
    }

    public static boolean isPunctuator(char chr) {
        return ('!' <= chr && chr <= '/') || (':' <= chr && chr <= '@') || ('[' <= chr && chr <= '`')
                || ('{' <= chr && chr <= '~');
    }

    public static boolean isAlpha(char chr) {
        return isPrintable(chr) && !isPunctuator(chr) && !isDigit(chr) && isAllowed(chr);
    }

    public static boolean isLineBreak(char chr) {
        return chr == '\n' || chr == '\r';
    }

    public static boolean isAlnum(char chr) {
        return isAlpha(chr) || isDigit(chr);
    }
}