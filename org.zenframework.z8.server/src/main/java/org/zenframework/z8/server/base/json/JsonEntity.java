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
		objectFromJson(this, source.get().get());
	}

	public void operatorAssign(string source) {
		objectFromJson(this, new org.zenframework.z8.server.json.parser.JsonObject(source.get()));
	}

	public org.zenframework.z8.server.json.parser.JsonObject toJson() {
		return z8_toJson().get().get();
	}

	public JsonEntity fromJson(org.zenframework.z8.server.json.parser.JsonObject json) {
		return z8_fromJson(JsonObject.getJsonObject(json)).get();
	}

	protected JsonEntity newObject(org.zenframework.z8.server.json.parser.JsonObject json) {
		return z8_newObject(JsonObject.getJsonObject(json)).get();
	}

	public static org.zenframework.z8.server.json.parser.JsonObject toJsonObject(OBJECT object) {
		return (org.zenframework.z8.server.json.parser.JsonObject) toJsonValue(object);
	}

	@SuppressWarnings("rawtypes")
	public static org.zenframework.z8.server.json.parser.JsonArray toJsonArray(RCollection array) {
		return (org.zenframework.z8.server.json.parser.JsonArray) toJsonValue(array);
	}

	@SuppressWarnings("unchecked")
	public OBJECT.CLASS<? extends OBJECT> z8_getParent() {
		IObject container = getContainer();
		return container != null ? (OBJECT.CLASS<? extends OBJECT>) container.getCLASS() : null;
	}

	/* virtual */ public JsonObject.CLASS<? extends JsonObject> z8_toJson() {
		return JsonObject.getJsonObject(objectToJson(this));
	}

	@SuppressWarnings("unchecked")
	/* virtual */ public JsonEntity.CLASS<? extends JsonEntity> z8_fromJson(JsonObject.CLASS<? extends JsonObject> json) {
		return (JsonEntity.CLASS<? extends JsonEntity>) objectFromJson(newObject(json.get().get()), json.get().get()).getCLASS();
	}

	/* virtual */ @SuppressWarnings("unchecked")
	public JsonEntity.CLASS<? extends JsonEntity> z8_newObject(JsonObject.CLASS<? extends JsonObject> json) {
		return (JsonEntity.CLASS<? extends JsonEntity>) getCLASS();
	}

	public static JsonObject.CLASS<? extends JsonObject> z8_toJsonObject(OBJECT.CLASS<? extends OBJECT> object) {
		return JsonObject.getJsonObject(toJsonObject(object.get()));
	}

	@SuppressWarnings("rawtypes")
	public static JsonArray.CLASS<? extends JsonArray> z8_toJsonArray(RCollection array) {
		return JsonArray.getJsonArray(toJsonArray(array));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object toJsonValue(Object value) {
		if (value instanceof primary)
			return value;

		if (value instanceof JsonObject.CLASS)
			return ((JsonObject.CLASS<? extends JsonObject>) value).get().get();

		if (value instanceof JsonArray.CLASS)
			return ((JsonArray.CLASS<? extends JsonArray>) value).get().get();

		if (value instanceof JsonEntity.CLASS)
			return ((JsonEntity.CLASS<? extends JsonEntity>) value).get().toJson();

		if (value instanceof OBJECT.CLASS)
			return objectToJson(((OBJECT.CLASS) value).get());

		if (value instanceof RCollection)
			return arrayToJson((RCollection) value);

		if (value instanceof RLinkedHashMap)
			return mapToJson((RLinkedHashMap) value);

		return null;
	}

	private static org.zenframework.z8.server.json.parser.JsonObject objectToJson(IObject object) {
		try {
			org.zenframework.z8.server.json.parser.JsonObject json = new org.zenframework.z8.server.json.parser.JsonObject();

			for (Field field : object.getClass().getFields()) {
				if (!isJsonField(field))
					continue;

				Object jsonValue = toJsonValue(field.get(object));

				if (jsonValue != null)
					json.set(getName(field), jsonValue);
			}

			return json;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private static org.zenframework.z8.server.json.parser.JsonArray arrayToJson(RCollection array) {
		Object first = array.z8_first();

		if (first == null || !(first instanceof primary || first instanceof OBJECT.CLASS || first instanceof RCollection
				|| first instanceof RLinkedHashMap))
			return null;

		org.zenframework.z8.server.json.parser.JsonArray json = new org.zenframework.z8.server.json.parser.JsonArray();

		for (Object element : array)
			json.add(toJsonValue(element));

		return json;
	}

	@SuppressWarnings("rawtypes")
	private static org.zenframework.z8.server.json.parser.JsonObject mapToJson(RLinkedHashMap<?, ?> map) {
		Map.Entry first = map.entrySet().iterator().next();

		if (first == null || !(first.getKey() instanceof string)
				|| !(first.getValue() instanceof primary || first.getValue() instanceof OBJECT.CLASS
						|| first instanceof RCollection || first instanceof RLinkedHashMap))
			return null;

		org.zenframework.z8.server.json.parser.JsonObject json = new org.zenframework.z8.server.json.parser.JsonObject();

		for (Map.Entry<?, ?> entry : map.entrySet())
			json.set((string) entry.getKey(), toJsonValue(entry.getValue()));

		return json;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object fromJsonValue(OBJECT parent, Object jsonValue, META meta) {
		if (jsonValue == null)
			return null;

		switch (meta.getType()) {
		case Primary:
			return org.zenframework.z8.server.json.parser.JsonObject.asType(jsonValue, meta.getJavaClass());
		case Object:
			OBJECT object = ((OBJECT.CLASS<? extends OBJECT>) meta.newObject(parent)).get();
			if (object instanceof JsonEntity)
				return ((JsonEntity) object).fromJson(org.zenframework.z8.server.json.parser.JsonObject.asJsonObject(jsonValue)).getCLASS();
			if (object instanceof JsonObject)
				return JsonObject.getJsonObject(org.zenframework.z8.server.json.parser.JsonObject.asJsonObject(jsonValue));
			if (object instanceof JsonArray)
				return JsonArray.getJsonArray(org.zenframework.z8.server.json.parser.JsonObject.asJsonArray(jsonValue));
			return objectFromJson(object, org.zenframework.z8.server.json.parser.JsonObject.asJsonObject(jsonValue)).getCLASS();
		case Array:
			return arrayFromJson(parent, (RCollection) meta.newObject(parent),
					org.zenframework.z8.server.json.parser.JsonObject.asJsonArray(jsonValue));
		case Map:
			return mapFromJson(parent, (RLinkedHashMap) meta.newObject(parent),
					org.zenframework.z8.server.json.parser.JsonObject.asJsonObject(jsonValue));
		default:
			throw new IllegalStateException(meta.getType().toString());
		}
	}

	private static OBJECT objectFromJson(OBJECT object, org.zenframework.z8.server.json.parser.JsonObject json) {
		try {
			for (Field field : object.getClass().getFields()) {
				if (!isJsonField(field))
					continue;

				String key = getName(field);
				field.set(object, json.containsKey(key) ? fromJsonValue(object, json.get(key), META.get(object, field)) : null);
			}

			return object;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static RCollection arrayFromJson(OBJECT parent, RCollection array, org.zenframework.z8.server.json.parser.JsonArray json) {
		META valueMeta = array.getMeta().getValueMeta();

		for (int i = 0; i < json.size(); i++)
			array.add(fromJsonValue(parent, json.get(i), valueMeta));

		return array;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static RLinkedHashMap mapFromJson(OBJECT parent, RLinkedHashMap map,
			org.zenframework.z8.server.json.parser.JsonObject json) {
		META valueMeta = map.getMeta().getValueMeta();

		for (Map.Entry<String, Object> entry : json.entrySet())
			map.put(new string(entry.getKey()), fromJsonValue(parent, json.get(entry.getKey()), valueMeta));

		return map;
	}

	private static String getName(Field field) {
		String name = field.getName();
		return name.charAt(0) == '_' ? name.substring(1) : name;
	}

	private static boolean isJsonField(Field field) {
		return !Modifier.isStatic(field.getModifiers()) && field.getDeclaringClass() != OBJECT.class;
	}

}
