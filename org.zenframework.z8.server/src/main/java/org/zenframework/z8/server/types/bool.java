package org.zenframework.z8.server.types;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.sql.sql_bool;

public final class bool extends primary {

	static private final long serialVersionUID = 4231676741592349038L;

	private boolean value = false;

	static public class strings {
		public final static String True = "bool.true";
		public final static String False = "bool.false";
	}

	static public String trueString = Resources.get(strings.True);
	static public String falseString = Resources.get(strings.False);

	public static bool False = new bool(false);
	public static bool True = new bool(true);

	public bool() {
	}

	public bool(bool x) {
		set(x.value);
	}

	public bool(integer x) {
		set((x.get() == 1));
	}

	public bool(String x) {
		if(x == null || x.equalsIgnoreCase("0") || x.equalsIgnoreCase("false") || x.equalsIgnoreCase("no") || x.equalsIgnoreCase("n") || x.equalsIgnoreCase(falseString)) {
			set(false);
		} else if(x.equalsIgnoreCase("1") || x.equalsIgnoreCase("true") || x.equalsIgnoreCase("yes") || x.equalsIgnoreCase("y") || x.equalsIgnoreCase(trueString)) {
			set(true);
		} else {
			throw new RuntimeException("Invalid value for bool: '" + x + "'");
		}
	}

	public bool(boolean x) {
		set(x);
	}

	public String toNumber() {
		return (value ? "1" : "0");
	}

	@Override
	public String toString() {
		return Boolean.toString(value);
	}

	public boolean get() {
		return value;
	}

	private void set(boolean value) {
		this.value = value;
	}

		@Override
	public FieldType type() {
		return FieldType.Boolean;
	}

	@Override
	public String toDbConstant(DatabaseVendor dbtype) {
		switch(dbtype) {
		case SqlServer:
			return toNumber();
		default:
			return toNumber();
		}
	}

	@Override
	public int hashCode() {
		return Boolean.valueOf(value).hashCode();
	}

	@Override
	public boolean equals(Object b) {
		if(b instanceof bool) {
			return value == ((bool)b).value;
		}
		return false;
	}

	static public bool z8_parse(string string) {
		return new bool(string.get());
	}

	public sql_bool sql_bool() {
		return new sql_bool(this);
	}

	public bool operatorNot() {
		return new bool(!value);
	}

	public bool operatorAnd(bool x) {
		return new bool(value && x.value);
	}

	public bool operatorOr(bool x) {
		return new bool(value || x.value);
	}

	public bool operatorEqu(bool x) {
		return new bool(value == x.value);
	}

	public bool operatorNotEqu(bool x) {
		return new bool(value != x.value);
	}

	@Override
	public int compareTo(primary primary) {
		if(primary instanceof bool) {
			bool bool = (bool)primary;
			return value != bool.value ? (value ? 1 : -1) : 0;
		}
		return -1;
	}
}
