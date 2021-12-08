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
			setAttribute(SystemTool, Integer.toString(400));
		}

		@Override
		public Object newObject(IObject container) {
			return new TablesView(container);
		}
	}

	public Listbox.CLASS<Listbox> fieldsListbox = new Listbox.CLASS<Listbox>(this);
	private Fields.CLASS<Fields> fields = new Fields.CLASS<Fields>(this);

	public TablesView(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();
		objects.add(this.fields);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		fields.setIndex("fields");

		readOnly = bool.True;
		colCount = new integer(6);

		fieldsListbox.setIndex("fieldsListbox");
		fieldsListbox.setDisplayName(Fields.displayNames.Title);

		Fields fields = this.fields.get();
		fields.readOnly = bool.True;

		fieldsListbox.get().query = this.fields;
		fieldsListbox.get().link = fields.tableId;
		fieldsListbox.get().sortFields.add(fields.position);

		fields.columns.add(fields.name);
		fields.columns.add(fields.type);
		fields.columns.add(fields.displayName);

		classId.get().colSpan = new integer(2);

		name.get().colSpan = new integer(2);
		name.get().width = new integer(100);

		displayName.get().colSpan = new integer(2);
		displayName.get().width = new integer(200);

		description.get().colSpan = new integer(6);
		description.get().height = new integer(2);

		fieldsListbox.get().colSpan = new integer(6);
		fieldsListbox.get().flex = new integer(1);

		registerControl(classId);
		registerControl(name);
		registerControl(displayName);
		registerControl(description);
		registerControl(fieldsListbox);

		names.add(displayName);
		names.add(name);
		sortFields.add(name);
	}
}
