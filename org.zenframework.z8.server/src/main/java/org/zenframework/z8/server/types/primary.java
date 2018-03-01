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

	public string string() {
		return z8_toString();
	}
}
