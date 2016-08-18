package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.string;

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

	public string header = new string();

	public Fieldset(IObject container) {
		super(container);
	}

	@Override
	public void writeMeta(JsonWriter writer) {
		super.writeMeta(writer);

		writer.writeProperty(Json.isFieldset, true);
		writer.writeProperty(Json.header, header);
	}
}
