package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.RoleFieldAccess;
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

		__RoleTableAccess roleTable = new __RoleTableAccess.CLASS<__RoleTableAccess>(this).get();

		tables.get().query = (Query.CLASS<Query>)roleTable.getCLASS();
		tables.get().link = roleTable.role;
		tables.get().height = new integer(500);
		tables.get().sortFields.add(roleTable.tables.get().name);

		roleTable.tables.get().name.get().width = new integer(150);
		roleTable.tables.get().displayName.get().width = new integer(150);

		roleTable.read.get().width = new integer(30);
		roleTable.read.setIcon("fa-eye");

		roleTable.write.get().width = new integer(30);
		roleTable.write.setIcon("fa-pencil");

		roleTable.create.get().width = new integer(30);
		roleTable.create.setIcon("fa-file-o");

		roleTable.copy.get().width = new integer(30);
		roleTable.copy.setIcon("fa-copy");

		roleTable.destroy.get().width = new integer(30);
		roleTable.destroy.setIcon("fa-trash");

		roleTable.gridFields.add(roleTable.tables.get().name);
		roleTable.gridFields.add(roleTable.tables.get().displayName);
		roleTable.gridFields.add(roleTable.read);
		roleTable.gridFields.add(roleTable.write);
		roleTable.gridFields.add(roleTable.create);
		roleTable.gridFields.add(roleTable.copy);
		roleTable.gridFields.add(roleTable.destroy);

		__RoleFieldAccess roleField = new __RoleFieldAccess.CLASS<__RoleFieldAccess>(this).get();

		fields.setIndex("fields");
		fields.setDisplayName(Fields.displayNames.Title);
		fields.get().query = (Query.CLASS<Query>)roleField.getCLASS();
		fields.get().link = roleField.role;
		fields.get().height = new integer(500);
		fields.get().sortFields.add(roleField.fields.get().name);

		roleField.fields.get().name.get().width = new integer(150);
		roleField.fields.get().displayName.get().width = new integer(150);
		roleField.read.get().width = new integer(30);
		roleField.write.get().width = new integer(30);

		roleField.gridFields.add(roleField.fields.get().name);
		roleField.gridFields.add(roleField.fields.get().displayName);
		roleField.gridFields.add(roleField.read);
		roleField.gridFields.add(roleField.write);

		tables.get().dependencies.add(fields);
		fields.get().dependsOn = roleField.fields.get().table;

		registerFormField(name);
		registerFormField(tables);
		registerFormField(fields);

		sortFields.add(name);
	}
}
