package org.zenframework.z8.server.db.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.Requests;
import org.zenframework.z8.server.base.table.system.RoleFieldAccess;
import org.zenframework.z8.server.base.table.system.RoleRequestAccess;
import org.zenframework.z8.server.base.table.system.RoleSecuredObjectAccess;
import org.zenframework.z8.server.base.table.system.RoleTableAccess;
import org.zenframework.z8.server.base.table.system.Roles;
import org.zenframework.z8.server.base.table.system.SecuredObjectAccess;
import org.zenframework.z8.server.base.table.system.SecuredObjects;
import org.zenframework.z8.server.base.table.system.Tables;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.security.Access;
import org.zenframework.z8.server.security.Role;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class AccessRightsGenerator {
	private Tables tables = new Tables.CLASS<Tables>().get();
	private Fields fields = new Fields.CLASS<Fields>().get();
	private Requests requests = new Requests.CLASS<Requests>().get();

	private RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();
	private RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>().get();
	private RoleRequestAccess rra = new RoleRequestAccess.CLASS<RoleRequestAccess>().get();

	private SecuredObjects securedObjects = SecuredObjects.newInstance();
	private SecuredObjectAccess soa = SecuredObjectAccess.newInstance();
	private RoleSecuredObjectAccess rsoa = new RoleSecuredObjectAccess.CLASS<RoleSecuredObjectAccess>().get();

	private Collection<guid> tableKeys = new HashSet<guid>();
	private Collection<guid> requestKeys = new HashSet<guid>();

	private Collection<Role> roles = null;

	@SuppressWarnings("unused")
	private ILogger logger;

	public AccessRightsGenerator(ILogger logger) {
		this.logger = logger;
		tableKeys.addAll(Runtime.instance().tableKeys());
		requestKeys.addAll(Runtime.instance().requestKeys());
	}

	public void run() {
		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();

			clearTables();
			createTables();

			clearRequests();
			createRequests();

			clearSecuredObjects();
			createSecuredObjects();

			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		} finally {
			connection.release();
		}
	}

	private void clearTables() {
		tables.read(Arrays.asList(tables.primaryKey()), tables.primaryKey().notInVector(tableKeys));

		while(tables.next()) {
			guid tableId = tables.recordId();
			rta.destroy(new Equ(rta.tableId.get(), tableId));
			rfa.destroy(new Equ(rfa.field.get().tableId.get(), tableId));
			fields.destroy(new Equ(fields.tableId.get(), tableId));
			tables.destroy(tableId);
		}
	}

	private void createTables() {
		tables.read(Arrays.asList(tables.primaryKey()), tables.primaryKey().inVector(tableKeys));
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

			createFields(table);

			for(Role role : getRoles()) {
				Access access = role.getAccess();

				rta.roleId.get().set(role.getId());
				rta.tableId.get().set(key);

				rta.read.get().set(new bool(access.get(Access.TableRead)));
				rta.write.get().set(new bool(access.get(Access.TableWrite)));
				rta.create.get().set(new bool(access.get(Access.TableCreate)));
				rta.copy.get().set(new bool(access.get(Access.TableCopy)));
				rta.destroy.get().set(new bool(access.get(Access.TableDestroy)));
				rta.create();
			}
		}
	}

	private void clearRequests() {
		requests.read(Arrays.asList(requests.primaryKey()), requests.primaryKey().notInVector(requestKeys));

		while(requests.next()) {
			guid requestId = requests.recordId();
			rra.destroy(new Equ(rra.requestId.get(), requestId));
			requests.destroy(requestId);
		}
	}

	private void createRequests() {
		requests.read(Arrays.asList(requests.primaryKey()), requests.primaryKey().inVector(requestKeys));
		while(requests.next()) {
			guid requestId = requests.recordId();
			OBJECT request = Runtime.instance().getRequestByKey(requestId).newInstance();
			setRequestProperties(request);
			requests.update(requestId);
			requestKeys.remove(requestId);
		}

		for(guid key : requestKeys) {
			OBJECT request = Runtime.instance().getRequestByKey(key).newInstance();
			setRequestProperties(request);
			requests.create(key);

			for(Role role : getRoles()) {
				Access access = role.getAccess();

				rra.roleId.get().set(role.getId());
				rra.requestId.get().set(key);

				rra.execute.get().set(new bool(access.get(Access.RequestExecute)));
				rra.create();
			}
		}
	}

	private Collection<guid> securedObjectKeys() {
		Collection<guid> keys = new ArrayList<guid>();

		for(Map<IField, primary> record : securedObjects.getStaticRecords())
			keys.add((guid)record.get(securedObjects.primaryKey()));

		return keys;
	}

	private Collection<guid> securedObjectAccessKeys() {
		Collection<guid> keys = new ArrayList<guid>();

		for(Map<IField, primary> record : soa.getStaticRecords())
			keys.add((guid)record.get(soa.primaryKey()));

		return keys;
	}

	private void clearSecuredObjects() {
		securedObjects.read(Arrays.asList(securedObjects.primaryKey()), securedObjects.primaryKey().notInVector(securedObjectKeys()));

		while(securedObjects.next()) {
			guid securedObjectId = securedObjects.recordId();
			rsoa.destroy(new Equ(rsoa.securedObjectAccess.get().securedObjectId.get(), securedObjectId));
			soa.destroy(new Equ(soa.securedObjectId.get(), securedObjectId));
			securedObjects.destroy(securedObjectId);
		}

		soa.read(Arrays.asList(soa.primaryKey()), soa.primaryKey().notInVector(securedObjectAccessKeys()));

		while(soa.next()) {
			guid securedObjectAccessId = soa.recordId();
			rsoa.destroy(new Equ(rsoa.securedObjectAccessId.get(), securedObjectAccessId));
			soa.destroy(securedObjectAccessId);
		}
	}

	private void createSecuredObjects() {
		Collection<guid> soaKeys = securedObjectAccessKeys();

		Map<guid, Collection<guid>> roleToSoaKeys = new HashMap<guid, Collection<guid>>();

		Field roleId = rsoa.roleId.get();
		Field soaId = rsoa.securedObjectAccessId.get();
		rsoa.read(Arrays.asList(roleId, soaId));

		while(rsoa.next()) {
			Collection<guid> keys = roleToSoaKeys.get(roleId.guid());

			if(keys == null) {
				keys = new ArrayList<guid>();
				roleToSoaKeys.put(roleId.guid(), keys);
			}

			keys.add(soaId.guid());
		}

		for(guid key : soaKeys) {
			for(Role role : getRoles()) {
				Collection<guid> keys = roleToSoaKeys.get(role.getId());

				if(keys != null && keys.contains(key))
					continue;

				rsoa.roleId.get().set(role.getId());
				rsoa.securedObjectAccessId.get().set(key);
				rsoa.value.get().set(new bool(role.getAccess().get(key)));
				rsoa.create();
			}
		}
	}

	private void updateFields(Table table) {
		Map<guid, Field> fieldsMap = table.getFieldsMap();
		Collection<guid> fieldKeys = new HashSet<guid>(fieldsMap.keySet());

		fields.read(Arrays.asList(fields.primaryKey()),
				new And(new Equ(fields.tableId.get(), table.key()), fields.primaryKey().notInVector(fieldKeys)));

		while(fields.next()) {
			guid fieldId = fields.recordId();
			rfa.destroy(new Equ(rfa.fieldId.get(), fieldId));
			fields.destroy(fieldId);
		}

		createFields(table);
	}

	private void createFields(Table table) {
		Map<guid, Field> fieldsMap = table.getFieldsMap();
		Collection<guid> fieldKeys = new HashSet<guid>(fieldsMap.keySet());

		fields.read(Arrays.asList(fields.primaryKey()), fields.primaryKey().inVector(fieldKeys));
		while(fields.next()) {
			guid fieldId = fields.recordId();
			setFieldProperties(fieldsMap.get(fieldId), table.key());
			fields.update(fieldId);

			fieldKeys.remove(fieldId);
		}

		for(guid key : fieldKeys) {
			setFieldProperties(fieldsMap.get(key), table.key());
			fields.create(key);
			for(Role role : getRoles()) {
				Access access = role.getAccess();

				rfa.roleId.get().set(role.getId());
				rfa.fieldId.get().set(key);

				rfa.read.get().set(new bool(access.get(Access.FieldRead)));
				rfa.write.get().set(new bool(access.get(Access.FieldWrite)));
				rfa.create();
			}
		}
	}

	private void setTableProperties(Table table) {
		tables.classId.get().set(new string(table.classId()));
		tables.name.get().set(new string(table.name()));
		tables.displayName.get().set(new string(table.displayName()));
		tables.description.get().set(new string(table.description()));
		tables.lock.get().set(RecordLock.Full);
	}

	private void setFieldProperties(Field field, guid tableKey) {
		fields.tableId.get().set(tableKey);
		fields.name.get().set(new string(field.name()));
		fields.displayName.get().set(new string(displayName(field)));
		fields.description.get().set(new string(field.description()));
		fields.type.get().set(new string(getFieldType(field)));
		fields.position.get().set(new integer(field.ordinal()));
		fields.lock.get().set(RecordLock.Full);
	}

	private void setRequestProperties(OBJECT request) {
		requests.classId.get().set(request.classId());
		requests.name.get().set(new string(request.displayName()));
		requests.lock.get().set(RecordLock.Full);
	}

	private Collection<Role> getRoles() {
		if(roles == null)
			roles = Roles.get().values();
		return roles;
	}

	private String displayName(Field field) {
		String name = field.displayName();

		if(name != null && !name.isEmpty())
			return name;

		name = field.name();
		return name == null || name.isEmpty() ? field.getClass().getSimpleName() : name;
	}

	private String getFieldType(Field field) {
		FieldType type = field.type();
		return type.toString() + (type == FieldType.String ? "(" + field.length.get() + ")" : "");
	}
}
