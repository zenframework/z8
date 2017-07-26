package org.zenframework.z8.server.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.table.system.RoleFieldAccess;
import org.zenframework.z8.server.base.table.system.RoleRequestAccess;
import org.zenframework.z8.server.base.table.system.RoleTableAccess;
import org.zenframework.z8.server.base.table.system.SystemTools;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.system.UserRoles;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.NotEqu;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.db.sql.functions.string.EqualsIgnoreCase;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.exceptions.InvalidVersionException;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

public class User implements IUser {
	static private final long serialVersionUID = -4955893424674255525L;

	private guid id;

	private String login;
	private String password;

	private String firstName;
	private String middleName;
	private String lastName;

	private Collection<IRole> roles;
	private IPrivileges privileges;

	private String description;
	private String phone;
	private String email;
	private boolean banned;

	private String settings;

	private Collection<Entry> entries = new ArrayList<Entry>();
	private RLinkedHashMap<string, primary> parameters = new RLinkedHashMap<string, primary>();

	static private User system = null;
	static private User site = null;

	static public IUser system() {
		if(system != null)
			return system;

		guid id = BuiltinUsers.System.guid();
		String login = BuiltinUsers.displayNames.SystemName;
		String description = BuiltinUsers.displayNames.SystemDescription;

		system = new User();

		system.id = id;
		system.login = login;
		system.password = "";
		system.firstName = "";
		system.middleName = "";
		system.lastName = "";
		system.description = description;
		system.banned = false;

		system.settings = "";
		system.phone = "";
		system.email = "";
		system.roles = new HashSet<IRole>(Arrays.asList(Role.administrator()));

		system.addSystemTools();

		return system;
	}

	static public IUser site() {
		if(site != null)
			return site;

		guid id = BuiltinUsers.Site.guid();
		String login = BuiltinUsers.displayNames.SiteName;

		site = new User();

		site.id = id;
		site.login = login;
		site.password = "";
		site.banned = true;
		site.roles = new HashSet<IRole>(Arrays.asList(Role.site()));
		site.privileges = new Privileges(Access.site());
		return site;
	}

	public User() {
	}

	@Override
	public guid id() {
		return id;
	}

	@Override
	public String login() {
		return login;
	}

	@Override
	public String password() {
		return password;
	}

	@Override
	public String firstName() {
		return firstName;
	}

	@Override
	public String middleName() {
		return middleName;
	}

	@Override
	public String lastName() {
		return lastName;
	}

	@Override
	public String name() {
		String name = firstName;
		return name + (name.isEmpty() ? "" : " ") + lastName;
	}

	@Override
	public String fullName() {
		String name = firstName;
		name += (name.isEmpty() ? "" : " ") + middleName;
		return name + (name.isEmpty() ? "" : " ") + lastName;
	}

	@Override
	public Collection<IRole> roles() {
		return roles;
	}

	@Override
	public IPrivileges privileges() {
		return privileges;
	}

	@Override
	public boolean isAdministrator() {
		if(roles != null) {
			for(IRole role : roles) {
				if(role.id().equals(Role.Administrator))
					return true;
			}
		}
		return id.equals(Users.Administrator) || id.equals(Users.System);
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public String phone() {
		return phone;
	}

	@Override
	public String email() {
		return email;
	}

	@Override
	public boolean banned() {
		return banned;
	}

	@Override
	public Collection<Entry> entries() {
		return entries;
	}

	@Override
	public void setEntries(Collection<Entry> entries) {
		this.entries = entries;
	}

	@Override
	public Map<string, primary> parameters() {
		return parameters;
	}

	static public IUser read(guid userId) {
		return userId.isNull() ? null : read((primary)userId);
	}

	static private IUser read(String login) {
		return read(new string(login));
	}

	static private IUser read(primary loginOrId) {
		User user = new User();

		boolean isLatestVersion = ServerConfig.isLatestVersion();

		if(loginOrId instanceof string)
			user.readInfo((string)loginOrId, !isLatestVersion);
		else
			user.readInfo((guid)loginOrId, !isLatestVersion);

		if(isLatestVersion) {
			user.loadRoles();
			user.loadEntries();
		} else if(user.isAdministrator())
			user.privileges = new Privileges(Access.administrator());
		else
			throw new InvalidVersionException();

		if(user.isAdministrator())
			user.addSystemTools();

		ConnectionManager.release();

		return user;
	}

	static public IUser load(String login, String password) {
		if(!ServerConfig.isSystemInstalled())
			return User.system();

		IUser user = read(login);

		if(password == null || !password.equals(user.password()) || user.banned() || user.id().equals(Users.Site))
			throw new AccessDeniedException();

		return user;
	}

	private void addSystemTools() {
		for(Entry entry : entries) {
			if(entry.id().equals(SystemTools.Id))
				return;
		}

		SystemTools.CLASS<SystemTools> systemTools = new SystemTools.CLASS<SystemTools>();
		entries.add(new Entry(systemTools.key(), systemTools.classId(), Resources.get(SystemTools.strings.Title)));
	}

	private void readInfo(guid id, boolean shortInfo) {
		Users users = Users.newInstance();
		SqlToken where = new Equ(users.recordId.get(), id);
		readInfo(users, where, shortInfo);
	}

	private void readInfo(string login, boolean shortInfo) {
		Users users = Users.newInstance();
		SqlToken where = new EqualsIgnoreCase(users.name.get(), login);
		readInfo(users, where, shortInfo);
	}

	private void readInfo(Users users, SqlToken where, boolean shortInfo) {
		Collection<Field> fields = new ArrayList<Field>();
		fields.add(users.recordId.get());
		fields.add(users.name.get());
		fields.add(users.password.get());

		if(!shortInfo) {
			fields.add(users.banned.get());
			fields.add(users.firstName.get());
			fields.add(users.middleName.get());
			fields.add(users.lastName.get());
			fields.add(users.settings.get());
			fields.add(users.phone.get());
			fields.add(users.email.get());
		}

		users.read(fields, where);

		if(users.next()) {
			this.id = users.recordId.get().guid();
			this.login = users.name.get().string().get();
			this.password = users.password.get().string().get();
			this.banned = users.banned.get().bool().get();
			this.firstName = users.firstName.get().string().get();
			this.middleName = users.middleName.get().string().get();
			this.lastName = users.lastName.get().string().get();
			this.settings = users.settings.get().string().get();
			this.phone = users.phone.get().string().get();
			this.email = users.email.get().string().get();
		} else {
			throw new AccessDeniedException();
		}

		if(shortInfo)
			return;

		try {
			if(!users.getExtraParameters(this, parameters))
				throw new AccessDeniedException();
		} catch(Throwable e) {
			Trace.logError(e);
			if(!BuiltinUsers.Administrator.guid().equals(id) && !BuiltinUsers.System.guid().equals(id))
				throw new AccessDeniedException();
		}
	}

	private Collection<guid> getRoles() {
		Collection<guid> result = new ArrayList<guid>();

		for(IRole role : roles)
			result.add(role.id());

		return result;
	}

	private IAccess defaultAccess() {
		IAccess access = new Access();

		for(IRole role : roles)
			access.apply(role.access());

		return access;
	}

	private void loadRoles() {
		UserRoles userRoles = new UserRoles.CLASS<UserRoles>().get();
		roles = userRoles.get(id);

		IAccess defaultAccess = defaultAccess();
		privileges = new Privileges(defaultAccess);

		loadTablesAccess(privileges, defaultAccess);
		loadFieldsAccess(privileges, defaultAccess);
		loadRequestAccess(privileges, defaultAccess);

/*
		Trace.logEvent(privileges);
*/
	}

	private void loadTablesAccess(IPrivileges privileges, IAccess defaultAccess) {
		RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();
		Field tableId = rta.table.get();
		Field read = rta.read.get();
		Field write = rta.write.get();
		Field create = rta.create.get();
		Field copy = rta.copy.get();
		Field destroy = rta.destroy.get();

		Collection<Field> groups = new ArrayList<Field>(Arrays.asList(tableId));
		Collection<Field> fields = Arrays.asList(tableId, read, write, create, copy, destroy);

		SqlToken where = new InVector(rta.role.get(), getRoles());

		SqlToken notRead = new NotEqu(read, new sql_bool(defaultAccess.read()));
		SqlToken notWrite = new NotEqu(write, new sql_bool(defaultAccess.write()));
		SqlToken notCreate = new NotEqu(create, new sql_bool(defaultAccess.create()));
		SqlToken notCopy = new NotEqu(copy, new sql_bool(defaultAccess.copy()));
		SqlToken notDestroy = new NotEqu(destroy, new sql_bool(defaultAccess.destroy()));
		SqlToken having = new Group(Or.fromList(Arrays.asList(notRead, notWrite, notCreate, notCopy, notDestroy)));

		rta.group(fields, groups, where, having);
		while(rta.next()) {
			IAccess access = new Access();
			access.setRead(read.bool().get());
			access.setWrite(write.bool().get());
			access.setCreate(create.bool().get());
			access.setCopy(copy.bool().get());
			access.setDestroy(destroy.bool().get());
			privileges.setTableAccess(tableId.guid(), access);
		}
	}

	private void loadFieldsAccess(IPrivileges privileges, IAccess defaultAccess) {
		RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>().get();
		Field tableId = rfa.fields.get().table.get();
		Field fieldId = rfa.field.get();
		Field read = rfa.read.get();
		Field write = rfa.write.get();

		Collection<Field> groups = new ArrayList<Field>(Arrays.asList(fieldId));
		Collection<Field> fields = Arrays.asList(tableId, read, write);

		SqlToken where = new InVector(rfa.role.get(), getRoles());

		SqlToken notRead = new NotEqu(read, new sql_bool(defaultAccess.read()));
		SqlToken notWrite = new NotEqu(write, new sql_bool(defaultAccess.write()));
		SqlToken having = new Group(Or.fromList(Arrays.asList(notRead, notWrite)));

		rfa.group(fields, groups, where, having);
		while(rfa.next()) {
			guid table = tableId.guid();

			IAccess tableAccess = privileges.getTableAccess(table);
			if(!tableAccess.read() || !tableAccess.write())
				continue;

			boolean readable = read.bool().get();
			boolean writable = write.bool().get();

			if(!readable || !writable) {
				IAccess access = new Access();
				access.setRead(readable);
				access.setWrite(writable);
				privileges.setFieldAccess(fieldId.guid(), access);
			}
		}
	}

	private void loadRequestAccess(IPrivileges privileges, IAccess defaultAccess) {
		RoleRequestAccess rra = new RoleRequestAccess.CLASS<RoleRequestAccess>().get();
		Field requestId = rra.request.get();
		Field execute = rra.execute.get();

		Collection<Field> groups = new ArrayList<Field>(Arrays.asList(requestId));
		Collection<Field> fields = Arrays.asList(requestId, execute);

		SqlToken where = new InVector(rra.role.get(), getRoles());

		SqlToken notExecute = new NotEqu(execute, new sql_bool(defaultAccess.execute()));
		SqlToken having = new Group(notExecute);

		rra.group(fields, groups, where, having);
		while(rra.next()) {
			IAccess access = new Access();
			access.setExecute(execute.bool().get());
			privileges.setRequestAccess(requestId.guid(), access);
		}
	}

	private void loadEntries() {
		UserEntries userEntries = new UserEntries.CLASS<UserEntries>().get();

		Field entry = userEntries.entry.get();
		Field id = userEntries.entries.get().classId.get();
		Field name = userEntries.entries.get().name.get();

		Collection<Field> fields = Arrays.asList(entry, id, name);

		Field position = userEntries.position.get();
		Collection<Field> sortFields = Arrays.asList(position);

		SqlToken where = new Equ(userEntries.user.get(), this.id);

		userEntries.read(fields, sortFields, where);

		List<Entry> entries = new ArrayList<Entry>();

		while(userEntries.next())
			entries.add(new Entry(entry.guid(), id.string().get(), name.string().get()));

		setEntries(entries);
	}

	@Override
	public String settings() {
		return settings;
	}

	@Override
	public void setSettings(String settings) {
		this.settings = settings;

		Users.saveSettings(id, settings);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		RmiIO.writeLong(out, serialVersionUID);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream(32 * NumericUtils.Kilobyte);
		ObjectOutputStream objects = new ObjectOutputStream(bytes);

		RmiIO.writeGuid(objects, id);

		RmiIO.writeString(objects, login);
		RmiIO.writeString(objects, password);

		RmiIO.writeString(objects, firstName);
		RmiIO.writeString(objects, middleName);
		RmiIO.writeString(objects, lastName);

		RmiIO.writeString(objects, description);
		RmiIO.writeString(objects, phone);
		RmiIO.writeString(objects, email);

		RmiIO.writeBoolean(objects, banned);

		RmiIO.writeString(objects, settings);

		objects.writeObject(roles);
		objects.writeObject(privileges);
		objects.writeObject(entries);
		objects.writeObject(parameters);

		objects.close();

		RmiIO.writeBytes(out, IOUtils.zip(bytes.toByteArray()));
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		ByteArrayInputStream bytes = new ByteArrayInputStream(IOUtils.unzip(RmiIO.readBytes(in)));
		ObjectInputStream objects = new ObjectInputStream(bytes);

		id = RmiIO.readGuid(objects);

		login = RmiIO.readString(objects);
		password = RmiIO.readString(objects);

		firstName = RmiIO.readString(objects);
		middleName = RmiIO.readString(objects);
		lastName = RmiIO.readString(objects);

		description = RmiIO.readString(objects);
		phone = RmiIO.readString(objects);
		email = RmiIO.readString(objects);

		banned = RmiIO.readBoolean(objects);

		settings = RmiIO.readString(objects);

		roles = (Collection)objects.readObject();
		privileges = (IPrivileges)objects.readObject();
		entries = (Collection)objects.readObject();
		parameters = (RLinkedHashMap)objects.readObject();

		objects.close();
	}
}
