package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;

public class Fieldset extends Section {
	public static class CLASS<T extends Fieldset> extends Section.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Fieldset.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Fieldset(container);
		}
	}

	public Fieldset(IObject container) {
		super(container);
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		super.writeMeta(writer, query, context);

		writer.writeProperty(Json.isFieldset, true);
		writer.writeProperty(Json.legend, displayName());
		writer.writeProperty(Json.icon, icon());
	}
}
