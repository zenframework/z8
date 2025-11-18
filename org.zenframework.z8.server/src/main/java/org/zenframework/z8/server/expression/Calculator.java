package org.zenframework.z8.server.expression;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.birt.report.model.api.IllegalOperationException;
import org.zenframework.z8.server.expression.function.Format;
import org.zenframework.z8.server.expression.function.Function;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Calculator {

	private static final Map<String, Function> Functions = new HashMap<String, Function>();

	public static void register(String name, Function function) {
		Functions.put(name, function);
	}

	static {
		register(Format.NAME, new Format());
	}

	private final List<String> errors = new LinkedList<String>();

	private Expression.Getter getter = null;
	private Context context = null;
	private boolean silent = false;

	public Calculator setGetter(Expression.Getter getter) {
		this.getter = getter;
		return this;
	}

	public Calculator setContext(Context context) {
		this.context = context;
		return this;
	}

	public boolean isSilent() {
		return silent;
	}

	public Calculator setSilent(boolean silent) {
		this.silent = silent;
		return this;
	}

	public Variable getVariable(String name) {
		Variable variable = context != null ? context.getVariable(name) : null;

		if (!silent && variable == null)
			addError(new MessageFormat(Resources.getByKey("$Expression.variableIsNotSet$")).format(new Object[] { name }));

		return variable;
	}

	public Value getVariableValue(String name) {
		Variable variable = getVariable(name);
		Value value = new Value();

		return variable != null ? value.setValue(getValue(variable.getValue())) : value.setExpression(name);
	}

	public List<String> getErrors() {
		return errors;
	}

	public Calculator addError(String error) {
		errors.add(error);
		return this;
	}

	public Calculator clear() {
		errors.clear();
		return this;
	}

	public Object getValue(Object value) {
		return getter != null ? getter.getValue(wrap(value)) : wrap(value);
	}

	@SuppressWarnings("rawtypes")
	public Object getItem(Object array, Object index) {
		if (array == null || index == null)
			return array;

		if (index instanceof decimal)
			index = ((decimal) index).round();

		Object value;

		if (index instanceof string) {
			string indexStr = (string) index;

			if (array instanceof org.zenframework.z8.server.base.json.parser.JsonObject)
				array = ((org.zenframework.z8.server.base.json.parser.JsonObject) array).get();

			if (array instanceof Map) {
				value = ((Map) array).get(indexStr);
			} else {
				value = null;
				addError("Illegal operator " + array.getClass().getName() + "[\"" + indexStr + "\"]");
			}

		} else if (index instanceof integer) {
			int indexInt = ((integer) index).getInt();

			if (array instanceof org.zenframework.z8.server.base.json.parser.JsonArray)
				array = ((org.zenframework.z8.server.base.json.parser.JsonArray) array).get();

			if (array instanceof JsonArray) {
				value = ((JsonArray) array).get(indexInt);
			} else if (array instanceof RCollection) {
				value = ((RCollection) array).get(indexInt);
			} else if (array instanceof RLinkedHashMap) {
				value = ((RLinkedHashMap) array).get(indexInt);
			} else {
				value = null;
				addError("Illegal operator " + array.getClass().getName() + '[' + indexInt + ']');
			}
		} else {
			throw new IllegalOperationException("Illegal operator [" + index.getClass().getSimpleName() + ']');
		}

		return getValue(value);
	}

	public Object unary(Object value, Operator operator) {
		if (operator == null)
			return value;

		if (value instanceof integer) {
			switch (operator) {
			case PLUS:
				return getValue(((integer) value).operatorAdd());
			case MINUS:
				return getValue(((integer) value).operatorSub());
			default:
				break;
			}
		} else if (value instanceof bool) {
			switch (operator) {
			case NOT:
				return getValue(((bool) value).operatorNot());
			default:
				break;
			}
		}

		throw new IllegalStateException("Illegal operator '" + operator + "' for " + value.getClass().getSimpleName() + " type");
	}

	public Object binary(Object left, Object right, Operator operator) {
		if (left instanceof integer) {
			integer leftInt = (integer) left;
			switch (operator) {
			case PLUS:
				return getValue(right instanceof integer ? leftInt.operatorAdd((integer) right) : leftInt.operatorAdd((decimal) right));
			case MINUS:
				return getValue(right instanceof integer ? leftInt.operatorSub((integer) right) : leftInt.operatorSub((decimal) right));
			case TIMES:
				return getValue(right instanceof integer ? leftInt.operatorMul((integer) right) : leftInt.operatorMul((decimal) right));
			case DIV:
				return getValue(right instanceof integer ? leftInt.operatorDiv((integer) right) : leftInt.operatorDiv((decimal) right));
			case GT:
				return getValue(right instanceof integer ? leftInt.operatorMore((integer) right) : leftInt.operatorMore((decimal) right));
			case LT:
				return getValue(right instanceof integer ? leftInt.operatorLess((integer) right) : leftInt.operatorLess((decimal) right));
			case GE:
				return getValue(right instanceof integer ? leftInt.operatorMoreEqu((integer) right) : leftInt.operatorMoreEqu((decimal) right));
			case LE:
				return getValue(right instanceof integer ? leftInt.operatorLessEqu((integer) right) : leftInt.operatorLessEqu((decimal) right));
			case EQ:
				return getValue(right instanceof integer ? leftInt.operatorEqu((integer) right) : leftInt.operatorEqu((decimal) right));
			case NE:
				return getValue(right instanceof integer ? leftInt.operatorNotEqu((integer) right) : leftInt.operatorNotEqu((decimal) right));
			default:
				break;
			}

		} else if (left instanceof decimal) {
			decimal leftDec = (decimal) left;
			switch (operator) {
			case PLUS:
				return getValue(right instanceof integer ? leftDec.operatorAdd((integer) right) : leftDec.operatorAdd((decimal) right));
			case MINUS:
				return getValue(right instanceof integer ? leftDec.operatorSub((integer) right) : leftDec.operatorSub((decimal) right));
			case TIMES:
				return getValue(right instanceof integer ? leftDec.operatorMul((integer) right) : leftDec.operatorMul((decimal) right));
			case DIV:
				return getValue(right instanceof integer ? leftDec.operatorDiv((integer) right) : leftDec.operatorDiv((decimal) right));
			case GT:
				return getValue(right instanceof integer ? leftDec.operatorMore((integer) right) : leftDec.operatorMore((decimal) right));
			case LT:
				return getValue(right instanceof integer ? leftDec.operatorLess((integer) right) : leftDec.operatorLess((decimal) right));
			case GE:
				return getValue(right instanceof integer ? leftDec.operatorMoreEqu((integer) right) : leftDec.operatorMoreEqu((decimal) right));
			case LE:
				return getValue(right instanceof integer ? leftDec.operatorLessEqu((integer) right) : leftDec.operatorLessEqu((decimal) right));
			case EQ:
				return getValue(right instanceof integer ? leftDec.operatorEqu((integer) right) : leftDec.operatorEqu((decimal) right));
			case NE:
				return getValue(right instanceof integer ? leftDec.operatorNotEqu((integer) right) : leftDec.operatorNotEqu((decimal) right));
			default:
				break;
			}

		} else if (left instanceof bool) {
			bool leftBool = (bool) left;
			switch (operator) {
			case AND:
				return getValue(leftBool.operatorAnd((bool) right));
			case OR:
				return getValue(leftBool.operatorOr((bool) right));
			case EQ:
				return getValue(leftBool.operatorEqu((bool) right));
			case NE:
				return getValue(leftBool.operatorNotEqu((bool) right));
			default:
				break;
			}

		} else if (left instanceof string) {
			string leftStr = (string) left;
			switch (operator) {
			case PLUS:
				return getValue(leftStr.operatorAdd(new string(right != null ? right.toString() : "")));
			case GT:
				return getValue(leftStr.operatorMore((string) right));
			case LT:
				return getValue(leftStr.operatorLess((string) right));
			case GE:
				return getValue(leftStr.operatorMoreEqu((string) right));
			case LE:
				return getValue(leftStr.operatorLessEqu((string) right));
			case EQ:
				return getValue(leftStr.operatorEqu((string) right));
			case NE:
				return getValue(leftStr.operatorNotEqu((string) right));
			default:
				break;
			}
		}

		throw new IllegalStateException("Illegal operator '" + operator + "' for "
					+ (left != null ? left.getClass().getSimpleName() : "[null]")
							+ " and " + (right != null ? right.getClass().getSimpleName() : "[null]") + " types");
	}

	@SuppressWarnings("rawtypes")
	public Object getProperty(Object parent, String property) {
		if (parent instanceof OBJECT) {
			CLASS value = (CLASS) ((OBJECT) parent).getMember(property);
			if (value != null)
				return getValue(value);
		}

		try {
			return getValue(parent.getClass().getField(property).get(parent));
		} catch (NoSuchFieldException e) {
			// Try to find public getter
			return callMethod(parent, getterName(property));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object callMethod(Object object, String name, Object... arguments) {
		try {
			Class<?> cls = object instanceof Class ? (Class<?>) object : object.getClass();
			object = object instanceof Class ? null : object;
			try {
				return getValue(cls.getMethod(name, asClasses(arguments)).invoke(object, arguments));
			} catch (NoSuchMethodException e) {
				return getValue(cls.getMethod("z8_" + name, asClasses(arguments)).invoke(object, arguments));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object callFunction(String name, Object... arguments) {
		Function function = Functions.get(name);

		if (function == null)
			throw new RuntimeException("Unknown function '" + name + "'");

		return getValue(function.call(arguments));
	}

	public static boolean isTrue(Object value) {
		return !isFalse(value);
	}

	public static boolean isFalse(Object value) {
		return value == null
				|| value instanceof integer && ((integer) value).get() == 0L
				|| value instanceof bool && !((bool) value).get();
	}

	private static Class<?>[] asClasses(Object... objects) {
		Class<?>[] classes = new Class[objects.length];

		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			classes[i] = object != null ? object.getClass() : Object.class;
		}

		return classes;
	}

	private static String getterName(String field) {
		return "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
	}

	@SuppressWarnings("rawtypes")
	private static Object wrap(Object o) {
		if (o instanceof CLASS)
			o = ((CLASS) o).get();

		if (o == null || JsonObject.NULL.equals(o))
			return null;
		if (o instanceof Boolean)
			return new bool((Boolean) o);
		if (o instanceof GregorianCalendar)
			return new date((GregorianCalendar) o);
		if (o instanceof Float || o instanceof Double)
			return new decimal((Double) o);
		if (o instanceof BigDecimal)
			return new decimal((BigDecimal) o);
		if (o instanceof UUID)
			return new guid((UUID) o);
		if (o instanceof Byte || o instanceof Short || o instanceof Integer || o instanceof Long)
			return new integer(((Number) o).longValue());
		if (o instanceof String)
			return new string((String) o);
		//if (o instanceof org.zenframework.z8.server.base.json.parser.JsonObject)
		//	return ((org.zenframework.z8.server.base.json.parser.JsonObject) o).get();
		//if (o instanceof org.zenframework.z8.server.base.json.parser.JsonArray)
		//	return ((org.zenframework.z8.server.base.json.parser.JsonArray) o).get();

		return o;
	}
}
