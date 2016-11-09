package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.SecurityGroup;
import org.zenframework.z8.server.types.integer;

public class UsersView extends Users {
	public static class CLASS<T extends UsersView> extends Users.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(UsersView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new UsersView(container);
		}
	}

	public Listbox.CLASS<Listbox> entries = new Listbox.CLASS<Listbox>(this);

	public UsersView(IObject container) {
		super(container);
	}

	public static class __UserEntries extends UserEntries {
		public static class CLASS<T extends UsersView.__UserEntries> extends UserEntries.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(UsersView.__UserEntries.class);
			}

			public Object newObject(IObject container) {
				return new UsersView.__UserEntries(container);
			}
		}

		public __UserEntries(IObject container) {
			super(container);
		}

		public void constructor2() {
			super.constructor2();
			registerFormField(entries.get().name);
			registerFormField(entries.get().id);
			sortFields.add(position);
		}
	};

	@Override
	public void constructor2() {
		super.constructor2();

		columns = new integer(6);

		boolean administrator = ApplicationServer.getUser().securityGroup() == SecurityGroup.Administrators;
		readOnly.set(!administrator);

		entries.setIndex("entries");
		entries.setDisplayName(UserEntries.displayNames.Title);

		__UserEntries.CLASS<__UserEntries> userEntries = new __UserEntries.CLASS<__UserEntries>(this);

		entries.get().query = userEntries;
		entries.get().link = userEntries.get().user;

		name.get().colspan = new integer(4);
		registerFormField(name);
		securityGroups.get().name.get().colspan = new integer(4);
		registerFormField(securityGroups.get().name);
		password.get().colspan = new integer(4);
		registerFormField(password);
		phone.get().colspan = new integer(4);
		registerFormField(phone);
		email.get().colspan = new integer(4);
		registerFormField(email);
		blocked.get().colspan = new integer(4);
		registerFormField(blocked);

		description.get().colspan = new integer(6);
		registerFormField(description);

		entries.get().colspan = new integer(6);
		registerFormField(entries);

		sortFields.add(name);
	}
}
