package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;

public class Selector extends OBJECT {
	public static class CLASS<T extends Selector> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Selector.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Selector(container);
		}
	}

	public Link.CLASS<? extends Link> link = null;
	public RCollection<Field.CLASS<? extends Field>> columns = new RCollection<Field.CLASS<? extends Field>>();
	public RCollection<Field.CLASS<? extends Field>> sortFields = new RCollection<Field.CLASS<? extends Field>>();

	public bool copy = bool.False;
	public bool multiselect = bool.True;

	public RLinkedHashMap<Field.CLASS<? extends Field>, RCollection<Field.CLASS<? extends Field>>> inFields = new RLinkedHashMap<Field.CLASS<? extends Field>, RCollection<Field.CLASS<? extends Field>>>();
	public RLinkedHashMap<Field.CLASS<? extends Field>, RCollection<Field.CLASS<? extends Field>>> outFields = new RLinkedHashMap<Field.CLASS<? extends Field>, RCollection<Field.CLASS<? extends Field>>>();

	public Selector(IObject container) {
		super(container);
	}

	public void writeMeta(JsonWriter writer, Query requestQuery, Query context) {
		writer.writeProperty(Json.isSelector, true);
		writer.writeProperty(Json.header, displayName());
		writer.writeProperty(Json.icon, icon());

		writer.writeProperty(Json.copy, copy);
		writer.writeProperty(Json.multiselect, multiselect);

		if(link != null) {
			writer.startObject(Json.link);
			link.get().writeMeta(writer, requestQuery, context);
			writer.finishObject();
		}

		writer.writeControls(Json.columns, CLASS.asList(this.columns), requestQuery, context);
		writer.writeSort(CLASS.asList(sortFields));
	}
}
