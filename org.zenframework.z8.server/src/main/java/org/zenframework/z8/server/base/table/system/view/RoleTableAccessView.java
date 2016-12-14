package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.RoleFieldAccess;
import org.zenframework.z8.server.base.table.system.RoleTableAccess;
import org.zenframework.z8.server.base.table.system.Roles;
import org.zenframework.z8.server.base.table.value.Join;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;

public class RoleTableAccessView extends Roles {
	public static class CLASS<T extends RoleTableAccessView> extends Roles.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(RoleTableAccessView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new RoleTableAccessView(container);
		}
	}

	public static class __RoleTableAccess extends RoleTableAccess {
		public static class CLASS<T extends __RoleTableAccess> extends RoleTableAccess.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(__RoleTableAccess.class);
			}

			public Object newObject(IObject container) {
				return new __RoleTableAccess(container);
			}
		}

		public __RoleTableAccess(IObject container) {
			super(container);
		}

		public void constructor2() {
			super.constructor2();
			table.get().join = Join.Right;
			role.get().setRightJoined(true);
		}
	};

	public static class __RoleFieldAccess extends RoleFieldAccess {
		public static class CLASS<T extends __RoleFieldAccess> extends RoleFieldAccess.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(__RoleFieldAccess.class);
			}

			public Object newObject(IObject container) {
				return new __RoleFieldAccess(container);
			}
		}

		public __RoleFieldAccess(IObject container) {
			super(container);
		}

		public void constructor2() {
			super.constructor2();
			field.get().join = Join.Right;
			role.get().setRightJoined(true);

		}
	};

	public Listbox.CLASS<Listbox> tables = new Listbox.CLASS<Listbox>(this);
	public Listbox.CLASS<Listbox> fields = new Listbox.CLASS<Listbox>(this);

	public RoleTableAccessView(IObject container) {
		super(container);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void constructor2() {
		super.constructor2();

		columns = new integer(2);

		name.get().colspan = new integer(2);

		tables.setIndex("tables");
		tables.setDisplayName(RoleTableAccess.displayNames.Title);

		__RoleTableAccess rta = new __RoleTableAccess.CLASS<__RoleTableAccess>(this).get();

		tables.get().query = (Query.CLASS<Query>)rta.getCLASS();
		tables.get().link = rta.role;
		tables.get().height = new integer(500);
		tables.get().sortFields.add(rta.tables.get().name);

		rta.tables.get().name.get().width = new integer(150);
		rta.tables.get().displayName.get().width = new integer(150);

		rta.read.get().width = new integer(30);
		rta.read.setIcon("fa-eye");

		rta.write.get().width = new integer(30);
		rta.write.setIcon("fa-pencil");

		rta.create.get().width = new integer(30);
		rta.create.setIcon("fa-file-o");

		rta.copy.get().width = new integer(30);
		rta.copy.setIcon("fa-copy");

		rta.destroy.get().width = new integer(30);
		rta.destroy.setIcon("fa-trash");

		rta.gridFields.add(rta.tables.get().name);
		rta.gridFields.add(rta.tables.get().displayName);
		rta.gridFields.add(rta.read);
		rta.gridFields.add(rta.write);
		rta.gridFields.add(rta.create);
		rta.gridFields.add(rta.copy);
		rta.gridFields.add(rta.destroy);

		__RoleFieldAccess rfa = new __RoleFieldAccess.CLASS<__RoleFieldAccess>(this).get();

		fields.setIndex("fields");
		fields.setDisplayName(Fields.displayNames.Title);
		fields.get().query = (Query.CLASS<Query>)rfa.getCLASS();
		fields.get().link = rfa.role;
		fields.get().height = new integer(500);
		fields.get().sortFields.add(rfa.fields.get().name);

		rfa.fields.get().name.get().width = new integer(150);
		rfa.fields.get().displayName.get().width = new integer(150);

		rfa.read.get().width = new integer(30);
		rfa.read.setIcon("fa-eye");

		rfa.write.get().width = new integer(30);
		rfa.write.setIcon("fa-pencil");

		rfa.gridFields.add(rfa.fields.get().name);
		rfa.gridFields.add(rfa.fields.get().displayName);
		rfa.gridFields.add(rfa.read);
		rfa.gridFields.add(rfa.write);

		tables.get().dependencies.add(fields);
		fields.get().dependency = rfa.fields.get().table;
		fields.get().dependsOn = rta.table;

		registerFormField(name);
		registerFormField(tables);
		registerFormField(fields);

		sortFields.add(name);
	}
}
