package org.zenframework.z8.server.db.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ForeignKey {
	private final String referenceTable;
	private final String referenceField;
	private final String table;
	private final String field;
	private final String name;
	private String oldName;

	public ForeignKey(String referenceTable, String referenceField, String table, String field, String name) {
		this.referenceTable = referenceTable;
		this.referenceField = referenceField;
		this.table = table;
		this.field = field;
		this.name = name;
		this.oldName = name;
	}

	public String getReferenceTable() {
		return referenceTable;
	}

	public String getReferenceField() {
		return referenceField;
	}

	public String getTable() {
		return table;
	}

	public String getField() {
		return field;
	}

	public String getName() {
		return name;
	}

	public String getOldName() {
		return oldName;
	}

	public ForeignKey setOldName(String oldName) {
		this.oldName = oldName;
		return this;
	}

	@Override
	public int hashCode() {
		return (referenceTable + '|' + referenceField + '|' + table + '|' + field + '|').hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ForeignKey))
			return false;
		ForeignKey fk = (ForeignKey) o;
		return referenceTable.equals(fk.referenceTable) && referenceField.equals(fk.referenceField) && table.equals(fk.table) && field.equals(fk.field);
	}

	@Override
	public String toString() {
		return (oldName != null ? "[" + oldName + '/' + name + ']' : name)
				+ "('" + table + "'.'" + field + "' -> '" + referenceTable + "'.'" + referenceField + "')";
	}

	public static Map<ForeignKey, String> toNamesMap(Collection<ForeignKey> foreignKeys) {
		Map<ForeignKey, String> names = new HashMap<ForeignKey, String>();

		for (ForeignKey foreignKey : foreignKeys)
			names.put(foreignKey, foreignKey.getName());

		return names;
	}
}
