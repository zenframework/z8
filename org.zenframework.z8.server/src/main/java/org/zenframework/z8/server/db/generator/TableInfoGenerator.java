package org.zenframework.z8.server.db.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.RoleFieldAccess;
import org.zenframework.z8.server.base.table.system.RoleTableAccess;
import org.zenframework.z8.server.base.table.system.Roles;
import org.zenframework.z8.server.base.table.system.Tables;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.UnaryNot;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class TableInfoGenerator {
	private Collection<guid> roles = null;

	Tables tables = new Tables.CLASS<Tables>().get();
	Fields fields = new Fields.CLASS<Fields>().get();
	RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();
	RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>().get();
	Collection<guid> tableKeys = new HashSet<guid>();

	@SuppressWarnings("unused")
	private ILogger logger;

	public TableInfoGenerator(ILogger logger) {
		this.logger = logger;
		tableKeys.addAll(Runtime.instance().tableKeys());
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

	private void writeTableInfo() {
		tables.read(Arrays.asList(tables.primaryKey()), new UnaryNot(new InVector(tables.primaryKey(), tableKeys)));

		while(tables.next()) {
			guid tableId = tables.recordId();
			rta.destroy(new Equ(rta.table.get(), tableId));
			rfa.destroy(new Equ(rfa.fields.get().table.get(), tableId));
			fields.destroy(new Equ(fields.table.get(), tableId));
			tables.destroy(tableId);
		}

		createTables();
	}

	private void createTables() {
		tables.read(Arrays.asList(tables.primaryKey()), new InVector(tables.primaryKey(), tableKeys));
		while(tables.next()) {
			guid tableId = tables.recordId();
			Table table = Runtime.instance().getTableByKey(tableId).newInstance();
			setTableProperties(table);
			tables.update(tableId);

			updateFields(table);
			tableKeys.remove(tableId);
		}

		for(guid key : tableKeys) {
			Table table = Runtime.instance().getTableByKey(key).newInstance();
			setTableProperties(table);
			tables.create(key);

			updateFields(table);

			for(guid role : getRoles()) {
				rta.role.get().set(role);
				rta.table.get().set(key);
				rta.create();
			}
		}
	}

	private void updateFields(Table table) {
		Map<guid, Field.CLASS<? extends Field>> fieldsMap = table.getFieldsMap();
		Collection<guid> fieldKeys = fieldsMap.keySet();

		fields.read(Arrays.asList(fields.primaryKey()), new InVector(fields.primaryKey(), fieldKeys));
		while(fields.next()) {
			guid fieldId = fields.recordId();
			setFieldProperties(fieldsMap.get(fieldId), table.key());
			fields.update(fieldId);

			fieldKeys.remove(fieldId);
		}

		for(guid key : fieldKeys) {
			setFieldProperties(fieldsMap.get(key), table.key());
			fields.create(key);
			for(guid role : getRoles()) {
				rfa.role.get().set(role);
				rfa.field.get().set(key);
				rfa.create();
			}
		}
	}

	private void setTableProperties(Table table) {
		tables.id.get().set(new string(table.classId()));
		tables.name.get().set(new string(table.name()));
		tables.displayName.get().set(new string(table.displayName()));
		tables.description.get().set(new string(table.description()));
	}

	private void setFieldProperties(Field.CLASS<? extends Field> field, guid tableKey) {
		fields.table.get().set(tableKey);
		fields.name.get().set(new string(field.name()));
		fields.displayName.get().set(new string(displayName(field)));
		fields.description.get().set(new string(field.description()));
		fields.type.get().set(new string(getFieldType(field.get())));
		fields.position.get().set(new integer(field.ordinal()));
	}

	private Collection<guid> getRoles() {
		if(roles != null)
			return roles;

		roles = new ArrayList<guid>();

		Roles rolesTable = new Roles.CLASS<Roles>(null).get();
		rolesTable.read(Arrays.asList(rolesTable.primaryKey()));

		while(rolesTable.next())
			roles.add(rolesTable.recordId());

		return roles;
	}

	private String displayName(Field.CLASS<? extends Field> field) {
		String name = field.displayName();

		if(name != null && !name.isEmpty())
			return name;

		name = field.name();
		return name == null || name.isEmpty() ? field.getJavaClass().getSimpleName() : name;
	}

	private String getFieldType(Field field) {
		FieldType type = field.type();
		return type.toString() + (type == FieldType.String ? "(" + field.length.get() + ")" : "");
	}
}
