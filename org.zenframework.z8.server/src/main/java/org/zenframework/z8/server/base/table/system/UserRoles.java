package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.security.Role;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

public class UserRoles extends Table {
	final static public String TableName = "SystemUserRoles";

	final static public guid System = new guid("DAD7CCDE-51D8-4B24-884D-3F7C2C13F9EC");
	final static public guid Administrator = new guid("8D05DB1E-1A17-4E19-949C-F2894920261E");

	static public class fieldNames {
		public final static String User = "User";
		public final static String Role = "Role";
	}

	static public class strings {
		public final static String Title = "UserRoles.title";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
	}

	public static class CLASS<T extends UserRoles> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(UserRoles.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new UserRoles(container);
		}
	}

	public Users.CLASS<? extends Users> user = new Users.CLASS<Users>(this);
	public Roles.CLASS<? extends Roles> role = new Roles.CLASS<Roles>(this);

	public Link.CLASS<Link> userId = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> roleId = new Link.CLASS<Link>(this);

	public UserRoles() {
		this(null);
	}

	public UserRoles(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		userId.get(IClass.Constructor1).operatorAssign(user);
		roleId.get(IClass.Constructor1).operatorAssign(role);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(userId);
		objects.add(roleId);

		objects.add(user);
		objects.add(role);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		user.setIndex("user");
		role.setIndex("role");

		userId.setName(fieldNames.User);
		userId.setIndex("userId");

		roleId.setName(fieldNames.Role);
		roleId.setIndex("roleId");

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());
	}

	@Override
	public void initStaticRecords() {
		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(userId.get(), BuiltinUsers.Administrator.guid());
			record.put(roleId.get(), Role.Administrator);
			record.put(lock.get(), RecordLock.Full);
			addRecord(Administrator, record);
		}

		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(userId.get(), BuiltinUsers.System.guid());
			record.put(roleId.get(), Role.Administrator);
			record.put(lock.get(), RecordLock.Full);
			addRecord(System, record);
		}
	}

	@Override
	public void afterCreate(guid recordId) {
		super.afterCreate(recordId);
		Users.notifyUserChange(userId.get().guid());
	}

	@Override
	public void beforeDestroy(guid recordId) {
		super.beforeDestroy(recordId);

		if(recordId.equals(Administrator) || recordId.equals(System))
			throw new exception("Builtin user's roles can not be removed.");

		Field user = this.userId.get();
		saveState();
		if(readRecord(recordId, Arrays.asList(user)))
			Users.notifyUserChange(user.guid());
		restoreState();
	}

	static public Collection<Role> get(guid userId) {
		Map<guid, Role> rolesMap = Roles.get();

		UserRoles userRoles = new UserRoles.CLASS<UserRoles>().get();
		Field user = userRoles.userId.get();
		Field role = userRoles.roleId.get();

		userRoles.read(Arrays.asList(role), new Equ(user, userId));

		Collection<Role> roles = new HashSet<Role>();

		while(userRoles.next())
			roles.add(rolesMap.get(role.guid()));

		return roles;
	}
}
