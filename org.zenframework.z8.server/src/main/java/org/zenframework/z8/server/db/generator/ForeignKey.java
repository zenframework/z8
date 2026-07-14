package org.zenframework.z8.server.db.generator;

public class ForeignKey {
	private final String referenceTable;
	private final String referenceField;
	private final String table;
	private final String field;
	private final String name;

	public ForeignKey(String referenceTable, String referenceField, String table, String field, String name) {
		this.referenceTable = referenceTable;
		this.referenceField = referenceField;
		this.table = table;
		this.field = field;
		this.name = name;
	}

	public ForeignKey(String table, IForeignKey fk, int index) {
		this(fk.getReferencedTable().name(), fk.getReferer().name(), table, fk.getFieldDescriptor().name(), "FK" + index + "_" + table);
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
		return name + "('" + table + "'.'" + field + "' -> '" + referenceTable + "'.'" + referenceField + "')";
	}
}
