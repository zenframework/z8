package org.zenframework.z8.server.ie;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.guid;

public class ExportRule extends OBJECT {
	public static class CLASS<T extends ExportRule> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(ExportRule.class);
		}

		public Object newObject(IObject container) {
			return new ExportRule(container);
		}
	}

	private Table.CLASS<? extends Table> table = new Table.CLASS<Table>(this);
	private RCollection<Field.CLASS<? extends Field>> fields = null;
	private guid recordId = guid.Null;
	private ImportPolicy policy = ImportPolicy.OVERRIDE;

	public ExportRule(IObject container) {
		super(container);
	}

	public void setTable(Table.CLASS<? extends Table> table) {
		this.table = table;
	}

	public void setRecordId(guid recordId) {
		this.recordId = recordId;
	}

	public void setPolicy(ImportPolicy policy) {
		this.policy = policy;
	}

	public void setFields(RCollection<Field.CLASS<? extends Field>> fields) {
		this.fields = fields;
	}

	public guid getRecordId() {
		return recordId;
	}

	public String getTableName() {
		return table.get().name();
	}

	public RCollection<Field.CLASS<? extends Field>> getFields() {
		return fields;
	}

	public ImportPolicy getPolicy() {
		return policy;
	}

	static public ExportRule.CLASS<? extends ExportRule> z8_create(Table.CLASS<? extends Table> table, ImportPolicy policy) {
		return z8_create(guid.Null, table, policy);
	}

	static public ExportRule.CLASS<? extends ExportRule> z8_create(guid recordId, Table.CLASS<? extends Table> table, ImportPolicy policy) {
		ExportRule.CLASS<? extends ExportRule> rule = new ExportRule.CLASS<ExportRule>();

		ExportRule exportRule = rule.get();
		exportRule.setRecordId(recordId);
		exportRule.setTable(table);
		exportRule.setPolicy(policy);

		return rule;
	}

	static public ExportRule.CLASS<? extends ExportRule> z8_create(Table.CLASS<? extends Table> table, Field.CLASS<? extends Field> field, ImportPolicy policy) {
		return z8_create(guid.Null, table, field, policy);
	}

	static public ExportRule.CLASS<? extends ExportRule> z8_create(Table.CLASS<? extends Table> table, RCollection<Field.CLASS<? extends Field>> fields, ImportPolicy policy) {
		return z8_create(guid.Null, table, fields, policy);
	}

	static public ExportRule.CLASS<? extends ExportRule> z8_create(guid recordId, Table.CLASS<? extends Table> table,
			Field.CLASS<? extends Field> field) {
		return z8_create(recordId, table, field, ImportPolicy.OVERRIDE);
	}

	static public ExportRule.CLASS<? extends ExportRule> z8_create(guid recordId, Table.CLASS<? extends Table> table, Field.CLASS<? extends Field> field, ImportPolicy policy) {
		return z8_create(recordId, table, new RCollection<Field.CLASS<? extends Field>>(new Object[] { field }), policy);
	}

	static public ExportRule.CLASS<? extends ExportRule> z8_create(guid recordId, Table.CLASS<? extends Table> table, RCollection<Field.CLASS<? extends Field>> fields) {
		return z8_create(recordId, table, fields, ImportPolicy.OVERRIDE);
	}

	static public ExportRule.CLASS<? extends ExportRule> z8_create(guid recordId, Table.CLASS<? extends Table> table, RCollection<Field.CLASS<? extends Field>> fields, ImportPolicy policy) {
		ExportRule.CLASS<? extends ExportRule> rule = new ExportRule.CLASS<ExportRule>();
		ExportRule exportRule = rule.get();
		exportRule.setRecordId(recordId);
		exportRule.setTable(table);
		exportRule.setPolicy(policy);
		exportRule.setFields(fields);
		exportRule.checkFields();
		return rule;
	}

	public guid z8_getRecordId() {
		return getRecordId();
	}

	public Table.CLASS<? extends Table> z8_getTable() {
		return table;
	}

	public RCollection<Field.CLASS<? extends Field>> z8_getFields() {
		return getFields();
	}

	public ImportPolicy z8_getPolicy() {
		return getPolicy();
	}

	private void checkFields() {
		if (fields == null)
			return;

		Table t = table.get();
		for (Field.CLASS<? extends Field> field : fields) {
			if (t != (Table) field.get().getOwner())
				throw new RuntimeException("Field " + field.id() + " (\"" + field.get().name() + "\") doesn't belong to table " + table.id() + " (\"" + t.name() + "\")");
		}
	}
}
