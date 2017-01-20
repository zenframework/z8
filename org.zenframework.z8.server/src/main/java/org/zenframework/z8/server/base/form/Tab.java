package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;

public class Tab extends Section {
	public static class CLASS<T extends Tab> extends Section.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Tab.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Tab(container);
		}
	}

	public Tab(IObject container) {
		super(container);
		columnCount = new integer(1);
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		super.writeMeta(writer, query, context);
		writer.writeProperty(Json.isTab, true);
		writer.writeProperty(Json.header, displayName());
		writer.writeProperty(Json.icon, icon());
	}
}
