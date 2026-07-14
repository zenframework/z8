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

	public final String name;
	public final String tableName;
	public final List<String> fields = new ArrayList<String>();
	public final Map<String, Object> properties = new HashMap<String, Object>();
	public final boolean unique;

	private boolean exists;

	public Index(String tableName, String fieldName, String name, boolean unique, boolean exists) {
		this.tableName = tableName;
		this.fields.add(fieldName);
		this.name = name;
		this.unique = unique;
		this.exists = exists;
	}

	public Index(String tableName, IField field, int index, boolean exists) {
		this(tableName, field.name(), (field.unique() ? "Unq" : "Idx") + index + tableName, field.unique(), exists);
		properties.put(Type, field.type());
		properties.put(Trigram, ((Field) field).trigram());
		properties.put(CaseInsensitive, ((Field) field).caseInsensitive());
	}

	public void addField(String field) {
		fields.add(field);
	}

	public boolean isExists() {
		return exists;
	}

	public void setExists(boolean exists) {
		this.exists = exists;
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
