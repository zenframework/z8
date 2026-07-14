package org.zenframework.z8.server.db.generator;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.dialect.DatabaseDialect;

public class Column {
	private final String name;
	private final String type;
	private final int size;
	private final int scale;
	private final boolean nullable;
	private final String defaultValue;

	private int controlSum = 0;

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

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getSize() {
		return size;
	}

	public int getScale() {
		return scale;
	}

	public boolean isNullable() {
		return nullable;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public FieldType fieldType() {
		return FieldType.parse(type, size, scale);
	}

	public int controlSum() {
		if (controlSum == 0)
			controlSum = calculateControlSum();
		return controlSum;
	}

	public String controlData() {
		return name + " " + DatabaseDialect.Default.formatSqlType(fieldType(), size, scale);
	}

	protected int calculateControlSum() {
		return Math.abs(controlData().hashCode());
	}
}
