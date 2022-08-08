package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.runtime.IObject;
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

	public UserForm.CLASS<? extends UserForm> form = new UserForm.CLASS<UserForm>(this);

	public UsersView(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(form);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void constructor2() {
		super.constructor2();

		form.setIndex("form");
		form.get().users = (UsersView.CLASS<UsersView>) this.getCLASS();
		form.get().flex = new integer(1);

		registerControl(form);

		colCount = new integer(1);

		sortFields.add(name);

		names.add(name);
		names.add(lastName);

		quickFilters.add(name);
		quickFilters.add(lastName);
	}
}
