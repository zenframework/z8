package org.zenframework.z8.server.db.generator;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlToken;

public class Index {
	public static final String Type = "type";
	public static final String Trigram = "trigram";

	private final String name;
	private final String tableName;
	private final SqlToken token;
	private final Map<String, Object> properties = new HashMap<String, Object>();
	private final boolean unique;

	public Index(String tableName, String name, boolean unique, SqlToken token) {
		this.tableName = tableName;
		this.name = name;
		this.unique = unique;
		this.token = token;
	}

	public Index(String tableName, IField field, SqlToken token, int index) {
		this(tableName, (field.unique() ? "Unq" : "Idx") + index + tableName, field.unique(), token);
		properties.put(Type, field.type());
		properties.put(Trigram, ((Field) field).trigram());
	}

	public SqlToken getToken() {
		return token;
	}

	public String getName() {
		return name;
	}

	public String getTableName() {
		return tableName;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public boolean isUnique() {
		return unique;
	}

	@SuppressWarnings("unchecked")
	public <T> T property(String name) {
		return (T) properties.get(name);
	}

	public FieldType fieldType() {
		return this.<FieldType>property(Type);
	}

	public boolean trigram() {
		return this.<Boolean>property(Trigram);
	}

	@Override
	public String toString() {
		return name + (unique ? " [U]" : "");
	}
}
