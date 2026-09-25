package org.zenframework.z8.server.reports.poi;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

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
	public boolean next() {
		super.next();

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

	@Override
	public Collection<String> getCurrentValueIds() {
		Object currentItem = item.get();

		if (!(currentItem instanceof JsonObject))
			return Collections.emptyList();

		Collection<String> ids = new LinkedList<String>();
		collectPaths((JsonObject) currentItem, "", ids);
		return ids;
	}

	@Override
	protected void fillAggregated(AggregatedSource source) {
		JsonObject result = new JsonObject();

		for (String path : getAggregatedIds())
			setValueToJson(result, path, source.getValue(path));

		source.<Object>getObjectProperty(Item).set(result);
	}

	private static void collectPaths(JsonObject object, String prefix, Collection<String> ids) {
		for (String name : object.getNames()) {
			Object value = object.get(name);
			String path = prefix.isEmpty() ? name : prefix + '.' + name;

			if (value instanceof JsonObject)
				collectPaths((JsonObject) value, path, ids);
			else
				ids.add(path);
		}
	}

	private static void setValueToJson(JsonObject object, String path, Object value) {
		String[] parts = path.split("\\.");
		JsonObject current = object;

		for (int i = 0; i < parts.length - 1; i++) {
			JsonObject next = current.getJsonObject(parts[i]);

			if (next == null)
				current.set(parts[i], next = new JsonObject());

			current = next;
		}

		current.set(parts[parts.length - 1], value);
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

			current = currentObj.get(part);
		}

		return current;
	}

}
