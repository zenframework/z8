package org.zenframework.z8.server.db.generator;

import java.util.Collection;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.Tables;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class TableInfoGenerator {
	private Collection<Table.CLASS<Table>> tableClasses;
	@SuppressWarnings("unused")
	private ILogger logger;

	public TableInfoGenerator(Collection<Table.CLASS<Table>> tableClasses, ILogger logger) {
		this.tableClasses = tableClasses;
		this.logger = logger;
	}

	public void run() {
		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			writeTableInfo();
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void writeTableInfo() {
		Tables tables = new Tables.CLASS<Tables>().get();
		Fields fields = new Fields.CLASS<Fields>().get();

		for(Table.CLASS<Table> tableClass : tableClasses) {
			String classId = tableClass.classId();
			guid tableId;

			tables.read(new Equ(tables.id.get(), classId));

			tables.id.get().set(new string(classId));
			tables.name.get().set(new string(tableClass.name()));
			tables.displayName.get().set(new string(tableClass.displayName()));
			tables.description.get().set(new string(tableClass.description()));

			if(tables.next()) {
				tableId = tables.recordId();
				tables.update(tableId);
			} else
				tableId = tables.create();

			int position = 1;
			Collection<Field.CLASS<Field>> fieldClasses = (Collection)tableClass.get().primaryFields();
			for(Field.CLASS<Field> fieldClass : fieldClasses) {
				fields.read(new And(new Equ(fields.table.get(), tableId), new Equ(fields.name.get(), fieldClass.name())));

				fields.table.get().set(tableId);
				fields.name.get().set(new string(fieldClass.name()));
				fields.displayName.get().set(new string(fieldClass.displayName()));
				fields.description.get().set(new string(fieldClass.description()));
				fields.type.get().set(new string(getFieldType(fieldClass.get())));
				fields.position.get().set(new integer(position));

				if(fields.next())
					fields.update(fields.recordId());
				else
					fields.create();

				position++;
			}
		}
	}

	private String getFieldType(Field field) {
		FieldType type = field.type();
		return type.toString() + (type == FieldType.String ? "(" + field.length.get() + ")" : "");
	}
}
