package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class Source extends OBJECT {
	public static class CLASS<T extends Source> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Source.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Source(container);
		}
	}

	public Query.CLASS<? extends Query> query = null;
	public Field.CLASS<? extends Field> filterField = null;

	public Source(IObject container) {
		super(container);
	}

	public void writeMeta(JsonWriter writer, Query requestQuery, Query context) {
		if (query != null) {
			writer.writeProperty(Json.request, query.classId());
			writer.writeProperty(Json.text, query.displayName());
			if (filterField != null)
				writer.writeProperty(Json.field, filterField.id().substring(query.id().length() + 1));
		}
		IObject container = getContainer();
		if (container instanceof Field) {
			Field field = (Field) container;
			if (field.getPath() != null && !field.getPath().isEmpty()) {
				ILink[] links = field.getPath().toArray(new ILink[field.getPath().size()]);
				ILink lastLink = links[links.length - 1];
				writer.writeProperty(Json.link, lastLink.id());
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void operatorAssign(Query.CLASS<? extends Query> data) {
		query = (Query.CLASS)data;
	}

}
