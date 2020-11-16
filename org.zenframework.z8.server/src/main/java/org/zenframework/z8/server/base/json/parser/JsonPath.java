package org.zenframework.z8.server.base.json.parser;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.string;

public class JsonPath extends OBJECT {

	public static class CLASS<T extends JsonPath> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(JsonPath.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new JsonPath(container);
		}
	}

	public JsonPath(IObject container) {
		super(container);
	}

	private org.zenframework.z8.server.json.parser.JsonPath path;

	public org.zenframework.z8.server.json.parser.JsonPath get() {
		return path;
	}

	public static JsonPath.CLASS<JsonPath> z8_path(string path) {
		JsonPath.CLASS<JsonPath> jsonPath = new JsonPath.CLASS<JsonPath>(null);
		jsonPath.get().path = new org.zenframework.z8.server.json.parser.JsonPath(path.get());
		return jsonPath;
	}

}
