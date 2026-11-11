package org.zenframework.z8.server.reports.poi;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	protected void fillAggregated(AggregatedSource source) {
		JsonObject result = new JsonObject();

		for (String path : getAggregatedIds())
			setValueToJson(result, path, source.getValue(path));

		source.<Object>getObjectProperty(Item).set(result);
	}

	private static void setValueToJson(JsonObject object, String path, Object value) {
		List<Object> tokens = parsePath(path);
		Object current = object;

		for (int i = 0; i < tokens.size() - 1; i++) {
			Object next = getValue(current, tokens.get(i));

			if (next == null)
				setValue(current, tokens.get(i), next = tokens.get(i + 1) instanceof Integer
						? new org.zenframework.z8.server.json.parser.JsonArray() : new JsonObject());

			current = next;
		}

		setValue(current, tokens.get(tokens.size() - 1), value);
	}

	private static Object extractValueFromJson(Object object, String path) {
		for (Object token : parsePath(path)) {
			object = getValue(object, token);

			if (object == null)
				return null;
		}

		return object;
	}

	private static List<Object> parsePath(String path) {
		List<Object> tokens = new LinkedList<Object>();

		for (String part : path.split("\\.")) {
			int bracket = part.indexOf('[');

			tokens.add(bracket < 0 ? part : part.substring(0, bracket));

			while (bracket >= 0) {
				int close = part.indexOf(']', bracket);

				if (close < 0)
					break;

				tokens.add(Integer.valueOf(part.substring(bracket + 1, close)));
				bracket = part.indexOf('[', close);
			}
		}

		return tokens;
	}

	@SuppressWarnings("unchecked")
	private static Object getValue(Object object, Object token) {
		if (!(token instanceof Integer))
			return object instanceof Map ? ((Map<String, Object>) object).get(token) : null;

		if (!(object instanceof List))
			return null;

		List<Object> list = (List<Object>) object;
		int index = (Integer) token;

		return index >= 0 && index < list.size() ? list.get(index) : null;
	}

	@SuppressWarnings("unchecked")
	private static void setValue(Object object, Object token, Object value) {
		if (!(token instanceof Integer)) {
			((Map<String, Object>) object).put((String) token, value);
			return;
		}

		List<Object> list = (List<Object>) object;
		int index = (Integer) token;

		while (list.size() <= index)
			list.add(null);

		list.set(index, value);
	}

}
