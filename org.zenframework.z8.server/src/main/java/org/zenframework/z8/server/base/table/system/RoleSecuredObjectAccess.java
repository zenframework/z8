package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.Access;
import org.zenframework.z8.server.security.Role;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

public class RoleSecuredObjectAccess extends Table {
	final static public String TableName = "System Role Secured Object Access";

	static public class fieldNames {
		public final static String Role = "Role";
		public final static String SecuredObjectAccess = "Secured Object Access";
		public final static String Value = "Value";
	}

	static public class strings {
		public final static String Title = "RoleSecuredObjectAccess.title";
		public final static String Value = "RoleSecuredObjectAccess.value";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Value = Resources.get(strings.Value);
	}

	public static class CLASS<T extends RoleSecuredObjectAccess> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(RoleSecuredObjectAccess.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new RoleSecuredObjectAccess(container);
		}
	}

	public Roles.CLASS<Roles> role = new Roles.CLASS<Roles>(this);
	public SecuredObjectAccess.CLASS<SecuredObjectAccess> securedObjectAccess = new SecuredObjectAccess.CLASS<SecuredObjectAccess>(this);

	public Link.CLASS<Link> roleId = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> securedObjectAccessId = new Link.CLASS<Link>(this);

	public BoolField.CLASS<BoolField> value = new BoolField.CLASS<BoolField>(this);

	public RoleSecuredObjectAccess() {
		this(null);
	}

	public RoleSecuredObjectAccess(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		roleId.get(IClass.Constructor1).operatorAssign(role);
		securedObjectAccessId.get(IClass.Constructor1).operatorAssign(securedObjectAccess);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(role);
		objects.add(securedObjectAccess);

		objects.add(roleId);
		objects.add(securedObjectAccessId);

		objects.add(value);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		role.setIndex("role");
		securedObjectAccess.setIndex("securedObjectAccess");

		roleId.setName(fieldNames.Role);
		roleId.setIndex("roleId");

		securedObjectAccessId.setName(fieldNames.SecuredObjectAccess);
		securedObjectAccessId.setIndex("securedObjectAccessId");

		value.setName(fieldNames.Value);
		value.setIndex("value");
		value.setDisplayName(displayNames.Value);
	}

	private void addStaticRecord(guid roleId, guid securedObjectAccessId, boolean value) {
		LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
		record.put(this.roleId.get(), roleId);
		record.put(this.securedObjectAccessId.get(), securedObjectAccessId);
		record.put(this.value.get(), new bool(value));
		record.put(lock.get(), RecordLock.Full);
		addRecord(guid.create(), record);
	}

	@Override
	public void initStaticRecords() {
		addStaticRecord(Role.Administrator, Access.TableRead, true);
		addStaticRecord(Role.Administrator, Access.TableWrite, true);
		addStaticRecord(Role.Administrator, Access.TableCreate, true);
		addStaticRecord(Role.Administrator, Access.TableCopy, true);
		addStaticRecord(Role.Administrator, Access.TableDestroy, true);

		addStaticRecord(Role.Administrator, Access.FieldRead, true);
		addStaticRecord(Role.Administrator, Access.FieldWrite, true);

		addStaticRecord(Role.Administrator, Access.RequestExecute, true);

		addStaticRecord(Role.User, Access.TableRead, true);
		addStaticRecord(Role.User, Access.FieldRead, true);
	}

	@Override
	public void onUpdateAction(guid recordId) {
		super.onUpdateAction(recordId);

		if(readRecord(recordId, Arrays.asList(roleId.get())))
			Roles.notifyRoleChange(roleId.get().guid());
	}
}
