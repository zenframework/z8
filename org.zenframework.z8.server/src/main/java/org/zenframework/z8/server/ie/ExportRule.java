package org.zenframework.z8.server.ie;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;

@SuppressWarnings("all")
public class ExportRule extends OBJECT {
	public static class CLASS<T extends ExportRule> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(ExportRule.class);
		}

		public Object newObject(IObject container) {
			return new ExportRule(container);
		}
	}

	public Table.CLASS<? extends Table> table;
	public RCollection fields;
	public guid recordId;
	public ImportPolicy policy;
	public bool ignoreThisRule;

	static {
		staticConstructor();
	}

	public static void staticConstructor() {
	}

	public ExportRule(IObject container) {
		super(container);
		table = new Table.CLASS<Table>(this);
		fields = new RCollection();
		recordId = new guid();
	}

	public void constructor1() {
	}

	public void initMembers() {
		super.initMembers();
	}

	public void constructor2() {
		super.constructor2();
		table.setIndex("table");
		ignoreThisRule = new bool(false);
	}

	public boolean shouldBeIgnored() {
		return ignoreThisRule.get();
	}

	public guid getRecordId() {
		return z8_getRecordId();
	}

	public Table getTable() {
		return z8_getTable().get();
	}

	public RCollection<Field.CLASS<? extends Field>> getFields() {
		return z8_getFields();
	}

	public ImportPolicy getPolicy() {
		return z8_getPolicy();
	}

	static public ExportRule.CLASS<? extends ExportRule> z8_create(Table.CLASS<? extends Table> table,
			ImportPolicy policy) {
		return z8_create(guid.Null, table, policy);
	}

	static public ExportRule.CLASS<? extends ExportRule> z8_create(guid recordId, Table.CLASS<? extends Table> table, ImportPolicy policy) {
		ExportRule.CLASS<? extends ExportRule> rule = new ExportRule.CLASS<ExportRule>(null);
		rule.get().recordId = recordId;
		rule.get().table = table;
		rule.get().policy = policy;
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
		return z8_create(recordId, table, new RCollection(new Object[] { field }), policy);
	}

	static public ExportRule.CLASS<? extends ExportRule> z8_create(guid recordId, Table.CLASS<? extends Table> table, RCollection<Field.CLASS<? extends Field>> fields) {
		return z8_create(recordId, table, fields, ImportPolicy.OVERRIDE);
	}

	static public ExportRule.CLASS<? extends ExportRule> z8_create(guid recordId, Table.CLASS<? extends Table> table, RCollection<Field.CLASS<? extends Field>> fields, ImportPolicy policy) {
		String tableName = table.get().name();
		for (Field.CLASS<? extends Field> field : fields) {
			Table fieldTable = (Table) field.get().getOwner();
			if (!tableName.equals(fieldTable.name()))
				throw new RuntimeException(
						"Field \"" + field.get().name() + "\" doesn't belong to table \"" + tableName + "\"");
		}

		ExportRule.CLASS<? extends ExportRule> rule = new ExportRule.CLASS<ExportRule>(null);
		rule.get().table = table;
		rule.get().fields = fields;
		rule.get().policy = policy;
		rule.get().recordId = recordId;
		rule.get().ignoreThisRule = ((bool) fields.z8_isEmpty());
		return rule;
	}

	public guid z8_getRecordId() {
		return recordId;
	}

	public Table.CLASS<? extends Table> z8_getTable() {
		return table;
	}

	public RCollection z8_getFields() {
		return fields;
	}

	public ImportPolicy z8_getPolicy() {
		return policy;
	}
}
