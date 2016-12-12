package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.table.system.RoleTableAccess;
import org.zenframework.z8.server.base.table.system.SecurityGroups;
import org.zenframework.z8.server.base.table.value.Join;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;

public class RoleTableAccessView extends SecurityGroups {
	public static class CLASS<T extends RoleTableAccessView> extends SecurityGroups.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(RoleTableAccessView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new RoleTableAccessView(container);
		}
	}

	public Listbox.CLASS<Listbox> tables = new Listbox.CLASS<Listbox>(this);

	public RoleTableAccessView(IObject container) {
		super(container);
	}

	public static class __RoleTableAccess extends RoleTableAccess {
		public static class CLASS<T extends RoleTableAccessView.__RoleTableAccess> extends RoleTableAccess.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(RoleTableAccessView.__RoleTableAccess.class);
			}

			public Object newObject(IObject container) {
				return new RoleTableAccessView.__RoleTableAccess(container);
			}
		}

		public __RoleTableAccess(IObject container) {
			super(container);
		}

		public void constructor2() {
			super.constructor2();

			table.get().join = Join.Right;

			tables.get().name.get().width = new integer(150);
			tables.get().displayName.get().width = new integer(150);
			read.get().width = new integer(30);
			write.get().width = new integer(30);
			create.get().width = new integer(30);
			copy.get().width = new integer(30);
			destroy.get().width = new integer(30);

			gridFields.add(tables.get().name);
			gridFields.add(tables.get().displayName);
			gridFields.add(read);
			gridFields.add(write);
			gridFields.add(create);
			gridFields.add(copy);
			gridFields.add(destroy);
		}
	};

	@Override
	public void constructor2() {
		super.constructor2();

		columns = new integer(1);

		tables.setIndex("tables");
		tables.setDisplayName(RoleTableAccess.displayNames.Title);

		__RoleTableAccess.CLASS<__RoleTableAccess> tablesClass = new __RoleTableAccess.CLASS<__RoleTableAccess>(this);

		tables.get().query = tablesClass;
		tables.get().link = tablesClass.get().table;
		tables.get().height = new integer(700);
		tables.get().sortFields.add(tablesClass.get().tables.get().name);

		registerFormField(name);
		registerFormField(tables);

		sortFields.add(name);
	}
}
