package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Form;
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

public class UsersForm extends Form {
	public static class CLASS<T extends UsersForm> extends Form.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(UsersForm.class);
			setAttribute(SystemTool, Integer.toString(100));
		}

		@Override
		public Object newObject(IObject container) {
			return new UsersForm(container);
		}
	}

	public Users.CLASS<? extends Users> users = new Users.CLASS<Users>();

	public Listbox.CLASS<Listbox> entriesListbox = new Listbox.CLASS<Listbox>(this);
	public Listbox.CLASS<Listbox> rolesListbox = new Listbox.CLASS<Listbox>(this);

	private UserEntries.CLASS<UserEntries> userEntries = new UserEntries.CLASS<UserEntries>(this);
	private UserRoles.CLASS<UserRoles> userRoles = new UserRoles.CLASS<UserRoles>(this);

	private Section.CLASS<Section> section2 = new Section.CLASS<Section>(this);
	private Section.CLASS<Section> section3 = new Section.CLASS<Section>(this);

	private ResetPasswordAction.CLASS<ResetPasswordAction> resetPassword = new ResetPasswordAction.CLASS<ResetPasswordAction>(this);

	public UsersForm(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(users);

		objects.add(resetPassword);

		objects.add(section2);
		objects.add(section3);

		objects.add(userEntries);
		objects.add(userRoles);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		userEntries.setIndex("userEntries");
		userRoles.setIndex("userRoles");

		section2.setIndex("section2");
		section3.setIndex("section3");
		resetPassword.setIndex("resetPassword");

		colCount = new integer(12);

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());

		entriesListbox.setIndex("entriesListbox");
		entriesListbox.setDisplayName(UserEntries.displayNames.Title);

		rolesListbox.setIndex("rolesListbox");
		rolesListbox.setDisplayName(UserRoles.displayNames.Title);

		UserEntries userEntries = this.userEntries.get();

		entriesListbox.get().query = this.userEntries;
		entriesListbox.get().link = userEntries.user;
		entriesListbox.get().flex = new integer(1);

		userEntries.position.get().editable = bool.True;

		userEntries.columns.add(userEntries.entries.get().name);
		userEntries.columns.add(userEntries.position);
		userEntries.sortFields.add(userEntries.position);

		UserRoles userRoles = this.userRoles.get();

		Source.CLASS<Source> rolesSource = new Source.CLASS<Source>(rolesListbox.get());
		rolesSource.get().query = new RoleTableAccessView.CLASS<RoleTableAccessView>(this);

		rolesListbox.get().query = this.userRoles;
		rolesListbox.get().link = userRoles.user;
		rolesListbox.get().source = rolesSource;
		rolesListbox.get().flex = new integer(1);

		userRoles.columns.add(userRoles.roles.get().name);

		users.get().name.get().colSpan = new integer(3);
		users.get().name.get().editable = bool.True;
		users.get().banned.get().colSpan = new integer(3);
		users.get().banned.get().setIcon("fa-ban");
		users.get().changePassword.get().colSpan = new integer(3);
		users.get().changePassword.get().setIcon("fa-key");
		resetPassword.get().type = ActionType.Primary;

		users.get().firstName.get().colSpan = new integer(3);
		users.get().lastName.get().colSpan = new integer(3);
		users.get().lastName.get().editable = bool.True;
		users.get().middleName.get().colSpan = new integer(3);
		section2.get().colSpan = new integer(3);

		users.get().phone.get().colSpan = new integer(3);
		users.get().email.get().colSpan = new integer(3);
		section3.get().colSpan = new integer(6);

		users.get().company.get().colSpan = new integer(3);
		users.get().position.get().colSpan = new integer(3);

		users.get().description.get().colSpan = new integer(12);
		users.get().description.get().height = new integer(3);

		entriesListbox.get().colSpan = new integer(6);
		rolesListbox.get().colSpan = new integer(6);

		controls.add(users.get().name);
		controls.add(users.get().banned);
		controls.add(users.get().changePassword);
		controls.add(resetPassword);

		controls.add(users.get().lastName);
		controls.add(users.get().firstName);
		controls.add(users.get().middleName);
		controls.add(section2);

		controls.add(users.get().phone);
		controls.add(users.get().email);
		controls.add(section3);

		controls.add(users.get().company);
		controls.add(users.get().position);

		controls.add(users.get().description);
		controls.add(rolesListbox);
		controls.add(entriesListbox);
	}
}
