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
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.NotEqu;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.functions.string.EqualsIgnoreCase;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.exceptions.InvalidVersionException;
import org.zenframework.z8.server.exceptions.UserNotFoundException;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.utils.Email;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

public class User implements IUser {
	static private final long serialVersionUID = -4955893424674255525L;
	static private final int verificationTimeHours = 12;

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
	private String company;
	private String position;
	private String verification;
	
	private boolean banned;
	private boolean changePassword;

	private String settings;
	private IDatabase database;

	private Collection<Entry> entries = new ArrayList<Entry>();
	private RLinkedHashMap<string, primary> parameters = new RLinkedHashMap<string, primary>();

	static public IUser system(String scheme) {
		return system(Database.get(scheme));
	}

	static public IUser system(IDatabase database) {
		guid id = BuiltinUsers.System.guid();
		String login = BuiltinUsers.displayNames.SystemName;

		User system = new User(database);

		system.id = id;
		system.login = login;
		system.password = "";
		system.firstName = "";
		system.middleName = "";
		system.lastName = "";
		system.company = "";
		system.position = "";
		system.banned = false;
		system.changePassword = false;

		system.settings = "";
		system.phone = "";
		system.email = "";
		system.roles = new HashSet<IRole>(Arrays.asList(Role.administrator()));
		system.privileges = new Privileges(Access.administrator());

		system.addSystemTools();

		return system;
	}

	public User() {
	}

	public User(IDatabase database) {
		this.database = database;
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
	public String verification() {
		return verification;
	}
	
	@Override
	public String company() {
		return company;
	}
	
	@Override
	public String position() {
		return position;
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
	public boolean isBuiltinAdministrator() {
		return id.equals(Users.Administrator) || id.equals(Users.System);
	}

	@Override
	public boolean isAdministrator() {
		if(roles != null) {
			for(IRole role : roles) {
				if(role.id().equals(Role.Administrator))
					return true;
			}
		}
		return isBuiltinAdministrator();
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
	public boolean changePassword() {
		return changePassword;
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
		return userId.isNull() ? null : read(new LoginParameters(userId));
	}

	static private IUser read(LoginParameters loginParameters) {
		IDatabase database = ApplicationServer.getDatabase();
		User user = new User(database);

		boolean isLatestVersion = database.isLatestVersion();

		boolean exists = user.readInfo(loginParameters, !isLatestVersion);
		if(!exists)
			throw new UserNotFoundException();

		if(isLatestVersion) {
			user.loadRoles();
			user.loadEntries();
		} else if(user.isAdministrator()) {
			user.roles = new HashSet<IRole>(Arrays.asList(Role.administrator()));
			user.privileges = new Privileges(Access.administrator());
		} else
			throw new InvalidVersionException();

		if(user.isBuiltinAdministrator())
			user.addSystemTools();

		ConnectionManager.release();

		return user;
	}
	
	private static String getVerificationLink(String host, String hashPrefix, String verification) {
		return new StringBuilder().append(host).append("#").append(hashPrefix).append("_").append(verification).toString();
	}
	
	public static IUser create(LoginParameters loginParameters) {
		IDatabase database = ApplicationServer.getDatabase();
		User user = new User(database);
		
		String plainPassword = User.generateOneTimePassword();
		ApplicationServer.getRequest().getParameters().put(new string("plainPassword"), new string(plainPassword));
		user.login = loginParameters.getLogin();
		user.password = Digest.md5(plainPassword);

		Users users = Users.newInstance();
		users.name.get().set(loginParameters.getLogin());
		users.password.get().set(new string(user.password));
		user.id = users.create();
		
		return read(loginParameters.setUserId(user.id));
	}
	
	public static IUser register(LoginParameters loginParameters, String password, String host) {
		IDatabase database = ApplicationServer.getDatabase();
		User user = new User(database);
		
		user.login = loginParameters.getLogin();
		user.lastName = loginParameters.getLastName();
		user.firstName = loginParameters.getFirstName();
		user.email = loginParameters.getEmail();
		user.company = loginParameters.getCompany();
		user.position = loginParameters.getPosition();
		user.password = password;

		Users users = Users.newInstance();
		if(users.readFirst(Arrays.asList(users.recordId.get()), new Equ(users.name.get(), user.login))) // user already exist
			throw new UserNotFoundException();
		
		String verification = Digest.md5(guid.create().toString());
		
		users = Users.newInstance();
		users.name.get().set(user.login);
		users.lastName.get().set(user.lastName);
		users.firstName.get().set(user.firstName);
		users.email.get().set(user.email);
		users.company.get().set(user.company);
		users.position.get().set(user.position);
		users.banned.get().set(true);
		users.password.get().set(user.password);
		users.changePassword.get().set(false);
		users.verification.get().set(verification);
		Connection connection = ConnectionManager.get();
		connection.beginTransaction();
		try {
			user.id = users.create();
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
		Email.send(new Email.Message(Email.TYPE.Registration)
				.setRecipientAddress(user.email)
				.setRecipientName(user.firstName)
				.setButtonLink(getVerificationLink(host, "verify", verification)));
		return user;
	}
	
	public static IUser verify(String verification, String host) {
		Users users = Users.newInstance();
		if (verification == null || verification.isEmpty() || !users.readFirst(Arrays.asList(users.banned.get(), users.firstName.get(), users.email.get(), users.name.get(), users.verificationModAt.get()), new Equ(users.verification.get(), verification)))
			throw new UserNotFoundException();
		if (!users.banned.get().bool().get())
			throw new UserNotFoundException();
		
		IDatabase database = ApplicationServer.getDatabase();
		User user = new User(database);
		
		date expirationDate = new date(users.verificationModAt.get().toString()).addHour(verificationTimeHours);
		
		user.login = users.name.get().string().get();
		
		Connection connection = ConnectionManager.get();
		connection.beginTransaction();
		if (new date().operatorMore(expirationDate).get()) {
			String newVerification = Digest.md5(guid.create().toString());
			users.verification.get().set(newVerification);
			user.banned = true;
			Email.send(new Email.Message(Email.TYPE.Registration)
					.setRecipientAddress(users.email.get().string().get())
					.setRecipientName(users.firstName.get().string().get())
					.setButtonLink(getVerificationLink(host, "verify", newVerification)));
		} else {
			user.banned = false;
			users.banned.get().set(user.banned);
			users.verification.get().set("");
			/*Email.send(new Email.Message(Email.TYPE.VerificationSuccess)
					.setRecipientAddress(users.email.get().string().get())
					.setRecipientName(users.firstName.get().string().get()));*/
		}
		
		try {
			users.update(users.recordId());
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
		
		return user;
	}
	
	public static IUser remindInit(String login, String host) {
		Users users = Users.newInstance();
		if (login == null || login.isEmpty() || !users.readFirst(Arrays.asList(users.verification.get(), users.firstName.get(), users.banned.get(), users.email.get()), new Equ(users.name.get(), login)))
			throw new UserNotFoundException();
		if (users.banned.get().bool().get() || !users.verification.get().string().get().isEmpty())
			throw new UserNotFoundException();
		
		IDatabase database = ApplicationServer.getDatabase();
		User user = new User(database);
		
		Connection connection = ConnectionManager.get();
		connection.beginTransaction();
		
		String verification = Digest.md5(guid.create().toString());
		users.verification.get().set(verification);
		try {
			users.update(users.recordId());
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
		Email.send(new Email.Message(Email.TYPE.RemindPassword)
				.setRecipientAddress(users.email.get().string().get())
				.setRecipientName(users.firstName.get().string().get())
				.setButtonLink(getVerificationLink(host, "remind", verification)));
		return user;
	}
	
	public static IUser remind(String verification, String host) {
		Users users = Users.newInstance();
		if (verification == null || verification.isEmpty() || !users.readFirst(Arrays.asList(users.banned.get(), users.firstName.get(), users.verification.get(), users.email.get(), users.name.get(), users.verificationModAt.get()), new Equ(users.verification.get(), verification)))
			throw new UserNotFoundException();
		if (users.banned.get().bool().get())
			throw new UserNotFoundException();
		
		IDatabase database = ApplicationServer.getDatabase();
		User user = new User(database);
		
		date expirationDate = new date(users.verificationModAt.get().toString()).addHour(verificationTimeHours); 
		
		user.verification = users.verification.get().string().get();
		user.login = users.name.get().string().get();
		
		if (new date().operatorMore(expirationDate).get()) {
			Connection connection = ConnectionManager.get();
			connection.beginTransaction();

			String newVerification = Digest.md5(guid.create().toString());
			users.verification.get().set(newVerification);

			try {
				users.update(users.recordId());
				connection.commit();
			} catch(Throwable e) {
				connection.rollback();
				throw new RuntimeException(e);
			}
			/* Repeated message with updated link */
			Email.send(new Email.Message(Email.TYPE.RemindPassword)
					.setRecipientAddress(users.email.get().string().get())
					.setRecipientName(users.firstName.get().string().get())
					.setButtonLink(getVerificationLink(host, "remind", newVerification)));
		}
		
		return user;
	}
	
	public static IUser changePassword(String verification, String password, String host) {
		Users users = Users.newInstance();
		if (verification == null || verification.isEmpty() || !users.readFirst(Arrays.asList(users.banned.get(), users.firstName.get(), users.changePassword.get(), users.email.get(), users.name.get(), users.verificationModAt.get()), new Equ(users.verification.get(), verification)))
			throw new UserNotFoundException();
		if (users.banned.get().bool().get())
			throw new UserNotFoundException();
		
		IDatabase database = ApplicationServer.getDatabase();
		User user = new User(database);
		
		user.password = password;
		user.changePassword = false;
		
		Connection connection = ConnectionManager.get();
		connection.beginTransaction();
		
		users.verification.get().set("");
		users.password.get().set(user.password);
		users.changePassword.get().set(user.changePassword);
		
		try {
			users.update(users.recordId());
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
		
		Email.send(new Email.Message(Email.TYPE.PasswordChanged)
				.setRecipientAddress(users.email.get().string().get())
				.setRecipientName(users.firstName.get().string().get()));
		
		return user;
	}

	static public IUser load(LoginParameters loginParameters, String password) {
		IDatabase database = ApplicationServer.getDatabase();

		if(!database.isSystemInstalled())
			return User.system(database);

		IUser user = read(loginParameters);

		if(password != null && !password.equals(user.password()) /*&& !password.equals(MD5.hex(""))*/ || user.banned())
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

		SqlToken where = (loginParameters.getUserId() != null) ? new Equ(users.recordId.get(), loginParameters.getUserId())
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
			fields.add(users.company.get());
			fields.add(users.position.get());
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
		this.company = users.company.get().string().get();
		this.position = users.position.get().string().get();
		
		if(shortInfo)
			return true;

		loginParameters.setUserId(this.id);

		try {
			return users.getExtraParameters(loginParameters, parameters);
		} catch(Throwable e) {
			Trace.logError(e);
			return BuiltinUsers.Administrator.guid().equals(id) || BuiltinUsers.System.guid().equals(id);
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
			access = access.or(role.access());

		return access;
	}

	private void loadRoles() {
		UserRoles userRoles = new UserRoles.CLASS<UserRoles>().get();
		roles = userRoles.get(id);

		IAccess defaultAccess = defaultAccess();
		privileges = new Privileges(defaultAccess);

		loadTableAccess(privileges, defaultAccess);
		loadFieldAccess(privileges, defaultAccess);
		loadRequestAccess(privileges, defaultAccess);

		/*
		* Trace.logEvent(privileges);
		*/
	}

	private void loadTableAccess(IPrivileges privileges, IAccess defaultAccess) {
		RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();
		Field tableId = rta.table.get();
		Field read = rta.read.get();
		Field write = rta.write.get();
		Field create = rta.create.get();
		Field copy = rta.copy.get();
		Field destroy = rta.destroy.get();

		Collection<Field> groups = new ArrayList<Field>(Arrays.asList(tableId));
		Collection<Field> fields = Arrays.asList(tableId, read, write, create, copy, destroy);

		SqlToken where = rta.role.get().inVector(getRoles());

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

	private void loadFieldAccess(IPrivileges privileges, IAccess defaultAccess) {
		RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>().get();
		Field tableId = rfa.fields.get().table.get();
		Field fieldId = rfa.field.get();
		Field read = rfa.read.get();
		Field write = rfa.write.get();

		Collection<Field> groups = new ArrayList<Field>(Arrays.asList(fieldId));
		Collection<Field> fields = Arrays.asList(tableId, read, write);

		SqlToken where = rfa.role.get().inVector(getRoles());

		rfa.group(fields, groups, where);

		while(rfa.next()) {
			guid table = tableId.guid();

			IAccess tableAccess = privileges.getTableAccess(table);

			boolean tableReadable = tableAccess.read();
			boolean tableWritable = tableAccess.write();

			boolean readable = read.bool().get();
			boolean writable = write.bool().get();

			if(tableReadable == readable && tableWritable == writable)
				continue;

			IAccess access = new Access();
			access.setRead(readable);
			access.setWrite(writable);
			privileges.setFieldAccess(fieldId.guid(), access);
		}
	}

	private void loadRequestAccess(IPrivileges privileges, IAccess defaultAccess) {
		RoleRequestAccess rra = new RoleRequestAccess.CLASS<RoleRequestAccess>().get();
		Field requestId = rra.request.get();
		Field execute = rra.execute.get();

		Collection<Field> groups = new ArrayList<Field>(Arrays.asList(requestId));
		Collection<Field> fields = Arrays.asList(requestId, execute);

		SqlToken where = rra.role.get().inVector(getRoles());

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

	public IDatabase database() {
		return this.database;
	}

	public void setDatabase(IDatabase database) {
		this.database = database;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	public static String generateOneTimePassword()
	{
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
		
		RmiIO.writeString(objects, company);
		RmiIO.writeString(objects, position);

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
		
		company = RmiIO.readString(objects);
		position = RmiIO.readString(objects);

		banned = RmiIO.readBoolean(objects);
		changePassword = RmiIO.readBoolean(objects);

		settings = RmiIO.readString(objects);

		database = (IDatabase)objects.readObject();

		roles = (Collection)objects.readObject();
		privileges = (IPrivileges)objects.readObject();
		entries = (Collection)objects.readObject();
		parameters = (RLinkedHashMap)objects.readObject();

		objects.close();
	}
}
