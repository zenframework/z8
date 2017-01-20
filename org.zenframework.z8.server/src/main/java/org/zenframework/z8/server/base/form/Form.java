package org.zenframework.z8.server.base.form;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;

public class Form extends Section {
	public static class CLASS<T extends Form> extends Section.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Form.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Form(container);
		}
	}

	public Link.CLASS<? extends Link> link = null;
	public Query.CLASS<? extends Query> query = null;

	public Form(IObject container) {
		super(container);
	}

	public Collection<Field.CLASS<Field>> fields() {
		return link != null ? super.fields() : new LinkedHashSet<Field.CLASS<Field>>();
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		if(link == null && this.query == null)
			throw new RuntimeException("Both Form.link and Form.query are null : '" + displayName() + "'");

		writer.writeProperty(Json.isForm, true);

		if(this.link != null) {
			super.writeMeta(writer, link.get().getQuery(), query);

			writer.writeProperty(Json.name, link.id());

			writer.startObject(Json.link);
			link.get().writeMeta(writer, query, context);
			writer.finishObject();
		} else
			super.writeMeta(writer, this.query.get(), null);

		writer.startObject(Json.query);
		writer.writeProperty(Json.id, this.query != null ? this.query.classId() : query.classId());
		writer.finishObject();
	}
}
