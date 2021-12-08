package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.Access;
import org.zenframework.z8.server.security.Role;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Roles extends Table {
	final static public String TableName = "SystemRoles";

	static public class fieldNames {
		public final static String Role = "Role";
		public final static String Table = "Table";
		public final static String Read = "Read";
		public final static String Write = "Write";
		public final static String Create = "Create";
		public final static String Copy = "Copy";
		public final static String Destroy = "Destroy";
		public final static String Execute = "Execute";
	}

	static public class strings {
		public final static String Title = "Roles.title";
		public final static String Name = "Roles.name";
		public final static String Read = "Roles.read";
		public final static String Write = "Roles.write";
		public final static String Create = "Roles.create";
		public final static String Copy = "Roles.copy";
		public final static String Destroy = "Roles.destroy";
		public final static String Execute = "Roles.execute";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
		public final static String Read = Resources.get(strings.Read);
		public final static String Write = Resources.get(strings.Write);
		public final static String Create = Resources.get(strings.Create);
		public final static String Copy = Resources.get(strings.Copy);
		public final static String Destroy = Resources.get(strings.Destroy);
		public final static String Execute = Resources.get(strings.Execute);
	}

	static public guid User = Role.User;
	static public guid Guest = Role.Guest;
	static public guid Administrator = Role.Administrator;

	public static class CLASS<T extends Roles> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Roles.class);
			setName(Roles.TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Roles(container);
		}
	}

	static public void notifyRoleChange(guid role) {
		try {
			ServerConfig.authorityCenter().roleChanged(role, ApplicationServer.getSchema());
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public Roles(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(50);
	}

	@Override
	public void initStaticRecords() {
		initStaticRecord(Role.displayNames.Administrator, Role.administrator());
		initStaticRecord(Role.displayNames.User, Role.user());
		initStaticRecord(Role.displayNames.Guest, Role.guest());
	}

	private void initStaticRecord(String displayName, Role role) {
		LinkedHashMap<IField, primary> values = new LinkedHashMap<IField, primary>();
		values.put(name.get(), new string(displayName));
		values.put(description.get(), new string(displayName));
		values.put(lock.get(), RecordLock.Destroy);
		addRecord(role.getId(), values);
	}

	@Override
	public void z8_onCopy(guid recordId) {
		super.z8_onCopy(recordId);
		lock.get().set(RecordLock.None);
	}

	@Override
	public void z8_afterCreate(guid recordId) {
		super.z8_afterCreate(recordId);

		RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();

		for(guid table : Runtime.instance().tableKeys()) {
			rta.roleId.get().set(recordId);
			rta.tableId.get().set(table);
			rta.create();
		}

		Fields fields = new Fields.CLASS<Fields>().get();
		RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>().get();

		fields.read(Arrays.asList(fields.primaryKey()));

		while(fields.next()) {
			rfa.roleId.get().set(recordId);
			rfa.fieldId.get().set(fields.recordId());
			rfa.create();
		}

		Requests requests = new Requests.CLASS<Requests>().get();
		RoleRequestAccess rra = new RoleRequestAccess.CLASS<RoleRequestAccess>().get();

		requests.read(Arrays.asList(requests.primaryKey()));

		while(requests.next()) {
			rra.roleId.get().set(recordId);
			rra.requestId.get().set(requests.recordId());
			rra.create();
		}

		SecuredObjectAccess soa = new SecuredObjectAccess.CLASS<SecuredObjectAccess>().get();
		RoleSecuredObjectAccess rsoa = new RoleSecuredObjectAccess.CLASS<RoleSecuredObjectAccess>().get();

		soa.read(Arrays.asList(soa.primaryKey()));

		while(soa.next()) {
			rsoa.roleId.get().set(recordId);
			rsoa.soaId.get().set(soa.recordId());
			rsoa.create();
		}

	}

	@Override
	public void z8_beforeDestroy(guid recordId) {
		super.z8_beforeDestroy(recordId);

		if(Role.Administrator.equals(recordId) || Role.User.equals(recordId) ||
				Role.Guest.equals(recordId))
			throw new exception("Builtin roles can not be deleted");

		RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>().get();
		rfa.destroy(new Equ(rfa.roleId.get(), recordId));

		RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();
		rta.destroy(new Equ(rta.roleId.get(), recordId));

		RoleRequestAccess rra = new RoleRequestAccess.CLASS<RoleRequestAccess>().get();
		rra.destroy(new Equ(rra.roleId.get(), recordId));

		RoleSecuredObjectAccess rsoa = new RoleSecuredObjectAccess.CLASS<RoleSecuredObjectAccess>().get();
		rsoa.destroy(new Equ(rsoa.roleId.get(), recordId));
	}

	static public Map<guid, Role> get() {
		Roles roles = new Roles.CLASS<Roles>().get();

		Map<guid, Role> rolesMap = new HashMap<guid, Role>();

		Field name = roles.name.get();

		roles.read(Arrays.asList(name));

		while(roles.next()) {
			guid id = roles.recordId();
			rolesMap.put(id, new Role(id, name.string().get(), new Access()));
		}

		RoleSecuredObjectAccess rsoa = new RoleSecuredObjectAccess.CLASS<RoleSecuredObjectAccess>().get();

		Field roleId = rsoa.roleId.get();
		Field value = rsoa.value.get();
		Field securedObjectAccessId = rsoa.soaId.get();

		rsoa.read(Arrays.asList(roleId, securedObjectAccessId, value));
		while(rsoa.next()) {
			if(!value.bool().get())
				continue;
			rolesMap.get(roleId.guid()).getAccess().set(securedObjectAccessId.guid(), true);
		}

		return rolesMap;
	}
}
