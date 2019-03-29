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

//	public Link.CLASS<? extends Link> link = null;

	private Query query = null;
	private boolean isDependent = false;

	public Form(IObject container) {
		super(container);
	}

	private Collection<Field> allFields() {
		Collection<Field> fields = super.fields();
		if(!isDependent && dependency != null)
			fields.add(dependency.get().owner().primaryKey());
		return fields;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<Field> fields() {
		if(isDependent) {
			for(Control.CLASS<? extends Control> cls : dependencies) {
				if(cls.instanceOf(Form.class)) {
					Form form = (Form)cls.get();
					form.setQuery(getQuery());
				}
			}
		}
		return isDependent ? Collections.EMPTY_LIST : allFields();
	}

	@SuppressWarnings("unchecked")
	public void setQuery(Query query) {
		if(query == null)
			return;

		if(dependency == null)
			this.query = query;

		isDependent = true;

		for(Field field : allFields())
			query.extraFields.add((Field.CLASS<? extends Field>)field.getCLASS());
	}

	private Link getLink() {
		return dependency != null ? (Link)dependency.get() : null;
	}

	private Query getQuery() {
		Link link = getLink();
		return link != null ? link.getQuery() : (query != null ? query : null);
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		writer.writeProperty(Json.isForm, true);

		Link link = getLink();
		Query formQuery = getQuery();

		if(formQuery != null) {
			writer.startObject(Json.link);
			if(link != null) {
				writer.writeProperty(Json.name, link.id());
				writer.writeProperty(Json.primaryKey, link.owner().primaryKey().id());
				writer.writeProperty(Json.owner, link.owner().id());
			} else
				writer.writeProperty(Json.owner, formQuery.id());

			writer.startObject(Json.query);
			writer.writeProperty(Json.primaryKey, formQuery.primaryKey().id());
			writer.finishObject();

			writer.finishObject();
		}

		super.writeMeta(writer, formQuery != null ? formQuery : query, context);
	}
}
