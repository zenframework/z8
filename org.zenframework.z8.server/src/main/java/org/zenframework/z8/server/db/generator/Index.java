package org.zenframework.z8.server.db.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlToken;

public class Index {
	public static class Metadata {
		public String tableName;
		public String name;
		public String value;
		public boolean unique;
		public String method;
		public boolean trigram;
		public String definition;
	}

	public static final String IndexPrefix = "Idx";
	public static final String UniqueIndexPrefix = "Unq";
	public static final String Type = "type";
	public static final String Trigram = "trigram";
	public static final String Method = "method";

	private String name;
	private final String tableName;
	private final SqlToken token;
	private final Map<String, Object> properties = new HashMap<String, Object>();
	private final boolean unique;
	private String comparisonValue;
	private String definition;

	public Index(String tableName, String name, boolean unique, SqlToken token) {
		this.tableName = tableName;
		this.name = name;
		this.unique = unique;
		this.token = token;
	}

	public Index(String tableName, IField field, SqlToken token, int index) {
		this(tableName, createName(tableName, field.unique(), index), field.unique(), token);
		properties.put(Type, field.type());
		properties.put(Trigram, field.type() == FieldType.String && ((Field) field).trigram());
	}

	public SqlToken getToken() {
		return token;
	}

	public String getName() {
		return name;
	}

	public Index setName(String name) {
		this.name = name;
		return this;
	}

	public static String createName(String tableName, boolean unique, int number) {
		return (unique ? UniqueIndexPrefix : IndexPrefix) + number + tableName;
	}

	public String getComparisonValue() {
		return comparisonValue;
	}

	public Index setComparisonValue(String value) {
		this.comparisonValue = value;
		return this;
	}

	public String getDefinition() {
		return definition;
	}

	public Index setDefinition(String definition) {
		this.definition = definition;
		return this;
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
		return Boolean.TRUE.equals(property(Trigram));
	}

	public String getMethod() {
		return property(Method);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tableName, comparisonValue, unique, getMethod(), trigram());
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (!(object instanceof Index))
			return false;
		Index index = (Index)object;
		return unique == index.unique && tableName.equals(index.tableName) && Objects.equals(comparisonValue, index.comparisonValue)
				&& Objects.equals(getMethod(), index.getMethod()) && trigram() == index.trigram();
	}

	@Override
	public String toString() {
		return name + (unique ? " [U]" : "");
	}
}
