package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.form.Section;
import org.zenframework.z8.server.base.form.Source;
import org.zenframework.z8.server.base.form.action.ActionType;
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
			setAttribute(SystemTool, Integer.toString(100));
		}

		@Override
		public Object newObject(IObject container) {
			return new UsersView(container);
		}
	}

	public Listbox.CLASS<Listbox> entriesListbox = new Listbox.CLASS<Listbox>(this);
	public Listbox.CLASS<Listbox> rolesListbox = new Listbox.CLASS<Listbox>(this);

	private UserEntries.CLASS<UserEntries> userEntries = new UserEntries.CLASS<UserEntries>(this);
	private UserRoles.CLASS<UserRoles> userRoles = new UserRoles.CLASS<UserRoles>(this);

	private Section.CLASS<Section> section2 = new Section.CLASS<Section>(this);

	private ResetPasswordAction.CLASS<ResetPasswordAction> resetPassword = new ResetPasswordAction.CLASS<ResetPasswordAction>(this);

	public UsersView(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(resetPassword);

		objects.add(section2);

		objects.add(userEntries);
		objects.add(userRoles);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		userEntries.setIndex("userEntries");
		userRoles.setIndex("userRoles");

		section2.setIndex("section2");
		resetPassword.setIndex("resetPassword");

		colCount = new integer(12);

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());

		entriesListbox.setIndex("entriesListbox");
		entriesListbox.setDisplayName(UserEntries.displayNames.Title);

		rolesListbox.setIndex("rolesListbox");
		rolesListbox.setDisplayName(UserRoles.displayNames.Title);

		UserEntries userEntries = this.userEntries.get();

		entriesListbox.get().query = this.userEntries;
		entriesListbox.get().link = userEntries.userId;
		entriesListbox.get().flex = new integer(1);

		userEntries.position.get().editable = bool.True;

		userEntries.columns.add(userEntries.entry.get().name);
		userEntries.columns.add(userEntries.position);
		userEntries.sortFields.add(userEntries.position);

		UserRoles userRoles = this.userRoles.get();

		Source.CLASS<Source> rolesSource = new Source.CLASS<Source>(rolesListbox.get());
		rolesSource.get().query = new RoleView.CLASS<RoleView>(this);

		rolesListbox.get().query = this.userRoles;
		rolesListbox.get().link = userRoles.userId;
		rolesListbox.get().source = rolesSource;
		rolesListbox.get().flex = new integer(1);

		userRoles.columns.add(userRoles.role.get().name);

		name.get().colSpan = new integer(3);
		name.get().editable = bool.True;
		banned.get().colSpan = new integer(3);
		banned.get().setIcon("fa-ban");
		changePassword.get().colSpan = new integer(3);
		changePassword.get().setIcon("fa-key");
		resetPassword.get().type = ActionType.Primary;

		firstName.get().colSpan = new integer(3);
		lastName.get().colSpan = new integer(3);
		lastName.get().editable = bool.True;
		middleName.get().colSpan = new integer(3);
		section2.get().colSpan = new integer(3);

		phone.get().colSpan = new integer(3);
		email.get().colSpan = new integer(3);

		description.get().colSpan = new integer(12);
		description.get().height = new integer(3);

		entriesListbox.get().colSpan = new integer(6);
		rolesListbox.get().colSpan = new integer(6);

		registerControl(name);
		registerControl(banned);
		registerControl(changePassword);
		registerControl(resetPassword);

		registerControl(lastName);
		registerControl(firstName);
		registerControl(middleName);
		registerControl(section2);

		registerControl(phone);
		registerControl(email);
		registerControl(description);
		registerControl(rolesListbox);
		registerControl(entriesListbox);

		sortFields.add(name);

		names.add(name);
		names.add(lastName);

		quickFilters.add(name);
		quickFilters.add(lastName);
	}
}
