package org.zenframework.z8.server.db.generator;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;

public class Column {
	public String name;
	public String type;
	public int size;
	public int scale;
	public boolean nullable;
	public String defaultValue;

	public Column(String name, String type, int size, int scale, boolean nullable, String defaultValue) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.scale = scale;
		this.nullable = nullable;
		this.defaultValue = defaultValue != null ? defaultValue : "";
	}

	@Override
	public String toString() {
		return "name " + name + " type " + type + " size " + Integer.toString(size) + " scale " + Integer.toString(scale) + " nullable " + Boolean.toString(nullable) + " default " + defaultValue;
	}

	public FieldType fieldType() {
		return FieldType.parse(type, size, scale);
	}

	public int controlSum() {
		return Math.abs((name + " " + fieldType().vendorSqlType(DatabaseVendor.Postgres, size, scale)).hashCode());
	}

	public String controlData() {
		return name + " " + fieldType().vendorSqlType(DatabaseVendor.Postgres, size, scale);
	}
}
