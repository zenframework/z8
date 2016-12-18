package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.integer;

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

	public integer height;

	public Query.CLASS<? extends Query> query = null;
	public Link.CLASS<? extends Link> link = null;
	public RCollection<Field.CLASS<? extends Field>> sortFields = new RCollection<Field.CLASS<? extends Field>>();

	public Listbox(IObject container) {
		super(container);
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query) {
		if(query == null)
			throw new RuntimeException("Listbox.query is null : displayName: '"  + displayName() + "'");

		if(link == null)
			throw new RuntimeException("Listbox.link is null : displayName: '"  + displayName() + "'");

		this.query.setContainer(null);
		query = this.query.get();

		super.writeMeta(writer, query);

		writer.writeProperty(Json.isListbox, true);
		writer.writeProperty(Json.header, displayName());
		writer.writeProperty(Json.icon, icon());

		writer.writeProperty(Json.height, height, new integer(300));

		writer.startObject(Json.query);
		writer.writeProperty(Json.id, query.classId());
		writer.writeProperty(Json.primaryKey, query.primaryKey().id());
		writer.writeProperty(Json.text, query.displayName());
		writer.writeProperty(Json.link, link.id());
		writer.writeControls(Json.fields, query.getColumns(), query);
		writer.writeSort(CLASS.asList(sortFields));
		writer.finishObject();
	}
}
