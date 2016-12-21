package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.Tables;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class TablesView extends Tables {
	public static class CLASS<T extends TablesView> extends Tables.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TablesView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new TablesView(container);
		}
	}

	public Listbox.CLASS<Listbox> fields = new Listbox.CLASS<Listbox>(this);

	public TablesView(IObject container) {
		super(container);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void constructor2() {
		super.constructor2();

		readOnly = bool.True;
		columnCount = new integer(6);

		fields.setIndex("fields");
		fields.setDisplayName(Fields.displayNames.Title);

		Fields fieldsTable = new Fields.CLASS<Fields>(this).get();

		fields.get().query = (Query.CLASS<Query>)fieldsTable.getCLASS();
		fields.get().link = fieldsTable.table;
		fields.get().height = new integer(600);
		fields.get().sortFields.add(fieldsTable.position);

		fieldsTable.columns.add(fieldsTable.name);
		fieldsTable.columns.add(fieldsTable.type);
		fieldsTable.columns.add(fieldsTable.displayName);

		id.get().colspan = new integer(2);

		name.get().colspan = new integer(2);
		name.get().width = new integer(100);

		displayName.get().colspan = new integer(2);
		displayName.get().width = new integer(200);

		description.get().colspan = new integer(6);

		fields.get().colspan = new integer(6);

		registerFormField(id);
		registerFormField(name);
		registerFormField(displayName);
		registerFormField(description);
		registerFormField(fields);

		nameFields.add(displayName);
		nameFields.add(name);
		sortFields.add(name);
	}
}
