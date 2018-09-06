package org.zenframework.z8.server.base.form;

import java.util.Collection;
import java.util.Collections;

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

	@Override
	@SuppressWarnings("unchecked")
	public Collection<Field> fields() {
		if(link == null)
			return Collections.EMPTY_LIST;

		Collection<Field> fields = super.fields();
		fields.add(link.get().owner().primaryKey());
		return fields;
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		writer.writeProperty(Json.isForm, true);

		Link link = this.link != null ? this.link.get() : null; 

		if(link != null) {
			writer.startObject(Json.link);
			writer.writeProperty(Json.primaryKey, link.owner().primaryKey().id());
			link.writeMeta(writer, query, context);
			writer.finishObject();
		}

		super.writeMeta(writer, link != null ? link.getQuery() : this.query != null ? this.query.get() : query, context);

		writer.startObject(Json.query);
		writer.writeProperty(Json.id, this.query != null ? this.query.id() : query.id());
		writer.finishObject();
	}
}
