package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;

public class ControlGroup extends Section {
	public static class CLASS<T extends ControlGroup> extends Section.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(ControlGroup.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new ControlGroup(container);
		}
	}

	public ControlGroup(IObject container) {
		super(container);
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		super.writeMeta(writer, query, context);
		writer.writeProperty(Json.isControlGroup, true);
	}
}
