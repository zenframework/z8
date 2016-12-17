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

	public static class __Fields extends Fields {
		public static class CLASS<T extends TablesView.__Fields> extends Fields.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(TablesView.__Fields.class);
			}

			public Object newObject(IObject container) {
				return new TablesView.__Fields(container);
			}
		}

		public __Fields(IObject container) {
			super(container);
		}

		public void constructor2() {
			super.constructor2();

			description.get().colspan = new integer(4);

			registerFormField(name);
			registerFormField(type);
			registerFormField(displayName);
			registerFormField(id);
			registerFormField(description);
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	public void constructor2() {
		super.constructor2();

		readOnly = bool.True;
		columns = new integer(6);

		fields.setIndex("fields");
		fields.setDisplayName(Fields.displayNames.Title);

		__Fields fieldsTable = new __Fields.CLASS<__Fields>(this).get();

		fields.get().query = (Query.CLASS<Query>)fieldsTable.getCLASS();
		fields.get().link = fieldsTable.table;
		fields.get().height = new integer(600);

		fieldsTable.gridFields.add(fieldsTable.name);
		fieldsTable.gridFields.add(fieldsTable.type);
		fieldsTable.gridFields.add(fieldsTable.displayName);

		fieldsTable.sortFields.add(fieldsTable.position);

		id.get().colspan = new integer(2);

		name.get().colspan = new integer(1);
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
