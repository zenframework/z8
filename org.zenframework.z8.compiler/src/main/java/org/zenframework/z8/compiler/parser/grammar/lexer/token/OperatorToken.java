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
		case IToken.Not:
			return "!";
		case IToken.BitwiseNot:
			return "~";

		case IToken.Plus:
		case IToken.AddAssign:
			return "+";
		case IToken.Minus:
		case IToken.SubAssign:
			return "-";
		case IToken.Mul:
		case IToken.MulAssign:
			return "*";
		case IToken.Div:
		case IToken.DivAssign:
			return "/";
		case IToken.Mod:
		case IToken.ModAssign:
			return "%";

		case IToken.Equ:
			return "==";
		case IToken.NotEqu:
			return "!=";
		case IToken.Less:
			return "<";
		case IToken.More:
			return ">";
		case IToken.LessEqu:
			return "<=";
		case IToken.MoreEqu:
			return ">=";

		case IToken.BitwiseAnd:
		case IToken.BitwiseAndAssign:
			return "&";
		case IToken.BitwiseOr:
		case IToken.BitwiseOrAssign:
			return "|";
		case IToken.BitwiseXor:
		case IToken.BitwiseXorAssign:
			return "^";

		case IToken.And:
			return "&&";
		case IToken.Or:
			return "||";

		case IToken.Assign:
			return "=";
		}

		throw new UnsupportedOperationException();
	}

	public String getJavaSign() {
		switch(getId()) {
		case IToken.Not:
			return "Not";
		case IToken.BitwiseNot:
			return "Not";

		case IToken.Plus:
		case IToken.AddAssign:
			return "Add";
		case IToken.Minus:
		case IToken.SubAssign:
			return "Sub";
		case IToken.Mul:
		case IToken.MulAssign:
			return "Mul";
		case IToken.Div:
		case IToken.DivAssign:
			return "Div";
		case IToken.Mod:
		case IToken.ModAssign:
			return "Mod";

		case IToken.Equ:
			return "Equ";
		case IToken.NotEqu:
			return "NotEqu";
		case IToken.Less:
			return "Less";
		case IToken.More:
			return "More";
		case IToken.LessEqu:
			return "LessEqu";
		case IToken.MoreEqu:
			return "MoreEqu";

		case IToken.BitwiseAnd:
		case IToken.BitwiseAndAssign:
			return "BitwiseAnd";
		case IToken.BitwiseOr:
		case IToken.BitwiseOrAssign:
			return "BitwiseOr";
		case IToken.BitwiseXor:
		case IToken.BitwiseXorAssign:
			return "Xor";

		case IToken.And:
			return "And";
		case IToken.Or:
			return "Or";

		case IToken.Assign:
			return "Assign";
		}

		throw new UnsupportedOperationException();
	}
}
