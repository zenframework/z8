package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;

public class Listbox extends Control {
	public static class CLASS<T extends Listbox> extends Control.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Listbox.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Listbox(container);
		}
	}

	public Query.CLASS<? extends Query> query = null;
	public Link.CLASS<? extends Link> link = null;

	public RCollection<Field.CLASS<? extends Field>> columns = new RCollection<Field.CLASS<? extends Field>>();
	public RCollection<Field.CLASS<? extends Field>> sortFields = new RCollection<Field.CLASS<? extends Field>>();
	public RCollection<Action.CLASS<? extends Action>> actions = new RCollection<Action.CLASS<? extends Action>>();

	public Listbox(IObject container) {
		super(container);
		this.editable = bool.True;
	}

	public boolean readOnly() {
		return super.readOnly() || query.get().readOnly();
	}

	@Override
	public void writeMeta(JsonWriter writer, Query requestQuery, Query context) {
		if(this.query == null)
			throw new RuntimeException("Listbox.query is null : displayName: '"  + displayName() + "'");

		if(link == null && dependency == null)
			throw new RuntimeException("Both Listbox.link and Control.dependency are null: '"  + displayName() + "'");

		String requestId = requestQuery.classId();
		Query query = this.query.get();

		super.writeMeta(writer, query, context);

		writer.writeProperty(Json.isListbox, true);
		writer.writeProperty(Json.header, displayName());
		writer.writeProperty(Json.icon, icon());

		writer.startObject(Json.query);

		writer.writeProperty(Json.id, requestId);
		writer.writeProperty(Json.name, query.id());

		writer.writeProperty(Json.primaryKey, query.primaryKey().id());

		if(!this.readOnly())
			writer.writeProperty(Json.lockKey, query.lockKey().id());

		Field parentKey = query.parentKey();
		if(parentKey != null)
			writer.writeProperty(Json.parentKey, parentKey.id());

		writer.writeProperty(Json.totals, query.totals);
		writer.writeProperty(Json.text, query.displayName());

		writer.writeControls(Json.fields, query.fields(), query, context);
		writer.writeControls(Json.columns, columns.isEmpty() ? query.columns() : CLASS.asList(columns), query, context);
		writer.writeSort(sortFields.isEmpty() ? query.sortFields() : CLASS.asList(sortFields));
		writer.writeActions(CLASS.asList(actions)); 

		if(link != null) {
			writer.startObject(Json.link);

			Link link = this.link.get();
			link.writeMeta(writer, query, context);

			if(link.query().instanceOf(TreeTable.class)) {
				writer.startArray(Json.parentKeys);
				for(Field parent : link.getQuery().parentKeys())
					writer.write(parent.id());
				writer.finishArray();
			}

			writer.finishObject();
		}

		writer.finishObject();
	}
}
