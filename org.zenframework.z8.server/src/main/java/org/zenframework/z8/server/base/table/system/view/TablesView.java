package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
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

			gridFields.add(name);
			gridFields.add(type);
			gridFields.add(displayName);

			sortFields.add(position);
		}
	};

	@Override
	public void constructor2() {
		super.constructor2();

		readOnly = new bool(true);
		columns = new integer(6);

		fields.setIndex("fields");
		fields.setDisplayName(Fields.displayNames.Title);

		__Fields.CLASS<__Fields> fieldsCls = new __Fields.CLASS<__Fields>(this);

		fields.get().query = fieldsCls;
		fields.get().link = fieldsCls.get().table;
		fields.get().height = new integer(600);

		id.get().colspan = new integer(2);
		name.get().colspan = new integer(1);
		displayName.get().colspan = new integer(2);

		description.get().colspan = new integer(6);

		fields.get().colspan = new integer(6);

		registerFormField(id);
		registerFormField(name);
		registerFormField(displayName);
		registerFormField(description);
		registerFormField(fields);

		nameFields.add(name);
		sortFields.add(name);
	}
}
