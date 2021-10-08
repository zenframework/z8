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
	public SecuredObjectAccess.CLASS<SecuredObjectAccess> soa = new SecuredObjectAccess.CLASS<SecuredObjectAccess>(this);

	public Link.CLASS<Link> roleId = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> soaId = new Link.CLASS<Link>(this);

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
		soaId.get(IClass.Constructor1).operatorAssign(soa);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(role);
		objects.add(soa);

		objects.add(roleId);
		objects.add(soaId);

		objects.add(value);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		role.setIndex("role");
		soa.setIndex("soa");

		roleId.setName(fieldNames.Role);
		roleId.setIndex("roleId");

		soaId.setName(fieldNames.SecuredObjectAccess);
		soaId.setIndex("securedObjectAccessId");

		value.setName(fieldNames.Value);
		value.setIndex("value");
		value.setDisplayName(displayNames.Value);
	}

	private void addStaticRecord(guid id, guid roleId, guid securedObjectAccessId, boolean value) {
		LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
		record.put(this.roleId.get(), roleId);
		record.put(this.soaId.get(), securedObjectAccessId);
		record.put(this.value.get(), new bool(value));
		record.put(lock.get(), RecordLock.Full);
		addRecord(id, record);
	}

	@Override
	public void initStaticRecords() {
		addStaticRecord(new guid("2B9B1579-BDA2-457D-8ACA-660A4591D2F3"), Role.Administrator, Access.TableRead, true);
		addStaticRecord(new guid("B3E84DAC-CB0D-40A4-9A46-53957F8D8131"), Role.Administrator, Access.TableWrite, true);
		addStaticRecord(new guid("8EE7ECEA-7578-40C7-A628-D045BD00083F"), Role.Administrator, Access.TableCreate, true);
		addStaticRecord(new guid("50A38EEF-C662-4EE2-BE5F-8A07D4F17371"), Role.Administrator, Access.TableCopy, true);
		addStaticRecord(new guid("450E96CA-AE90-4224-B5CA-A13B1419390A"), Role.Administrator, Access.TableDestroy, true);

		addStaticRecord(new guid("19B2B413-DD3A-42FE-A0FF-9B9A076D9F0C"), Role.Administrator, Access.FieldRead, true);
		addStaticRecord(new guid("24035128-7430-403E-8B28-023CAE21CF55"), Role.Administrator, Access.FieldWrite, true);

		addStaticRecord(new guid("FB86A054-FB6C-4893-974A-9BB9A7439AD6"), Role.Administrator, Access.RequestExecute, true);

		addStaticRecord(new guid("4083E37C-7ADB-414F-8003-B16CC1704FA1"), Role.User, Access.TableRead, true);
		addStaticRecord(new guid("E4F62430-04EB-4371-ADBE-CCDA0932D29E"), Role.User, Access.FieldRead, true);
	}

	@Override
	public void onUpdateAction(guid recordId) {
		super.onUpdateAction(recordId);

		if(readRecord(recordId, Arrays.asList(roleId.get())))
			Roles.notifyRoleChange(roleId.get().guid());
	}
}
