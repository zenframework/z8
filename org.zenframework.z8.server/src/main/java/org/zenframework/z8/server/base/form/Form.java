package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;

public class Form extends Fieldset {
	public static class CLASS<T extends Form> extends Fieldset.CLASS<T> {
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

	public Form(IObject container) {
		super(container);
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query) {
		if(link == null)
			throw new RuntimeException("Form.link is null : '" + displayName() + "'");

		Link link = this.link.get();

		super.writeMeta(writer, link.getQuery());

		writer.writeProperty(Json.name, link.id());
		writer.writeProperty(Json.isForm, true);

		writer.startObject(Json.link);
		link.writeMeta(writer, query);
		writer.finishObject();
	}
}
