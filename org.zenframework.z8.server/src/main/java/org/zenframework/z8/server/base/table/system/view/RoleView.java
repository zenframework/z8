package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.form.Tab;
import org.zenframework.z8.server.base.form.TabControl;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.RoleFieldAccess;
import org.zenframework.z8.server.base.table.system.RoleRequestAccess;
import org.zenframework.z8.server.base.table.system.RoleSecuredObjectAccess;
import org.zenframework.z8.server.base.table.system.RoleTableAccess;
import org.zenframework.z8.server.base.table.system.Roles;
import org.zenframework.z8.server.base.table.system.SecuredObjectAccess;
import org.zenframework.z8.server.base.table.system.SecuredObjects;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class RoleView extends Roles {
	public static class CLASS<T extends RoleView> extends Roles.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(RoleView.class);
			setAttribute(SystemTool, Integer.toString(200));
		}

		@Override
		public Object newObject(IObject container) {
			return new RoleView(container);
		}
	}

	public Listbox.CLASS<Listbox> tables = new Listbox.CLASS<Listbox>(this);
	public Listbox.CLASS<Listbox> fields = new Listbox.CLASS<Listbox>(this);
	public Tab.CLASS<Tab> databaseTab = new Tab.CLASS<Tab>(this);

	public Listbox.CLASS<Listbox> requests = new Listbox.CLASS<Listbox>(this);
	public Tab.CLASS<Tab> requestTab = new Tab.CLASS<Tab>(this);

	public Listbox.CLASS<Listbox> securedObjects = new Listbox.CLASS<Listbox>(this);
	public Listbox.CLASS<Listbox> securedObjectAccess = new Listbox.CLASS<Listbox>(this);
	public Tab.CLASS<Tab> securedObjectTab = new Tab.CLASS<Tab>(this);

	public TabControl.CLASS<TabControl> tabs = new TabControl.CLASS<TabControl>(this);

	private RoleTableAccess.CLASS<RoleTableAccess> rta = new RoleTableAccess.CLASS<RoleTableAccess>(this);
	private RoleFieldAccess.CLASS<RoleFieldAccess> rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>(this);
	private RoleRequestAccess.CLASS<RoleRequestAccess> rra = new RoleRequestAccess.CLASS<RoleRequestAccess>(this);
	private RoleSecuredObjectAccess.CLASS<RoleSecuredObjectAccess> rso = new RoleSecuredObjectAccess.CLASS<RoleSecuredObjectAccess>(this);
	private RoleSecuredObjectAccess.CLASS<RoleSecuredObjectAccess> rsoa = new RoleSecuredObjectAccess.CLASS<RoleSecuredObjectAccess>(this);

	public RoleView(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(rta);
		objects.add(rfa);
		objects.add(rra);
		objects.add(rso);
		objects.add(rsoa);

		objects.add(tables);
		objects.add(fields);
		objects.add(databaseTab);

		objects.add(requests);
		objects.add(requestTab);

		objects.add(securedObjects);
		objects.add(securedObjectAccess);
		objects.add(securedObjectTab);

		objects.add(tabs);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		rta.setIndex("rta");
		rfa.setIndex("rfa");
		rra.setIndex("rra");
		rso.setIndex("rso");
		rsoa.setIndex("rsoa");

		colCount = new integer(1);

		/* Tables */
		RoleTableAccess rta = this.rta.get();

		tables.setIndex("tables");
		tables.setDisplayName(RoleTableAccess.displayNames.Title);

		tables.get().query = this.rta;
		tables.get().link = rta.roleId;
		tables.get().flex = new integer(1);
		tables.get().sortFields.add(rta.table.get().name);

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

		rta.columns.add(rta.table.get().name);
		rta.columns.add(rta.table.get().displayName);
		rta.columns.add(rta.read);
		rta.columns.add(rta.write);
		rta.columns.add(rta.create);
		rta.columns.add(rta.copy);
		rta.columns.add(rta.destroy);
		/* Tables */

		/* Fields */
		RoleFieldAccess rfa = this.rfa.get();

		fields.setIndex("fields");
		fields.setDisplayName(Fields.displayNames.Title);
		fields.get().query = this.rfa;
		fields.get().link = rfa.roleId;
		fields.get().flex = new integer(1);
		fields.get().sortFields.add(rfa.field.get().position);

		rfa.field.get().name.get().width = new integer(150);
		rfa.field.get().displayName.get().width = new integer(150);
		rfa.field.get().type.get().width = new integer(90);

		rfa.read.get().editable = bool.True;
		rfa.read.setIcon("fa-eye");

		rfa.write.get().editable = bool.True;
		rfa.write.setIcon("fa-pencil");

		rfa.columns.add(rfa.field.get().name);
		rfa.columns.add(rfa.field.get().displayName);
		rfa.columns.add(rfa.field.get().type);
		rfa.columns.add(rfa.read);
		rfa.columns.add(rfa.write);

		tables.get().dependencies.add(fields);
		fields.get().dependency = rfa.field.get().tableId;
		fields.get().dependsOn = rta.tableId;
		/* Fields */

		databaseTab.setIndex("databaseTab");
		databaseTab.setDisplayName(RoleTableAccess.displayNames.Title);
		databaseTab.get().colCount = new integer(2);
		databaseTab.get().controls.add(tables);
		databaseTab.get().controls.add(fields);

		/* Requests */
		RoleRequestAccess rra = this.rra.get();

		requests.setIndex("requests");
		requests.setDisplayName(RoleRequestAccess.displayNames.Title);

		requests.get().query = this.rra;
		requests.get().link = rra.roleId;
		requests.get().flex = new integer(1);
		requests.get().sortFields.add(rra.request.get().name);

		rra.execute.get().editable = bool.True;
		rra.execute.setIcon("fa-eye");

		rra.columns.add(rra.request.get().name);
		rra.columns.add(rra.request.get().classId);
		rra.columns.add(rra.execute);
		/* Requests */

		requestTab.setIndex("requestTab");
		requestTab.get().controls.add(requests);
		requestTab.setDisplayName(RoleRequestAccess.displayNames.Title);

		/* SecuredObjects */
		RoleSecuredObjectAccess rso = this.rso.get();
		rso.groupBy.add(rso.soa.get().securedObjectId);

		securedObjects.setIndex("securedObjects");
		securedObjects.setDisplayName(SecuredObjects.displayNames.Title);

		securedObjects.get().query = this.rso;
		securedObjects.get().link = rso.roleId;
		securedObjects.get().flex = new integer(1);
		securedObjects.get().sortFields.add(rso.soa.get().securedObject.get().name);

		securedObjects.get().columns.add(rso.soa.get().securedObject.get().name);
		/* SecuredObjects */

		/* SecuredObjectAccess */
		RoleSecuredObjectAccess rsoa = this.rsoa.get();

		securedObjectAccess.setIndex("securedObjectAccess");
		securedObjectAccess.setDisplayName(SecuredObjectAccess.displayNames.Title);

		securedObjectAccess.get().query = this.rsoa;
		securedObjectAccess.get().link = rsoa.roleId;
		securedObjectAccess.get().flex = new integer(1);
		securedObjectAccess.get().sortFields.add(rsoa.soa.get().name);

		rsoa.value.get().editable = bool.True;
		rsoa.value.setIcon("fa-check");

		securedObjectAccess.get().columns.add(rsoa.soa.get().name);
		securedObjectAccess.get().columns.add(rsoa.value);

		securedObjects.get().dependencies.add(securedObjectAccess);
		securedObjectAccess.get().dependency = rsoa.soa.get().securedObjectId;
		securedObjectAccess.get().dependsOn = rso.soa.get().securedObjectId;
		/* SecuredObjectAccess */

		securedObjectTab.setIndex("securedObjectTab");
		securedObjectTab.get().colCount = new integer(2);
		securedObjectTab.get().controls.add(securedObjects);
		securedObjectTab.get().controls.add(securedObjectAccess);
		securedObjectTab.setDisplayName(SecuredObjects.displayNames.Title);

		tabs.setIndex("tabs");
		tabs.get().tabs.add(securedObjectTab);
		tabs.get().tabs.add(databaseTab);
		tabs.get().tabs.add(requestTab);
		tabs.get().flex = new integer(1);

		registerControl(name);
		registerControl(tabs);

		sortFields.add(name);
	}
}
