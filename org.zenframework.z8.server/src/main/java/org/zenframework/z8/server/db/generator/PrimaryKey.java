package org.zenframework.z8.server.db.generator;

import java.util.ArrayList;
import java.util.Collection;

public class PrimaryKey {
	private final String name;
	private final String tableName;
	private final Collection<String> fields = new ArrayList<String>();

	PrimaryKey(String name, String tableName, String fieldName) {
		this.name = name;
		this.tableName = tableName;
		this.fields.add(fieldName);
	}

	public void addField(String field) {
		fields.add(field);
	}

	public Collection<String> getFields() {
		return fields;
	}

	public String getName() {
		return name;
	}

	public String getTableName() {
		return tableName;
	}

	@Override
	public String toString() {
		String s = name;

		for (String col : fields)
			s += " " + col;

		return s;
	}
}
