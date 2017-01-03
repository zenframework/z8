package org.zenframework.z8.server.base.security;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class User extends OBJECT {
	public static class CLASS<T extends User> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(User.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new User(container);
		}
	}

	public guid id;
	public string login;
	public string firstName;
	public string middleName;
	public string lastName;

	public string description;
	public string phone;
	public string email;

	public RLinkedHashMap<string, primary> parameters;

	public User(IObject container) {
		super(container);
	}

	public void initialize(IUser user) {
		if(user == null)
			user = org.zenframework.z8.server.security.User.system();

		setValues(user);
	}

	private void setValues(IUser user) {
		if(user == null)
			user = org.zenframework.z8.server.security.User.system();

		id = user.id();
		login = new string(user.login());

		firstName = new string(user.firstName());
		middleName = new string(user.middleName());
		lastName = new string(user.lastName());

		description = new string(user.description());
		phone = new string(user.phone());
		email = new string(user.email());

		parameters = (RLinkedHashMap<string, primary>)user.parameters();
	}
}
