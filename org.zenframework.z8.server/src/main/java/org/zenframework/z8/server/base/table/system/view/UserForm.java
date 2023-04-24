package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Form;
import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.form.Section;
import org.zenframework.z8.server.base.form.Source;
import org.zenframework.z8.server.base.form.action.ActionType;
import org.zenframework.z8.server.base.table.system.Roles;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.system.UserRoles;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.db.sql.functions.Sql;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class UserForm extends Form {
	public static class CLASS<T extends UserForm> extends Form.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(UserForm.class);
			setAttribute(SystemTool, Integer.toString(100));
		}

		@Override
		public Object newObject(IObject container) {
			return new UserForm(container);
		}
	}

	public Users.CLASS<? extends Users> users = new Users.CLASS<Users>();

	protected Listbox.CLASS<Listbox> entriesListbox = new Listbox.CLASS<Listbox>(this);
	protected Listbox.CLASS<Listbox> rolesListbox = new Listbox.CLASS<Listbox>(this);

	protected UserEntries.CLASS<UserEntries> userEntries = new UserEntries.CLASS<UserEntries>(this);
	protected UniqueUserRoles.CLASS<UniqueUserRoles> userRoles = new UniqueUserRoles.CLASS<UniqueUserRoles>(this);

	protected Section.CLASS<Section> section2 = new Section.CLASS<Section>(this);
	protected Section.CLASS<Section> section3 = new Section.CLASS<Section>(this);

	protected ResetPasswordAction.CLASS<ResetPasswordAction> resetPassword = new ResetPasswordAction.CLASS<ResetPasswordAction>(this);

	public UserForm(IObject container) {
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

		Source.CLASS<Source> rolesSource = new Source.CLASS<Source>(rolesListbox.get());
		rolesSource.get().query = new RoleTableAccessView.CLASS<RoleTableAccessView>(this);

		rolesListbox.get().query = this.userRoles;
		rolesListbox.get().link = this.userRoles.get().user;
		rolesListbox.get().source = rolesSource;
		rolesListbox.get().flex = new integer(1);

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

	

	public static class UniqueUserRoles extends UserRoles
	{
		public static class CLASS<T extends UserForm.UniqueUserRoles> extends UserRoles.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(UserForm.UniqueUserRoles.class);
			}

			public Object newObject(IObject container) {
				return new UserForm.UniqueUserRoles(container);
			}
		}

		public static class UniqueRoles extends Roles
		{
			public static class CLASS<T extends UserForm.UniqueUserRoles.UniqueRoles> extends Roles.CLASS<T> {
				public CLASS(IObject container) {
					super(container);
					setJavaClass(UserForm.UniqueUserRoles.UniqueRoles.class);
				}

				public Object newObject(IObject container) {
					return new UserForm.UniqueUserRoles.UniqueRoles(container);
				}
			}

			public UniqueRoles(IObject container) {
				super(container);
			}

			public void constructor1() {
			}

			public void initMembers() {
				super.initMembers();
			}

			public void constructor2() {
				super.constructor2();
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void z8_beforeRead() {
				super.z8_beforeRead();
				UserRoles.CLASS<? extends UserRoles> ролиПользователя = new UserRoles.CLASS<UserRoles>(this);
				ролиПользователя.get().role.get().aggregation = Aggregation.Array;
				if(ролиПользователя.get().z8_aggregate(new RCollection(new Object[]{ролиПользователя.get().role}), ролиПользователя.get().user.get().sql_guid().operatorEqu(((UserForm.CLASS<UserForm>)getContainer().getContainer().getCLASS()).get().users.get().recordId.get().z8_get().sql_guid())).get())
					z8_addWhere(Sql.z8_inVector(recordId.get().sql_guid(), ролиПользователя.get().role.get().z8_array()).operatorNot());
			}
		};

		public UniqueUserRoles(IObject container) {
			super(container);
			roles = new UniqueRoles.CLASS<UniqueRoles>(this);
		}

		public void constructor1() {
			role.get(IClass.Constructor).operatorAssign(roles);
			user.get(IClass.Constructor).operatorAssign(users);
		}

		public void initMembers() {
			super.initMembers();
		}

		public void constructor2() {
			super.constructor2();
			columns.add(roles.get().name);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void z8_beforeCreate(guid recordId) {
			super.z8_beforeCreate(recordId);
			UserRoles.CLASS<? extends UserRoles> ролиПользователя = new UserRoles.CLASS<UserRoles>(this);
			if(ролиПользователя.get().z8_readFirst(new RCollection(new Object[]{}), ролиПользователя.get().user.get().sql_guid().operatorEqu(user.get().z8_get().sql_guid()).operatorAnd(ролиПользователя.get().role.get().sql_guid().operatorEqu(role.get().z8_get().sql_guid()))).get())
				throw new string("This role has already been added").exception();
		}
	};
}
