package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;

public class OperatorToken extends Token {
    public OperatorToken() {}

    public OperatorToken(int id, IPosition position) {
        super(id, position);
    }

    public String getName() {
        return KeywordToken.Operator + " " + getSign();
    }

    public String getJavaName() {
        return KeywordToken.Operator + getJavaSign();
    }

    public String getSign() {
        switch(getId()) {
        case IToken.EQU:
            return "==";
        case IToken.NOT_EQU:
            return "!=";
        case IToken.LESS:
            return "<";
        case IToken.MORE:
            return ">";
        case IToken.LESS_EQU:
            return "<=";
        case IToken.MORE_EQU:
            return ">=";
        case IToken.PLUS:
            return "+";
        case IToken.MINUS:
            return "-";
        case IToken.MUL:
            return "*";
        case IToken.DIV:
            return "/";
        case IToken.MOD:
            return "%";
        case IToken.CARET:
            return "^";
        case IToken.MUL_CARET:
            return "*^";
        case IToken.CARET_MUL:
            return "^*";
        case IToken.MUL_CARET_MUL:
            return "*^*";

        case IToken.NOT:
            return "!";
        case IToken.AND:
            return "&";
        case IToken.OR:
            return "|";

        case IToken.ASSIGN:
            return "=";
        case IToken.ADD_ASSIGN:
            return "+=";
        case IToken.SUB_ASSIGN:
            return "-=";
        case IToken.MUL_ASSIGN:
            return "*=";
        case IToken.DIV_ASSIGN:
            return "/=";
        case IToken.MOD_ASSIGN:
            return "%=";
        case IToken.CARET_ASSIGN:
            return "^=";
        case IToken.MUL_CARET_ASSIGN:
            return "*^=";
        case IToken.CARET_MUL_ASSIGN:
            return "^*=";
        case IToken.MUL_CARET_MUL_ASSIGN:
            return "*^*=";
        }

        throw new UnsupportedOperationException();
    }

    public String getJavaSign() {
        switch(getId()) {
        case IToken.EQU:
            return "Equ";
        case IToken.NOT_EQU:
            return "NotEqu";
        case IToken.LESS:
            return "Less";
        case IToken.MORE:
            return "More";
        case IToken.LESS_EQU:
            return "LessEqu";
        case IToken.MORE_EQU:
            return "MoreEqu";
        case IToken.PLUS:
            return "Add";
        case IToken.MINUS:
            return "Sub";
        case IToken.MUL:
            return "Mul";
        case IToken.DIV:
            return "Div";
        case IToken.MOD:
            return "Mod";
        case IToken.CARET:
            return "Caret";
        case IToken.MUL_CARET:
            return "MulCaret";
        case IToken.CARET_MUL:
            return "CaretMul";
        case IToken.MUL_CARET_MUL:
            return "MulCaretMul";

        case IToken.NOT:
            return "Not";
        case IToken.AND:
            return "And";
        case IToken.OR:
            return "Or";
        case IToken.ASSIGN:
            return "Assign";
        case IToken.ADD_ASSIGN:
            return "AddAssign";
        case IToken.SUB_ASSIGN:
            return "SubAssign";
        case IToken.MUL_ASSIGN:
            return "MulAssign";
        case IToken.DIV_ASSIGN:
            return "DivAssign";
        case IToken.MOD_ASSIGN:
            return "ModAssign";

        case IToken.CARET_ASSIGN:
            return "CaretAssign";
        case IToken.MUL_CARET_ASSIGN:
            return "MulCaretAssign";
        case IToken.CARET_MUL_ASSIGN:
            return "CaretMulAssign";
        case IToken.MUL_CARET_MUL_ASSIGN:
            return "MulCaretMulAssign";
        }

        throw new UnsupportedOperationException();
    }
}
