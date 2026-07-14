package org.zenframework.z8.server.db.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.FieldType;

public class Index {
	public static final String Type = "type";
	public static final String Trigram = "trigram";
	public static final String CaseInsensitive = "caseInsensitive";

	private final String name;
	private final String tableName;
	private final List<String> fields = new ArrayList<String>();
	private final Map<String, Object> properties = new HashMap<String, Object>();
	private final boolean unique;

	public Index(String tableName, String fieldName, String name, boolean unique) {
		this.tableName = tableName;
		this.fields.add(fieldName);
		this.name = name;
		this.unique = unique;
	}

	public Index(String tableName, IField field, int index) {
		this(tableName, field.name(), (field.unique() ? "Unq" : "Idx") + index + tableName, field.unique());
		properties.put(Type, field.type());
		properties.put(Trigram, ((Field) field).trigram());
		properties.put(CaseInsensitive, ((Field) field).caseInsensitive());
	}

	public void addField(String field) {
		fields.add(field);
	}

	public String getField() {
		return fields.get(0);
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

	public boolean caseInsensitive() {
		return this.<Boolean>property(CaseInsensitive);
	}

	@Override
	public String toString() {
		return name + '(' + (unique ? "[U] " : "") + StringUtils.join(fields, ", ") + ')';
	}
}
