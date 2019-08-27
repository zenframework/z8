package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;

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
import org.zenframework.z8.server.security.Access;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.security.IAccess;
import org.zenframework.z8.server.security.IRole;
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

	public Users.CLASS<? extends Users> users = new Users.CLASS<Users>(this);
	public Roles.CLASS<? extends Roles> roles = new Roles.CLASS<Roles>(this);

	public Link.CLASS<Link> user = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> role = new Link.CLASS<Link>(this);

	public UserRoles() {
		this(null);
	}

	public UserRoles(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		user.get(IClass.Constructor1).operatorAssign(users);
		role.get(IClass.Constructor1).operatorAssign(roles);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(user);
		objects.add(role);

		objects.add(users);
		objects.add(roles);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		users.setIndex("users");
		roles.setIndex("roles");

		user.setName(fieldNames.User);
		user.setIndex("user");

		role.setName(fieldNames.Role);
		role.setIndex("role");

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());
	}

	@Override
	public void initStaticRecords() {
		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(user.get(), BuiltinUsers.Administrator.guid());
			record.put(role.get(), Role.Administrator);
			record.put(lock.get(), RecordLock.Full);
			addRecord(Administrator, record);
		}

		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(user.get(), BuiltinUsers.System.guid());
			record.put(role.get(), Role.Administrator);
			record.put(lock.get(), RecordLock.Full);
			addRecord(System, record);
		}
	}

	@Override
	public void afterCreate(guid recordId) {
		super.afterCreate(recordId);
		Users.notifyUserChange(user.get().guid());
	}

	@Override
	public void beforeDestroy(guid recordId) {
		super.beforeDestroy(recordId);

		if(recordId.equals(Administrator) || recordId.equals(System))
			throw new exception("Builtin user's roles can not be removed.");

		Field user = this.user.get();
		saveState();
		if(readRecord(recordId, Arrays.asList(user)))
			Users.notifyUserChange(user.guid());
		restoreState();
	}

	public Collection<IRole> get(guid userId) {
		Field user = this.user.get();
		Field role = this.role.get();
		Field read = this.roles.get().read.get();
		Field write = this.roles.get().write.get();
		Field create = this.roles.get().create.get();
		Field copy = this.roles.get().copy.get();
		Field destroy = this.roles.get().destroy.get();
		Field execute = this.roles.get().execute.get();

		Collection<Field> fields = Arrays.asList(role, read, write, create, copy, destroy, execute);
		read(fields, new Equ(user, userId));

		Collection<IRole> roles = new HashSet<IRole>();

		while(next()) {
			IAccess access = new Access();
			access.setRead(read.bool().get());
			access.setWrite(write.bool().get());
			access.setCreate(create.bool().get());
			access.setCopy(copy.bool().get());
			access.setDestroy(destroy.bool().get());
			access.setExecute(execute.bool().get());

			roles.add(new Role(role.guid(), access));
		}

		return roles;
	}
}
