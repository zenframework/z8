package org.zenframework.z8.server.reports.poi;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class JsonSource extends DataSource {
	private static final String Item = "item";

	private final JsonArray json;

	private Wrapper<Object> item;

	public JsonSource(JsonArray json) {
		this.json = json;
	}

	@Override
	public OBJECT getObject() {
		return json;
	}

	@Override
	public int count() {
		return json.get().size();
	}

	@Override
	public void open() {
		super.open();
		item.set(null);
	}

	@Override
	protected void initialize() {
		super.initialize();
		item = getObjectProperty(Item);
	}

	@Override
	protected boolean internalNext() {
		int index = getIndex();
		boolean hasNext = index < json.get().size();
		item.set(hasNext ? json.get().get(index) : null);
		return hasNext;
	}

	@Override
	public Object getCurrentValue(String path) {
		Object currentItem = item.get();
		if (currentItem == null)
			return null;

		return extractValueFromJson(currentItem, path);
	}

	private Object extractValueFromJson(Object obj, String path) {
		if (!(obj instanceof JsonObject))
			return null;

		JsonObject jsonObj = (JsonObject) obj;
		String[] parts = path.split("\\.");
		Object current = jsonObj;

		for (String part : parts) {
			if (!(current instanceof JsonObject))
				return null;

			JsonObject currentObj = (JsonObject) current;

			if (!currentObj.has(part))
				return null;

			current = currentObj.opt(part);
		}

		return current;
	}

}
