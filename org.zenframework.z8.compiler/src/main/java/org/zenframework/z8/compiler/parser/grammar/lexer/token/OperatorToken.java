package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;

public class OperatorToken extends Token {
	public OperatorToken() {
	}

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
		case IToken.ADD_ASSIGN:
			return "+";
		case IToken.MINUS:
		case IToken.SUB_ASSIGN:
			return "-";
		case IToken.MUL:
		case IToken.MUL_ASSIGN:
			return "*";
		case IToken.DIV:
		case IToken.DIV_ASSIGN:
			return "/";
		case IToken.MOD:
		case IToken.MOD_ASSIGN:
			return "%";

		case IToken.NOT:
			return "!";
		case IToken.AND:
			return "&";
		case IToken.OR:
			return "|";

		case IToken.ASSIGN:
			return "=";
/*
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
*/
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
		case IToken.ADD_ASSIGN:
			return "Add";
		case IToken.MINUS:
		case IToken.SUB_ASSIGN:
			return "Sub";
		case IToken.MUL:
		case IToken.MUL_ASSIGN:
			return "Mul";
		case IToken.DIV:
		case IToken.DIV_ASSIGN:
			return "Div";
		case IToken.MOD:
		case IToken.MOD_ASSIGN:
			return "Mod";

		case IToken.NOT:
			return "Not";
		case IToken.AND:
			return "And";
		case IToken.OR:
			return "Or";

		case IToken.ASSIGN:
			return "Assign";
/*
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
*/
		}

		throw new UnsupportedOperationException();
	}
}
