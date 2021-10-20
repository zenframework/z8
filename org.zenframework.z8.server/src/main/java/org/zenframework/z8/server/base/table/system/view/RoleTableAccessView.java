package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.RoleFieldAccess;
import org.zenframework.z8.server.base.table.system.RoleRequestAccess;
import org.zenframework.z8.server.base.table.system.RoleTableAccess;
import org.zenframework.z8.server.base.table.system.Roles;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class RoleTableAccessView extends Roles {
	public static class CLASS<T extends RoleTableAccessView> extends Roles.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(RoleTableAccessView.class);
			setAttribute(SystemTool, Integer.toString(200));
		}

		@Override
		public Object newObject(IObject container) {
			return new RoleTableAccessView(container);
		}
	}

	public Listbox.CLASS<Listbox> tablesListbox = new Listbox.CLASS<Listbox>(this);
	public Listbox.CLASS<Listbox> fieldsListbox = new Listbox.CLASS<Listbox>(this);
	public Listbox.CLASS<Listbox> requestsListbox = new Listbox.CLASS<Listbox>(this);

	private RoleTableAccess.CLASS<RoleTableAccess> rta = new RoleTableAccess.CLASS<RoleTableAccess>(this);
	private RoleFieldAccess.CLASS<RoleFieldAccess> rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>(this);
	private RoleRequestAccess.CLASS<RoleRequestAccess> rra = new RoleRequestAccess.CLASS<RoleRequestAccess>(this);

	public RoleTableAccessView(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(rta);
		objects.add(rfa);
		objects.add(rra);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		rta.setIndex("rta");
		rfa.setIndex("rfa");
		rra.setIndex("rra");

		colCount = new integer(2);

		name.get().colSpan = new integer(2);

		/* Tables */
		RoleTableAccess rta = this.rta.get();

		tablesListbox.setIndex("tablesListbox");
		tablesListbox.setDisplayName(RoleTableAccess.displayNames.Title);

		tablesListbox.get().query = this.rta;
		tablesListbox.get().link = rta.role;
		tablesListbox.get().flex = new integer(1);
		tablesListbox.get().sortFields.add(rta.tables.get().name);

		rta.read.get().editable = bool.True;
		rta.read.setIcon("fa-eye");

		rta.write.get().editable = bool.True;
		rta.write.setIcon("fa-pencil");

		rta.create.get().editable = bool.True;
		rta.create.setIcon("fa-file-o");

		rta.copy.get().editable = bool.True;
		rta.copy.setIcon("fa-copy");

		rta.destroy.get().editable = bool.True;
		rta.destroy.setIcon("fa-trash");

		rta.columns.add(rta.tables.get().name);
		rta.columns.add(rta.tables.get().displayName);
		rta.columns.add(rta.read);
		rta.columns.add(rta.write);
		rta.columns.add(rta.create);
		rta.columns.add(rta.copy);
		rta.columns.add(rta.destroy);
		/* Tables */

		/* Fields */
		RoleFieldAccess rfa = this.rfa.get();

		fieldsListbox.setIndex("fieldsListbox");
		fieldsListbox.setDisplayName(Fields.displayNames.Title);
		fieldsListbox.get().query = this.rfa;
		fieldsListbox.get().link = rfa.role;
		fieldsListbox.get().flex = new integer(1);
		fieldsListbox.get().sortFields.add(rfa.fields.get().position);

		rfa.fields.get().name.get().width = new integer(150);
		rfa.fields.get().displayName.get().width = new integer(150);
		rfa.fields.get().type.get().width = new integer(90);

		rfa.read.get().editable = bool.True;
		rfa.read.setIcon("fa-eye");

		rfa.write.get().editable = bool.True;
		rfa.write.setIcon("fa-pencil");

		rfa.columns.add(rfa.fields.get().name);
		rfa.columns.add(rfa.fields.get().displayName);
		rfa.columns.add(rfa.fields.get().type);
		rfa.columns.add(rfa.read);
		rfa.columns.add(rfa.write);

		tablesListbox.get().dependencies.add(fieldsListbox);
		fieldsListbox.get().dependency = rfa.fields.get().table;
		fieldsListbox.get().dependsOn = rta.table;
		/* Fields */

		/* Requests */
		RoleRequestAccess rra = this.rra.get();

		requestsListbox.setIndex("requestsListbox");
		requestsListbox.setDisplayName(RoleRequestAccess.displayNames.Title);

		requestsListbox.get().query = this.rra;
		requestsListbox.get().link = rra.role;
		requestsListbox.get().flex = new integer(1);
		requestsListbox.get().colSpan = new integer(2);
		requestsListbox.get().sortFields.add(rra.requests.get().name);

		rra.execute.get().editable = bool.True;
		rra.execute.setIcon("fa-eye");

		rra.columns.add(rra.requests.get().name);
		rra.columns.add(rra.requests.get().classId);
		rra.columns.add(rra.execute);
		/* Requests */

		registerControl(name);

		registerControl(tablesListbox);
		registerControl(fieldsListbox);
		registerControl(requestsListbox);

		sortFields.add(name);
	}
}
