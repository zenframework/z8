package org.zenframework.z8.server.db.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TableDescription {
	private String name;
	private Map<String, Column> columns = new HashMap<String, Column>();
	private Map<String, Index> indexes = new HashMap<String, Index>();
	private Map<String, ForeignKey> foreignKeys = new HashMap<String, ForeignKey>();
	private Map<String, ForeignKey> referers = new HashMap<String, ForeignKey>();
	private PrimaryKey primaryKey;
	private int controlSum = 0;

	public TableDescription(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Map<String, Column> getColumns() {
		return columns;
	}

	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public Map<String, Index> getIndexes() {
		return indexes;
	}

	public Index getIndex(String name) {
		return indexes.get(name);
	}

	public Collection<ForeignKey> getForeignKeys() {
		return foreignKeys.values();
	}

	public Collection<ForeignKey> getReferers() {
		return referers.values();
	}

	public void addField(Column field) {
		columns.put(field.name, field);
	}

	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void addIndex(Index index) {
		indexes.put(index.name, index);
	}

	public void addForeignKey(ForeignKey foreignKey) {
		foreignKeys.put(foreignKey.getName(), foreignKey);
	}

	public void addReferer(ForeignKey foreignKey) {
		referers.put(foreignKey.getName(), foreignKey);
	}

	public int controlSum() {
		if (controlSum == 0)
			controlSum = calculateControlSum();
		return controlSum;
	}

	public String controlData() {
		StringBuilder str = new StringBuilder(1024);

		for (Column column : columns.values()) {
			if (str.length() > 0)
				str.append(", ");
			str.append(column.controlData());
		}

		return str.toString();
	}

	protected int calculateControlSum() {
		int result = 0;

		for (Column column : columns.values())
			result += column.controlSum();

		// Static records skipped

		return Math.abs(Integer.toString(result).hashCode());
	}
}
