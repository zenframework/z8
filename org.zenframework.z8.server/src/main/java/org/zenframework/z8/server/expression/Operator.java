package org.zenframework.z8.server.expression;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.eclipse.birt.report.model.api.IllegalOperationException;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.json.parser.JsonUtils;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public enum Operator {

	PLUS("+"),
	MINUS("-"),
	TIMES("*"),
	DIV("/"),
	GT(">"),
	LT("<"),
	GE(">="),
	LE("<="),
	EQ("=="),
	NE("!="),
	NOT("!"),
	AND("&"),
	OR("|"),
	XOR("^");

	private final String operator;

	private Operator(String operator) {
		this.operator = operator;
	}

	public static Operator parse(Token token) {
		return token != null ? parse(token.getText()) : null;
	}

	public static Operator parse(RuleContext ctx) {
		return ctx != null ? parse(ctx.getText()) : null;
	}

	public static Operator parse(String operator) {
		for (Operator op : values())
			if (op.operator.equals(operator))
				return op;
		return null;
	}

	public static Object index(Object array, Object index) {
		if (array == null || index == null)
			return array;

		if (index instanceof decimal)
			index = ((decimal) index).round();

		Object value;

		if (index instanceof string)
			value = new JsonObject(array.toString()).get(((string) index).get());
		else if (index instanceof integer)
			value = new JsonArray(array.toString()).get(((integer) index).getInt());
		else
			throw new IllegalOperationException("Operator.index()");

		if (value == null)
			return null;

		if (value instanceof JsonObject || value instanceof JsonArray)
			return new string(value.toString());

		return JsonUtils.wrap(value);
	}

	public static Object unary(Object value, Operator operator) {
		if (operator == null)
			return value;

		if (value instanceof integer) {
			switch (operator) {
			case PLUS:
				return ((integer) value).operatorAdd();
			case MINUS:
				return ((integer) value).operatorSub();
			default:
				break;
			}
		} else if (value instanceof bool) {
			switch (operator) {
			case NOT:
				return ((bool) value).operatorNot();
			default:
				break;
			}
		}

		throw new IllegalStateException("Illegal operator '" + operator.operator + "' for " + value.getClass().getSimpleName() + " type");
	}

	public static Object binary(Object left, Object right, Operator operator) {
		if (left instanceof integer) {
			integer leftInt = (integer) left;
			switch (operator) {
			case PLUS:
				return right instanceof integer ? leftInt.operatorAdd((integer) right) : leftInt.operatorAdd((decimal) right);
			case MINUS:
				return right instanceof integer ? leftInt.operatorSub((integer) right) : leftInt.operatorSub((decimal) right);
			case TIMES:
				return right instanceof integer ? leftInt.operatorMul((integer) right) : leftInt.operatorMul((decimal) right);
			case DIV:
				return right instanceof integer ? leftInt.operatorDiv((integer) right) : leftInt.operatorDiv((decimal) right);
			case GT:
				return right instanceof integer ? leftInt.operatorMore((integer) right) : leftInt.operatorMore((decimal) right);
			case LT:
				return right instanceof integer ? leftInt.operatorLess((integer) right) : leftInt.operatorLess((decimal) right);
			case GE:
				return right instanceof integer ? leftInt.operatorMoreEqu((integer) right) : leftInt.operatorMoreEqu((decimal) right);
			case LE:
				return right instanceof integer ? leftInt.operatorLessEqu((integer) right) : leftInt.operatorLessEqu((decimal) right);
			case EQ:
				return right instanceof integer ? leftInt.operatorEqu((integer) right) : leftInt.operatorEqu((decimal) right);
			case NE:
				return right instanceof integer ? leftInt.operatorNotEqu((integer) right) : leftInt.operatorNotEqu((decimal) right);
			default:
				break;
			}

		} else if (left instanceof decimal) {
			decimal leftDec = (decimal) left;
			switch (operator) {
			case PLUS:
				return right instanceof integer ? leftDec.operatorAdd((integer) right) : leftDec.operatorAdd((decimal) right);
			case MINUS:
				return right instanceof integer ? leftDec.operatorSub((integer) right) : leftDec.operatorSub((decimal) right);
			case TIMES:
				return right instanceof integer ? leftDec.operatorMul((integer) right) : leftDec.operatorMul((decimal) right);
			case DIV:
				return right instanceof integer ? leftDec.operatorDiv((integer) right) : leftDec.operatorDiv((decimal) right);
			case GT:
				return right instanceof integer ? leftDec.operatorMore((integer) right) : leftDec.operatorMore((decimal) right);
			case LT:
				return right instanceof integer ? leftDec.operatorLess((integer) right) : leftDec.operatorLess((decimal) right);
			case GE:
				return right instanceof integer ? leftDec.operatorMoreEqu((integer) right) : leftDec.operatorMoreEqu((decimal) right);
			case LE:
				return right instanceof integer ? leftDec.operatorLessEqu((integer) right) : leftDec.operatorLessEqu((decimal) right);
			case EQ:
				return right instanceof integer ? leftDec.operatorEqu((integer) right) : leftDec.operatorEqu((decimal) right);
			case NE:
				return right instanceof integer ? leftDec.operatorNotEqu((integer) right) : leftDec.operatorNotEqu((decimal) right);
			default:
				break;
			}

		} else if (left instanceof bool) {
			bool leftBool = (bool) left;
			switch (operator) {
			case AND:
				return leftBool.operatorAnd((bool) right);
			case OR:
				return leftBool.operatorOr((bool) right);
			case EQ:
				return leftBool.operatorEqu((bool) right);
			case NE:
				return leftBool.operatorNotEqu((bool) right);
			default:
				break;
			}

		} else if (left instanceof string) {
			string leftStr = (string) left;
			switch (operator) {
			case PLUS:
				return leftStr.operatorAdd(new string(right != null ? right.toString() : ""));
			case GT:
				return leftStr.operatorMore((string) right);
			case LT:
				return leftStr.operatorLess((string) right);
			case GE:
				return leftStr.operatorMoreEqu((string) right);
			case LE:
				return leftStr.operatorLessEqu((string) right);
			case EQ:
				return leftStr.operatorEqu((string) right);
			case NE:
				return leftStr.operatorNotEqu((string) right);
			default:
				break;
			}
		}

		throw new IllegalStateException("Illegal operator '" + operator.operator + "' for "
					+ (left != null ? left.getClass().getSimpleName() : "[null]")
							+ " and " + (right != null ? right.getClass().getSimpleName() : "[null]") + " types");
	}

	public static boolean isTrue(Object value) {
		return !isFalse(value);
	}

	public static boolean isFalse(Object value) {
		return value == null
				|| value instanceof integer && ((integer) value).get() == 0L
				|| value instanceof bool && !((bool) value).get();
	}

	@SuppressWarnings("rawtypes")
	public static Object getProperty(Object parent, String property) {
		if (parent instanceof CLASS)
			parent = ((CLASS) parent).get();

		if (parent instanceof OBJECT)
			return ((OBJECT) parent).getMember(property);

		try {
			return parent.getClass().getField(property).get(parent);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	public static Object callMethod(Object parent, String name, Object... arguments) {
		if (parent instanceof CLASS)
			parent = ((CLASS) parent).get();

		try {
			return parent.getClass().getMethod(name, asClasses(arguments)).invoke(parent, arguments);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Class<?>[] asClasses(Object... objects) {
		Class<?>[] classes = new Class[objects.length];

		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			classes[i] = object != null ? object.getClass() : Object.class;
		}

		return classes;
	}
}
