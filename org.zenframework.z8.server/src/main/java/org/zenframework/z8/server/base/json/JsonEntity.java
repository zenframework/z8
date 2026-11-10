package org.zenframework.z8.server.base.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.META;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class JsonEntity extends OBJECT {

	public static class CLASS<T extends JsonEntity> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(JsonEntity.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new JsonEntity(container);
		}
	}

	public JsonEntity(IObject container) {
		super(container);
	}

	public void operatorAssign(JsonObject.CLASS<? extends JsonObject> source) {
		readJson(source.get().get());
	}

	public void writeJson(org.zenframework.z8.server.json.parser.JsonObject json) {
		try {
			for (Field field : getClass().getDeclaredFields()) {
				if (!Modifier.isPublic(field.getModifiers()))
					continue;

				Object jsonValue = toJsonValue(field.get(this));

				if (jsonValue != null)
					json.set(field.getName(), jsonValue);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void readJson(org.zenframework.z8.server.json.parser.JsonObject json) {
		try {
			for (Field field : getClass().getDeclaredFields()) {
				if (!Modifier.isPublic(field.getModifiers()))
					continue;
				String key = field.getName();
				field.set(this, json.containsKey(key) ? fromJsonValue(json.get(key), META.get(field.get(this))) : null);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public org.zenframework.z8.server.json.parser.JsonObject toJsonObject() {
		org.zenframework.z8.server.json.parser.JsonObject json = new org.zenframework.z8.server.json.parser.JsonObject();
		writeJson(json);
		return json;
	}

	@SuppressWarnings("rawtypes")
	public static org.zenframework.z8.server.json.parser.JsonArray toJsonArray(RCollection array) {
		return (org.zenframework.z8.server.json.parser.JsonArray) toJsonValue(array);
	}

	public void z8_writeJson(JsonObject.CLASS<? extends JsonObject> json) {
		writeJson(json.get().get());
	}

	public void z8_readJson(JsonObject.CLASS<? extends JsonObject> json) {
		readJson(json.get().get());
	}

	public JsonObject.CLASS<? extends JsonObject> z8_toJsonObject() {
		return JsonObject.getJsonObject(toJsonObject());
	}

	@SuppressWarnings("rawtypes")
	public static JsonArray.CLASS<? extends JsonArray> z8_toJsonArray(RCollection array) {
		return JsonArray.getJsonArray(toJsonArray(array));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object toJsonValue(Object value) {
		if (value instanceof primary)
			return value;

		if (value instanceof JsonEntity.CLASS)
			return ((JsonEntity.CLASS<? extends JsonEntity>) value).get().toJsonObject();

		if (value instanceof RCollection) {
			RCollection array = (RCollection) value;
			Object first = array.z8_first();

			if (first == null || !(first instanceof primary || first instanceof JsonEntity.CLASS
					|| first instanceof RCollection || first instanceof RLinkedHashMap))
				return null;

			org.zenframework.z8.server.json.parser.JsonArray json = new org.zenframework.z8.server.json.parser.JsonArray();

			for (Object element : array)
				json.add(toJsonValue(element));

			return json;
		}

		if (value instanceof RLinkedHashMap) {
			RLinkedHashMap<?, ?> map = (RLinkedHashMap) value;
			Map.Entry first = map.entrySet().iterator().next();

			if (first == null || !(first.getKey() instanceof string)
					|| !(first.getValue() instanceof primary || first.getValue() instanceof JsonEntity.CLASS
							|| first instanceof RCollection || first instanceof RLinkedHashMap))
				return null;

			org.zenframework.z8.server.json.parser.JsonObject json = new org.zenframework.z8.server.json.parser.JsonObject();

			for (Map.Entry<?, ?> entry : map.entrySet())
				json.set((string) entry.getKey(), toJsonValue(entry.getValue()));

			return json;
		}

		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object fromJsonValue(Object jsonValue, META meta) {
		if (jsonValue == null)
			return null;

		switch (meta.getType()) {
		case Primary:
			return org.zenframework.z8.server.json.parser.JsonObject.asType(jsonValue, meta.getJavaClass());
		case Object:
			JsonEntity.CLASS<? extends JsonEntity> entity = ((JsonEntity.CLASS<? extends JsonEntity>) meta.newObject());
			entity.get().readJson(org.zenframework.z8.server.json.parser.JsonObject.asJsonObject(jsonValue));
			return entity;
		case Array:
			org.zenframework.z8.server.json.parser.JsonArray jsonArray = org.zenframework.z8.server.json.parser.JsonObject
					.asJsonArray(jsonValue);
			RCollection array = (RCollection) meta.newObject();

			for (int i = 0; i < jsonArray.size(); i++)
				array.add(fromJsonValue(jsonArray.get(i), meta.getValueMeta()));

			return array;
		case Map:
			org.zenframework.z8.server.json.parser.JsonObject jsonObject = org.zenframework.z8.server.json.parser.JsonObject
					.asJsonObject(jsonValue);
			RLinkedHashMap map = (RLinkedHashMap) meta.newObject();

			for (Map.Entry<String, Object> entry : jsonObject.entrySet())
				map.put(new string(entry.getKey()), fromJsonValue(jsonObject.get(entry.getKey()), meta.getValueMeta()));
		default:
			throw new IllegalStateException(meta.getType().toString());
		}
	}

}
