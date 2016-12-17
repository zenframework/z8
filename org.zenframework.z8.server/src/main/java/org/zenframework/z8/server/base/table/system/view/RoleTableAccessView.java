package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.RoleFieldAccess;
import org.zenframework.z8.server.base.table.system.RoleTableAccess;
import org.zenframework.z8.server.base.table.system.Roles;
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

	public Listbox.CLASS<Listbox> tables = new Listbox.CLASS<Listbox>(this);
	public Listbox.CLASS<Listbox> fields = new Listbox.CLASS<Listbox>(this);

	public RoleTableAccessView(IObject container) {
		super(container);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void constructor2() {
		super.constructor2();

		columnCount = new integer(12);

		name.get().colspan = new integer(12);

		read.get().colspan = new integer(2);
		write.get().colspan = new integer(2);
		create.get().colspan = new integer(2);
		copy.get().colspan = new integer(2);
		destroy.get().colspan = new integer(2);
		execute.get().colspan = new integer(2);

		tables.setIndex("tables");
		tables.setDisplayName(RoleTableAccess.displayNames.Title);

		RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>(this).get();

		tables.get().query = (Query.CLASS<Query>)rta.getCLASS();
		tables.get().link = rta.role;
		tables.get().colspan = new integer(6);
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

		rta.columns.add(rta.tables.get().name);
		rta.columns.add(rta.tables.get().displayName);
		rta.columns.add(rta.read);
		rta.columns.add(rta.write);
		rta.columns.add(rta.create);
		rta.columns.add(rta.copy);
		rta.columns.add(rta.destroy);

		RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>(this).get();

		fields.setIndex("fields");
		fields.setDisplayName(Fields.displayNames.Title);
		fields.get().query = (Query.CLASS<Query>)rfa.getCLASS();
		fields.get().link = rfa.role;
		fields.get().colspan = new integer(6);
		fields.get().height = new integer(500);
		fields.get().sortFields.add(rfa.fields.get().position);

		rfa.fields.get().name.get().width = new integer(150);
		rfa.fields.get().displayName.get().width = new integer(150);
		rfa.fields.get().type.get().width = new integer(90);

		rfa.read.get().width = new integer(30);
		rfa.read.setIcon("fa-eye");

		rfa.write.get().width = new integer(30);
		rfa.write.setIcon("fa-pencil");

		rfa.columns.add(rfa.fields.get().name);
		rfa.columns.add(rfa.fields.get().displayName);
		rfa.columns.add(rfa.fields.get().type);
		rfa.columns.add(rfa.read);
		rfa.columns.add(rfa.write);

		tables.get().dependencies.add(fields);
		fields.get().dependency = rfa.fields.get().table;
		fields.get().dependsOn = rta.table;

		registerFormField(name);

		registerFormField(read);
		registerFormField(write);
		registerFormField(create);
		registerFormField(copy);
		registerFormField(destroy);
		registerFormField(execute);

		registerFormField(tables);
		registerFormField(fields);

		sortFields.add(name);
	}
}
