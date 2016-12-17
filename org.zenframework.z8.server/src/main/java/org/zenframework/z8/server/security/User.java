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
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
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

	private String name;
	private String password;

	private Collection<IRole> roles;
	private IPrivileges privileges;

	private String description;
	private String phone;
	private String email;
	private boolean blocked;

	private String settings;

	private Collection<Component> components = new ArrayList<Component>();
	private RLinkedHashMap<string, primary> parameters = new RLinkedHashMap<string, primary>();

	static private User system = null;

	static public IUser system() {
		if(system != null)
			return system;

		guid id = BuiltinUsers.System.guid();
		String login = Resources.get("BuiltinUsers.System.name");
		String description = Resources.get("BuiltinUsers.System.description");

		system = new User();

		system.id = id;
		system.name = login;
		system.password = "";
		system.description = description;
		system.blocked = false;

		system.settings = "";
		system.phone = "";
		system.email = "";
		system.roles = new HashSet<IRole>(Arrays.asList(Role.administrator()));

		system.addSystemTools();

		return system;
	}

	public User() {
	}

	@Override
	public guid id() {
		return id;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String password() {
		return password;
	}

	@Override
	public Collection<IRole> roles() {
		return roles;
	}

	@Override
	public boolean isAdministrator() {
		for(IRole role : roles) {
			if(role.id().equals(Role.Administrator))
				return true;
		}
		return false;
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
	public boolean blocked() {
		return blocked;
	}

	@Override
	public Collection<Component> components() {
		return components;
	}

	@Override
	public void setComponents(Collection<Component> components) {
		this.components = components;
	}

	@Override
	public Map<string, primary> parameters() {
		return parameters;
	}

	static public IUser load(String login) {
		return load(new string(login));
	}

	static public IUser load(guid userId) {
		return load((primary)userId);
	}

	static public IUser load(string login) {
		return load((primary)login);
	}

	static private IUser load(primary loginOrId) {
		if(!ServerConfig.isSystemInstalled())
			return User.system();

		User user = new User();

		if(loginOrId instanceof string)
			user.readInfo((string)loginOrId);
		else
			user.readInfo((guid)loginOrId);

		user.loadRoles();
		user.loadComponents();

		if(user.isAdministrator())
			user.addSystemTools();

		ConnectionManager.release();

		return user;
	}

	static public IUser load(String login, String password) {
		IUser user = load(login);

		if(password == null || !password.equals(user.password()) || user.blocked())
			throw new AccessDeniedException();

		return user;
	}

	private void addSystemTools() {
		for(Component component : components) {
			if(component.className().equals(SystemTools.ClassName))
				return;
		}

		components.add(new Component(null, SystemTools.ClassName, Resources.get(SystemTools.strings.Title)));
	}

	private void readInfo(guid id) {
		Users users = Users.newInstance();
		SqlToken where = new Equ(users.recordId.get(), id);
		readInfo(users, where);
	}

	private void readInfo(string login) {
		Users users = Users.newInstance();
		SqlToken where = new EqualsIgnoreCase(users.name.get(), login);
		readInfo(users, where);
	}

	private void readInfo(Users users, SqlToken where) {

		Collection<Field> fields = new ArrayList<Field>();
		fields.add(users.recordId.get());
		fields.add(users.name.get());
		fields.add(users.password.get());
		fields.add(users.settings.get());
		fields.add(users.phone.get());
		fields.add(users.email.get());

		users.read(fields, where);

		if(users.next()) {
			this.id = users.recordId.get().guid();
			this.name = users.name.get().string().get();
			this.password = users.password.get().string().get();
			this.settings = users.settings.get().string().get();
			this.phone = users.phone.get().string().get();
			this.email = users.email.get().string().get();
		} else {
			throw new AccessDeniedException();
		}

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
		IPrivileges privileges = new Privileges(defaultAccess);

		RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();
		Field tableId = rta.table.get();
		Field read = rta.read.get();
		Field write = rta.write.get();
		Field create = rta.create.get();
		Field copy = rta.copy.get();
		Field destroy = rta.destroy.get();

		Collection<Field> groups = new ArrayList<Field>(Arrays.asList(rta.table.get()));
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

		System.out.println(privileges);
	}

	private void loadComponents() {
		UserEntries userEntries = new UserEntries.CLASS<UserEntries>().get();

		Field entryId = userEntries.entries.get().id.get();
		Field entryName = userEntries.entries.get().name.get();

		Collection<Field> fields = Arrays.asList(entryId, entryName);

		Field position = userEntries.position.get();
		Collection<Field> sortFields = Arrays.asList(position);

		SqlToken where = new Equ(userEntries.user.get(), id);

		userEntries.read(fields, sortFields, where);

		List<Component> components = new ArrayList<Component>();

		while(userEntries.next()) {
			String className = entryId.string().get();
			String title = entryName.string().get();
			components.add(new Component(null, className, title));
		}

		setComponents(components);
	}

	@Override
	public String settings() {
		return settings;
	}

	@Override
	public void setSettings(String settings) {
		this.settings = settings;
	}

	@Override
	public void save(Database database) {
		Users users = new Users.CLASS<Users>().get();

		users.settings.get().set(new string(settings));
		users.update(id);
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

		RmiIO.writeString(objects, name);
		RmiIO.writeString(objects, password);

		RmiIO.writeString(objects, description);
		RmiIO.writeString(objects, phone);
		RmiIO.writeString(objects, email);

		RmiIO.writeBoolean(objects, blocked);

		RmiIO.writeString(objects, settings);

		objects.writeObject(roles);
		objects.writeObject(privileges);
		objects.writeObject(components);
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

		name = RmiIO.readString(objects);
		password = RmiIO.readString(objects);

		description = RmiIO.readString(objects);
		phone = RmiIO.readString(objects);
		email = RmiIO.readString(objects);

		blocked = RmiIO.readBoolean(objects);

		settings = RmiIO.readString(objects);

		roles = (Collection)objects.readObject();
		privileges = (IPrivileges)objects.readObject();
		components = (Collection)objects.readObject();
		parameters = (RLinkedHashMap)objects.readObject();

		objects.close();
	}
}
