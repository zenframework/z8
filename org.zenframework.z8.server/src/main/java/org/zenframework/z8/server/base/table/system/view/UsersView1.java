package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;

public class UsersView1 extends Query {
	public static class CLASS<T extends UsersView1> extends Query.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(UsersView1.class);
			setDisplayName(Resources.get(Users.strings.Title));
		}

		@Override
		public Object newObject(IObject container) {
			return new UsersView1(container);
		}
	}

	private Users.CLASS<Users> users = new Users.CLASS<Users>(this);

	public UsersView1(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		registerFormField(users.get().name);
		registerFormField(users.get().description);
		registerFormField(users.get().password);
		registerFormField(users.get().phone);
		registerFormField(users.get().email);
		registerFormField(users.get().blocked);
		registerFormField(users.get().securityGroups.get().name);

		queries.add(users);

		sortFields.add(users.get().name);
	}
}
