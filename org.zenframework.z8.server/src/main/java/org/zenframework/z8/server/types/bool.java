package org.zenframework.z8.server.types;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.exceptions.MaskParseException;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.sql.sql_bool;

public final class bool extends primary {

	private static final long serialVersionUID = 4231676741592349038L;

	private boolean m_value = false;

	public static class strings {
		public final static String True = "bool.true";
		public final static String False = "bool.false";
	}

	private static String trueString = null;
	private static String falseString = null;

	static private void loadResources() {
		if (trueString == null) {
			trueString = Resources.get(strings.True);
			falseString = Resources.get(strings.False);
		}
	}

	public bool() {}

	public bool(bool x) {
		set(x.m_value);
	}

	public bool(integer x) {
		set((x.get() == 1));
	}

	public bool(String x) {
		loadResources();
		if (x == null || x.equalsIgnoreCase("0") || x.equalsIgnoreCase("false") || x.equalsIgnoreCase("no")
				|| x.equalsIgnoreCase("n") || x.equalsIgnoreCase(falseString)) {
			set(false);
		} else if (x.equalsIgnoreCase("1") || x.equalsIgnoreCase("true") || x.equalsIgnoreCase("yes")
				|| x.equalsIgnoreCase("y") || x.equalsIgnoreCase(trueString)) {
			set(true);
		} else {
			throw new MaskParseException(new string(x), "bool");
		}
	}

	public bool(boolean x) {
		set(x);
	}

	@Override
	public bool defaultValue() {
		return new bool();
	}

	public String toNumber() {
		return (m_value ? "1" : "0");
	}

	@Override
	public String toString() {
		return Boolean.toString(m_value);
	}

	public boolean get() {
		return m_value;
	}

	public void set(boolean bool) {
		m_value = bool;
	}

	public void set(bool value) {
		set(value.get());
	}

	@Override
	public FieldType type() {
		return FieldType.Boolean;
	}

	@Override
	public String toDbConstant(DatabaseVendor dbtype) {
		switch (dbtype) {
		case SqlServer:
			return toNumber();
		default:
			return toNumber();
		}
	}

	@Override
	public int hashCode() {
		return Boolean.valueOf(m_value).hashCode();
	}

	@Override
	public boolean equals(Object b) {
		if (b instanceof bool) {
			return m_value == ((bool) b).m_value;
		}
		return false;
	}

	static public bool z8_parse(string string) {
		return new bool(string.get());
	}

	public sql_bool sql_bool() {
		return new sql_bool(this);
	}

	public void operatorAssign(bool x) {
		set(x);
	}

	public bool operatorNot() {
		return new bool(!m_value);
	}

	public bool operatorAnd(bool x) {
		return new bool(m_value && x.m_value);
	}

	public bool operatorOr(bool x) {
		return new bool(m_value || x.m_value);
	}

	public bool operatorEqu(bool x) {
		return new bool(m_value == x.m_value);
	}

	public bool operatorNotEqu(bool x) {
		return new bool(m_value != x.m_value);
	}
}
