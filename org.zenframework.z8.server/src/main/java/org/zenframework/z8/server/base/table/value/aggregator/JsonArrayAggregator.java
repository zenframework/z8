package org.zenframework.z8.server.base.table.value.aggregator;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class JsonArrayAggregator extends Aggregator {

	public static class CLASS<T extends JsonArrayAggregator> extends Aggregator.CLASS<T> {

		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(JsonArrayAggregator.class);
			setName(JsonArrayAggregator.class.getName());
			setDisplayName(JsonArrayAggregator.class.getName());
		}

		@Override
		public Object newObject(IObject container) {
			return new JsonArrayAggregator(container);
		}

	}

	public JsonArrayAggregator(IObject container) {
		super(container);
	}

	@Override
	public primary z8_aggregate(primary oldValue, primary newValue) {
		JsonArray.CLASS<JsonArray> oldArray = JsonArray.z8_parse(oldValue.string());
		JsonArray.CLASS<JsonArray> newArray = JsonArray.z8_parse(newValue.string());
		for (int i = 0; i < oldArray.get().z8_length().get(); i++) {
			JsonObject.CLASS<? extends JsonObject> obj = oldArray.get().z8_getJsonObject(new integer(i));
			if (!contains(newArray, obj) && z8_keepElement(obj).get())
				newArray.get().z8_put(obj);
		}
		return new string(newArray.get().toString());
	}

	public bool z8_keepElement(JsonObject.CLASS<? extends JsonObject> obj) {
		return new bool(true);
	}

	public bool z8_equals(JsonObject.CLASS<? extends JsonObject> o1, JsonObject.CLASS<? extends JsonObject> o2) {
		throw new UnsupportedOperationException("Method must be overriden");
	}

	private boolean contains(JsonArray.CLASS<? extends JsonArray> arr, JsonObject.CLASS<? extends JsonObject> obj) {
		for (int j = 0; j < arr.get().z8_length().get(); j++) {
			if (z8_equals(arr.get().z8_getJsonObject(new integer(j)), obj).get()) {
				return true;
			}
		}
		return false;
	}

}
