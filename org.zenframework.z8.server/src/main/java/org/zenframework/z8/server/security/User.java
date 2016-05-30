package org.zenframework.z8.server.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.SystemTools;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.string.Lower;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;
import org.zenframework.z8.server.utils.ArrayUtils;

public class User implements IUser {
	private static final long serialVersionUID = -4955893424674255525L;

	private guid id;

	private String name;
	private String password;
	private SecurityGroup securityGroup = SecurityGroup.Users;

	private String description;
	private String phone;
	private String email;
	private boolean blocked;

	private String settings;

	private Collection<Component> components = new ArrayList<Component>();
	private RLinkedHashMap<string, primary> parameters = new RLinkedHashMap<string, primary>();

	static public IUser system() {
		guid id = BuiltinUsers.System.guid();
		String login = Resources.get("BuiltinUsers.System.name");
		String description = Resources.get("BuiltinUsers.System.description");
		String password = "";
		boolean blocked = false;

		User user = new User();

		user.id = id;
		user.name = login;
		user.password = password;
		user.description = description;
		user.blocked = blocked;

		user.settings = "";
		user.phone = "";
		user.email = "";
		user.securityGroup = SecurityGroup.Administrators;

		user.setComponents(ArrayUtils.collection(new Component(null, SystemTools.class.getCanonicalName(), Resources.get(SystemTools.strings.Title))));

		return user;
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
	public SecurityGroup securityGroup() {
		return securityGroup;
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
		if(!ApplicationServer.database().isSystemInstalled())
			return User.system();

		User user = new User();
		user.readInfo(login);

		user.readComponents();

		if(user.securityGroup == SecurityGroup.Administrators)
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
			if(component.className().equals(SystemTools.class.getCanonicalName())) {
				return;
			}
		}

		components.add(new Component(null, SystemTools.class.getCanonicalName(), Resources.get(SystemTools.strings.Title)));
	}

	@SuppressWarnings("unchecked")
	private Users getUsers() {
		CLASS<? extends Users> result = null;
		Class<?> cls = Users.class;

		for(CLASS<? extends Table> table : Runtime.instance().tables()) {
			if(table.instanceOf(cls)) {
				result = (CLASS<? extends Users>)table;
				cls = result.getJavaClass();
			}
		}

		return result != null ? (Users)result.newInstance() : null;
	}

	private void readInfo(String login) {
		Users users = getUsers();

		Collection<Field> fields = new ArrayList<Field>();
		fields.add(users.recordId.get());
		fields.add(users.password.get());
		users.password.get().mask = false;
		fields.add(users.settings.get());
		fields.add(users.phone.get());
		fields.add(users.email.get());
		fields.add(users.securityGroup.get());

		SqlToken where = new Rel(new Lower(users.name.get()), Operation.Eq, new sql_string(login.toLowerCase()));

		users.read(fields, where);

		if(users.next()) {
			this.id = users.recordId.get().guid();
			this.name = login;
			this.password = users.password.get().string().get();
			this.settings = users.settings.get().string().get();
			this.phone = users.phone.get().string().get();
			this.email = users.email.get().string().get();
			this.securityGroup = SecurityGroup.fromGuid(users.securityGroup.get().guid());
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

	private void readComponents() {
		UserEntries userEntries = new UserEntries.CLASS<UserEntries>().get();

		Collection<Field> fields = new ArrayList<Field>();
		fields.add(userEntries.entries.get().id.get());
		fields.add(userEntries.entries.get().name.get());

		Collection<Field> sortFields = new ArrayList<Field>();
		sortFields.add(userEntries.position.get());

		SqlToken first = new Rel(userEntries.user.get(), Operation.Eq, userEntries.users.get().recordId.get());
		SqlToken second = new Rel(userEntries.entry.get(), Operation.Eq, userEntries.entries.get().recordId.get());
		SqlToken third = new Rel(new Lower(userEntries.users.get().name.get()), Operation.Eq, new sql_string(name().toLowerCase()));

		SqlToken where = new And(new And(first, second), third);

		userEntries.read(fields, sortFields, where);

		List<Component> components = new ArrayList<Component>();

		while(userEntries.next()) {
			String className = userEntries.entries.get().id.get().string().get();
			String title = userEntries.entries.get().name.get().string().get();
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
	
    private void writeObject(ObjectOutputStream out)  throws IOException {
    	serialize(out);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	deserialize(in);
    }

    @Override
	public void serialize(ObjectOutputStream out) throws IOException {
		out.writeLong(serialVersionUID);
		
		RmiIO.writeGuid(out, id);

		RmiIO.writeString(out, name);
		RmiIO.writeString(out, password);
		
		RmiIO.writeGuid(out, securityGroup.guid());

		RmiIO.writeString(out, description);
		RmiIO.writeString(out, phone);
		RmiIO.writeString(out, email);
		
		out.writeBoolean(blocked);

		RmiIO.writeString(out, settings);

		out.writeInt(components.size());
		for(Component component : components) 
			out.writeObject(component);

		out.writeInt(parameters.size());
		for(string key : parameters.keySet()) {
			RmiIO.writeString(out, key);
			RmiIO.writePrimary(out, parameters.get(key));
		}
	}
	
    @Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {	
		@SuppressWarnings("unused")
		long version = in.readLong();
		
		id = RmiIO.readGuid(in);

		name = RmiIO.readString(in);
		password = RmiIO.readString(in);
		securityGroup = SecurityGroup.fromGuid(RmiIO.readGuid(in));
		
		description = RmiIO.readString(in);
		phone = RmiIO.readString(in);
		email = RmiIO.readString(in);
		
		blocked = in.readBoolean();

		settings = RmiIO.readString(in);

		int count = in.readInt();
		components = new ArrayList<Component>();
		for(int i = 0; i < count; i++)
			components.add((Component)in.readObject());
		
		count = in.readInt();
		parameters = new RLinkedHashMap<string, primary>();
		for(int i = 0; i < count; i++) {
			string key = new string(RmiIO.readString(in));
			primary value = RmiIO.readPrimary(in);
			parameters.put(key, value);
		}
	}
}
