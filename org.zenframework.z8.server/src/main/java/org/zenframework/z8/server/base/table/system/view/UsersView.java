package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.Roles;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.system.UserRoles;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
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
	public Listbox.CLASS<Listbox> roles = new Listbox.CLASS<Listbox>(this);

	public UsersView(IObject container) {
		super(container);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void constructor2() {
		super.constructor2();

		columnCount = new integer(12);

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());

		entries.setIndex("entries");
		entries.setDisplayName(UserEntries.displayNames.Title);

		roles.setIndex("roles");
		roles.setDisplayName(UserRoles.displayNames.Title);

		UserEntries userEntries = new UserEntries.CLASS<UserEntries>(this).get();

		entries.get().query = (Query.CLASS<Query>)userEntries.getCLASS();
		entries.get().link = userEntries.user;

		userEntries.columns.add(userEntries.entries.get().name);
		userEntries.columns.add(userEntries.entries.get().id);
		userEntries.sortFields.add(userEntries.position);

		UserRoles userRoles = new UserRoles.CLASS<UserRoles>(this).get();

		roles.get().query = (Query.CLASS<Query>)userRoles.getCLASS();
		roles.get().link = userRoles.user;
		roles.get().source = new Roles.CLASS<Roles>(this);

		userRoles.columns.add(userRoles.roles.get().name);

		name.get().colspan = new integer(3);
		lastName.get().colspan = new integer(3);
		firstName.get().colspan = new integer(3);
		middleName.get().colspan = new integer(3);

		phone.get().colspan = new integer(3);
		email.get().colspan = new integer(3);
		banned.get().colspan = new integer(3);
		banned.get().setIcon("fa-ban");

		description.get().colspan = new integer(12);

		entries.get().colspan = new integer(6);
		roles.get().colspan = new integer(6);

		registerFormField(name);
		registerFormField(lastName);
		registerFormField(firstName);
		registerFormField(middleName);
		registerFormField(phone);
		registerFormField(email);
		registerFormField(banned);
		registerFormField(description);
		registerFormField(roles);
		registerFormField(entries);

		sortFields.add(name);
	}
}
