package org.zenframework.z8.server.base.security;

import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.security.IRole;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class User extends OBJECT {
	static public String ClassName = "User";

	public static class CLASS<T extends User> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(User.class);
			setName(ClassName);
			setDisplayName("User");
		}

		@Override
		public Object newObject(IObject container) {
			return new User(container);
		}
	}

	static public User newInstance() {
		return (User)Runtime.instance().getNamed(ClassName).newInstance();
	}

	static public User.CLASS<? extends User> get(IUser user) {
		return newInstance().initialize(user);
	}

	static public User newInstance(IUser user) {
		return get(user).get();
	}

	private IUser user = new org.zenframework.z8.server.security.User();
	private RLinkedHashMap<guid, string> roles = new RLinkedHashMap<guid, string>();


	public guid id;
	public string login;
	public string firstName;
	public string middleName;
	public string lastName;

	public string description;
	public string phone;
	public string email;

	public RLinkedHashMap<string, primary> parameters;

	private boolean isAdministrator;

	public User(IObject container) {
		super(container);
	}

	public User.CLASS<? extends User> initialize(IUser user) {
		if(user == null)
			return null;

		this.user = user;

		if(user.getId() == null || !user.hasRoles())
			return (User.CLASS<?>)this.getCLASS();

		for(IRole role : user.getRoles())
			roles.put(role.id(), new string(role.name()));

		id = user.getId();
		login = new string(user.login());

		firstName = new string(user.firstName());
		middleName = new string(user.middleName());
		lastName = new string(user.lastName());

		description = new string(user.description());
		phone = new string(user.phone());
		email = new string(user.email());

		parameters = (RLinkedHashMap<string, primary>)user.parameters();

		isAdministrator = user.isAdministrator();

		return (User.CLASS<?>)this.getCLASS();
	}

	public void onBeforeLoad(String login) {
		z8_onBeforeLoad(new string(login));
	}

	public boolean authenticate(String password) {
		return z8_authenticate(password != null ? new string(password) : null).get();
	}

	public void onLoad() {
		z8_onLoad();
	}

	public bool z8_isAdministrator() {
		return new bool(isAdministrator);
	}

	public bool z8_isBuiltinAdministrator() {
		return new bool(user.isBuiltinAdministrator());
	}

	public bool z8_isBuiltinSystem() {
		return new bool(user.isBuiltinSystem());
	}

	public string z8_getParameter(string key, string defaultValue) {
		return parameters.containsKey(key) ? (string)parameters.get(key) : defaultValue;
	}

	public guid z8_getParameter(string key, guid defaultValue) {
		return parameters.containsKey(key) ? (guid)parameters.get(key) : defaultValue;
	}

	public integer z8_getParameter(string key, integer defaultValue) {
		return parameters.containsKey(key) ? (integer)parameters.get(key) : defaultValue;
	}

	public bool z8_getParameter(string key, bool defaultValue) {
		return parameters.containsKey(key) ? (bool)parameters.get(key) : defaultValue;
	}

	protected void z8_onBeforeLoad(string login) {
	}

	protected bool z8_authenticate(string password) {
		return bool.False;
	}

	protected void z8_onLoad() {
	}
}
