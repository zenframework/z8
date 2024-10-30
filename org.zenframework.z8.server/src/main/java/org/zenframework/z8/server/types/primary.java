package org.zenframework.z8.server.types;

import java.io.Serializable;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;

public abstract class primary implements Comparable<primary>, Serializable {

	private static final long serialVersionUID = -6139111122281366413L;

	public FieldType type() {
		throw new UnsupportedOperationException();
	}

	public String toDbConstant(DatabaseVendor dbtype) {
		throw new UnsupportedOperationException();
	}

	public integer z8_hashCode() {
		return new integer(hashCode());
	}

	public string z8_toString() {
		return new string(toString());
	}
	
	public string z8_getString() {
		if (this instanceof string)
			return (string) this;
		throw new IllegalStateException("Cannot convert to string");
	}
	
	public integer z8_getInt() {
		if (this instanceof integer)
			return (integer) this;
		throw new IllegalStateException("Cannot convert to int");
	}

	public string string() {
		return z8_toString();
	}
	
	public Object getValue() {
		if (this instanceof binary)
			return ((binary) this).get();
		if (this instanceof bool)
			return ((bool) this).get();
		if (this instanceof date)
			return ((date) this).get();
		if (this instanceof datespan)
			return ((datespan) this).get();
		if (this instanceof decimal)
			return ((decimal) this).get();
		if (this instanceof file)
			return ((file) this).get();
		if (this instanceof geometry)
			return ((geometry) this).get();
		if (this instanceof guid)
			return ((guid) this).get();
		if (this instanceof integer)
			return ((integer) this).get();
		if (this instanceof string)
			return ((string) this).get();
		throw new IllegalStateException();
	}

}
