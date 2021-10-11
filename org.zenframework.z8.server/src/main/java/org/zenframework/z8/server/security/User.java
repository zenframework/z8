package org.zenframework.z8.server.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.zenframework.z8.server.base.table.system.RoleFieldAccess;
import org.zenframework.z8.server.base.table.system.RoleRequestAccess;
import org.zenframework.z8.server.base.table.system.RoleTableAccess;
import org.zenframework.z8.server.base.table.system.SystemTools;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.system.UserRoles;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.crypto.Digest;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.NotEqu;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.functions.string.EqualsIgnoreCase;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.exceptions.InvalidVersionException;
import org.zenframework.z8.server.exceptions.UserNotFoundException;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

public class User implements RmiSerializable, Serializable {
	static private final long serialVersionUID = -4955893424674255525L;

	private guid id;

	private String login;
	private String password;

	private String firstName;
	private String middleName;
	private String lastName;

	private Collection<Role> roles;
	private Privileges privileges;

	private String description;
	private String phone;
	private String email;
	private boolean banned;
	private boolean changePassword;

	private String settings;
	private Database database;

	private Collection<Entry> entries = new ArrayList<Entry>();
	private RLinkedHashMap<string, primary> parameters = new RLinkedHashMap<string, primary>();

	static public User system(String scheme) {
		return system(Database.get(scheme));
	}

	static public User system(Database database) {
		guid id = BuiltinUsers.System.guid();
		String login = BuiltinUsers.displayNames.SystemName;

		User system = new User(database);

		system.id = id;
		system.login = login;
		system.password = "";
		system.firstName = "";
		system.middleName = "";
		system.lastName = "";
		system.banned = false;
		system.changePassword = false;

		system.settings = "";
		system.phone = "";
		system.email = "";
		system.roles = Arrays.asList(Role.administrator());
		system.privileges = new Privileges(Access.administrator());

		system.addSystemTools();

		return system;
	}

	public User() {
	}

	public User(Database database) {
		this.database = database;
	}

	public guid getId() {
		return id;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getName() {
		String name = firstName;
		return name + (name.isEmpty() ? "" : " ") + lastName;
	}

	public String getFullName() {
		String name = firstName;
		name += (name.isEmpty() ? "" : " ") + middleName;
		return name + (name.isEmpty() ? "" : " ") + lastName;
	}

	public Collection<Role> getRoles() {
		if(roles == null)
			roles = UserRoles.get(id);
		return roles;
	}

	public Privileges getPrivileges() {
		return privileges;
	}

	public Access getAccess() {
		Access access = new Access();

		for(Role role : getRoles())
			access = access.or(role.getAccess());

		return access;
	}

	public boolean isBuiltinAdministrator() {
		return id.equals(Users.Administrator) || id.equals(Users.System);
	}

	public boolean isAdministrator() {
		if(roles != null) {
			for(Role role : roles) {
				if(role.getId().equals(Role.Administrator))
					return true;
			}
		}
		return isBuiltinAdministrator();
	}

	public String getDescription() {
		return description;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public boolean getBanned() {
		return banned;
	}

	public boolean getChangePassword() {
		return changePassword;
	}

	public Collection<Entry> getEntries() {
		return entries;
	}

	public void setEntries(Collection<Entry> entries) {
		this.entries = entries;
	}

	public Map<string, primary> getParameters() {
		return parameters;
	}

	static public User read(guid userId) {
		return userId.isNull() ? null : read(new LoginParameters(userId));
	}

	static private User read(LoginParameters loginParameters) {
		Database database = ApplicationServer.getDatabase();
		User user = new User(database);

		boolean isLatestVersion = database.isLatestVersion();

		boolean exists = user.readInfo(loginParameters, !isLatestVersion);
		if(!exists)
			throw new UserNotFoundException();

		if(isLatestVersion) {
			user.loadPrivileges();
			user.loadEntries();
		} else if(user.isAdministrator()) {
			user.roles = Arrays.asList(Role.administrator());
			user.privileges = new Privileges(Access.administrator());
		} else
			throw new InvalidVersionException();

		if(user.isBuiltinAdministrator())
			user.addSystemTools();

		ConnectionManager.release();

		return user;
	}

	public static User create(LoginParameters loginParameters) {
		Database database = ApplicationServer.getDatabase();
		User user = new User(database);
		String plainPassword = User.generateOneTimePassword();
		ApplicationServer.getRequest().getParameters().put(new string("plainPassword"), new string(plainPassword));
		user.login = loginParameters.getLogin();
		user.password = Digest.md5(plainPassword);

		Users users = Users.newInstance();
		users.name.get().set(loginParameters.getLogin());
		users.password.get().set(new string(user.password));
		loginParameters.setId(user.id = users.create());
		return read(loginParameters);
	}

	static public User load(LoginParameters loginParameters, String password) {
		Database database = ApplicationServer.getDatabase();

		if(!database.isSystemInstalled())
			return User.system(database);

		User user = read(loginParameters);

		if(password != null && !password.equals(user.getPassword()) /*&& !password.equals(MD5.hex(""))*/ || user.getBanned())
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

	private boolean readInfo(LoginParameters loginParameters, boolean shortInfo) {
		Users users = Users.newInstance();
		Collection<Field> fields = new ArrayList<Field>();
		fields.add(users.recordId.get());
		fields.add(users.name.get());
		fields.add(users.password.get());

		SqlToken where = (loginParameters.getId() != null) ? new Equ(users.recordId.get(), loginParameters.getId())
				: new EqualsIgnoreCase(users.name.get(), new string(loginParameters.getLogin()));

		if(!shortInfo) {
			fields.add(users.banned.get());
			fields.add(users.changePassword.get());
			fields.add(users.firstName.get());
			fields.add(users.middleName.get());
			fields.add(users.lastName.get());
			fields.add(users.settings.get());
			fields.add(users.phone.get());
			fields.add(users.email.get());
		}

		users.read(fields, where);

		if(!users.next())
			return false;

		this.id = users.recordId.get().guid();
		this.login = users.name.get().string().get();
		this.password = users.password.get().string().get();
		this.banned = users.banned.get().bool().get();
		this.changePassword = users.changePassword.get().bool().get();
		this.firstName = users.firstName.get().string().get();
		this.middleName = users.middleName.get().string().get();
		this.lastName = users.lastName.get().string().get();
		this.settings = users.settings.get().string().get();
		this.phone = users.phone.get().string().get();
		this.email = users.email.get().string().get();

		if(shortInfo)
			return true;

		loginParameters.setId(this.id);

		try {
			return users.getExtraParameters(loginParameters, parameters);
		} catch(Throwable e) {
			Trace.logError(e);
			return BuiltinUsers.Administrator.guid().equals(id) || BuiltinUsers.System.guid().equals(id);
		}
	}

	private void loadPrivileges() {
		Access defaultAccess = getAccess();

		privileges = new Privileges(defaultAccess);

		loadTablePrivileges(privileges, defaultAccess);
		loadFieldPrivileges(privileges, defaultAccess);
		loadRequestPrivileges(privileges, defaultAccess);
	}

	private Collection<guid> getRoleIds() {
		Collection<guid> ids = new ArrayList<guid>();
		for(Role role : getRoles())
			ids.add(role.getId());
		return ids;
	}

	private void loadTablePrivileges(Privileges privileges, Access defaultAccess) {
		RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();
		Field tableId = rta.tableId.get();
		Field read = rta.read.get();
		Field write = rta.write.get();
		Field create = rta.create.get();
		Field copy = rta.copy.get();
		Field destroy = rta.destroy.get();

		Collection<Field> groups = new ArrayList<Field>(Arrays.asList(tableId));
		Collection<Field> fields = Arrays.asList(tableId, read, write, create, copy, destroy);

		SqlToken where = rta.roleId.get().inVector(getRoleIds());

		SqlToken notRead = new NotEqu(read, new sql_bool(defaultAccess.getRead()));
		SqlToken notWrite = new NotEqu(write, new sql_bool(defaultAccess.getWrite()));
		SqlToken notCreate = new NotEqu(create, new sql_bool(defaultAccess.getCreate()));
		SqlToken notCopy = new NotEqu(copy, new sql_bool(defaultAccess.getCopy()));
		SqlToken notDestroy = new NotEqu(destroy, new sql_bool(defaultAccess.getDestroy()));
		SqlToken having = new Group(Or.fromList(Arrays.asList(notRead, notWrite, notCreate, notCopy, notDestroy)));

		rta.group(fields, groups, where, having);
		while(rta.next()) {
			Access access = new Access();
			access.setRead(read.bool().get());
			access.setWrite(write.bool().get());
			access.setCreate(create.bool().get());
			access.setCopy(copy.bool().get());
			access.setDestroy(destroy.bool().get());
			privileges.setTableAccess(tableId.guid(), access);
		}
	}

	private void loadFieldPrivileges(Privileges privileges, Access defaultAccess) {
		RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>().get();
		Field tableId = rfa.field.get().tableId.get();
		Field fieldId = rfa.fieldId.get();
		Field read = rfa.read.get();
		Field write = rfa.write.get();

		Collection<Field> groups = new ArrayList<Field>(Arrays.asList(fieldId));
		Collection<Field> fields = Arrays.asList(tableId, read, write);

		SqlToken where = rfa.roleId.get().inVector(getRoleIds());

		rfa.group(fields, groups, where);

		while(rfa.next()) {
			guid table = tableId.guid();

			Access tableAccess = privileges.getTableAccess(table);

			boolean tableReadable = tableAccess.getRead();
			boolean tableWritable = tableAccess.getWrite();

			boolean readable = read.bool().get();
			boolean writable = write.bool().get();

			if(tableReadable == readable && tableWritable == writable)
				continue;

			Access access = new Access();
			access.setRead(readable);
			access.setWrite(writable);
			privileges.setFieldAccess(fieldId.guid(), access);
		}
	}

	private void loadRequestPrivileges(Privileges privileges, Access defaultAccess) {
		RoleRequestAccess rra = new RoleRequestAccess.CLASS<RoleRequestAccess>().get();
		Field requestId = rra.requestId.get();
		Field execute = rra.execute.get();

		Collection<Field> groups = new ArrayList<Field>(Arrays.asList(requestId));
		Collection<Field> fields = Arrays.asList(requestId, execute);

		SqlToken where = rra.roleId.get().inVector(getRoleIds());

		SqlToken notExecute = new NotEqu(execute, new sql_bool(defaultAccess.getExecute()));
		SqlToken having = new Group(notExecute);

		rra.group(fields, groups, where, having);
		while(rra.next()) {
			Access access = new Access();
			access.setExecute(execute.bool().get());
			privileges.setRequestAccess(requestId.guid(), access);
		}
	}

	private void loadEntries() {
		UserEntries userEntries = new UserEntries.CLASS<UserEntries>().get();

		Field entry = userEntries.entryId.get();
		Field id = userEntries.entry.get().classId.get();
		Field name = userEntries.entry.get().name.get();

		Collection<Field> fields = Arrays.asList(entry, id, name);

		Field position = userEntries.position.get();
		Collection<Field> sortFields = Arrays.asList(position);

		SqlToken where = new Equ(userEntries.userId.get(), this.id);

		userEntries.read(fields, sortFields, where);

		List<Entry> entries = new ArrayList<Entry>();

		while(userEntries.next())
			entries.add(new Entry(entry.guid(), id.string().get(), name.string().get()));

		setEntries(entries);
	}

	public String getSettings() {
		return settings;
	}

	public void setSettings(String settings) {
		this.settings = settings;

		Users.saveSettings(id, settings);
	}

	public Database getDatabase() {
		return this.database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	public static String generateOneTimePassword() {
		String saltChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

		StringBuilder plainPassword = new StringBuilder();

		Random rnd = new Random();
		while (plainPassword.length() <= 6) {
			int index = (int) (rnd.nextFloat() * saltChars.length());
			plainPassword.append(saltChars.charAt(index));
		}

		return plainPassword.toString();
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
		RmiIO.writeBoolean(objects, changePassword);

		RmiIO.writeString(objects, settings);

		objects.writeObject(database);

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
		changePassword = RmiIO.readBoolean(objects);

		settings = RmiIO.readString(objects);

		database = (Database)objects.readObject();

		roles = (Collection)objects.readObject();
		privileges = (Privileges)objects.readObject();
		entries = (Collection)objects.readObject();
		parameters = (RLinkedHashMap)objects.readObject();

		objects.close();
	}
}
