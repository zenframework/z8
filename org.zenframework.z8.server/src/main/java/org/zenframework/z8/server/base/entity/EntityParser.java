package org.zenframework.z8.server.base.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.json.parser.JsonObject;
import org.zenframework.z8.server.json.parser.JsonUtils;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class EntityParser extends OBJECT {
	public static class CLASS<T extends EntityParser> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(EntityParser.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new EntityParser(container);
		}
	}

	public static final string String = new string("string");
	public static final string Integer = new string("integer");
	public static final string Decimal = new string("decimal");
	public static final string Boolean = new string("boolean");
	public static final string Datespan = new string("datespan");
	public static final string Guid = new string("guid");
	public static final string Date = new string("date");
	public static final string Json = new string("json");

	private Registry registry = null;
	private Entity entity = null;
	private JsonObject json = null;

	public EntityParser(IObject container) {
		super(container);
	}

	public Registry getRegistry() {
		return registry;
	}

	public EntityParser setRegistry(Registry registry) {
		this.registry = registry;
		return this;
	}

	public Entity getEntity() {
		if (entity == null)
			entity = newEntity();
		return entity;
	}

	public EntityParser setEntity(Entity entity) {
		this.entity = entity;
		return this;
	}

	public org.zenframework.z8.server.json.parser.JsonObject getJson() {
		if (json == null)
			json = new JsonObject(this);
		return json.get();
	}

	public EntityParser setJson(String json) {
		return setJson(new org.zenframework.z8.server.json.parser.JsonObject(json));
	}

	public EntityParser setJson(org.zenframework.z8.server.json.parser.JsonObject json) {
		this.json = json != null ? JsonObject.getJsonObject(json).get() : null;
		return this;
	}

	public org.zenframework.z8.server.json.parser.JsonObject toJson(Entity entity) {
		return entity != null ? setEntity(entity).setJson((org.zenframework.z8.server.json.parser.JsonObject) null).write().getJson() : null;
	}

	public org.zenframework.z8.server.json.parser.JsonArray toJson(Collection<Entity> entities) {
		if (entities == null)
			return null;

		org.zenframework.z8.server.json.parser.JsonArray json = new org.zenframework.z8.server.json.parser.JsonArray();
		for (Entity entity : entities)
			json.add(toJson(entity));

		return json;
	}

	public Entity fromJson(org.zenframework.z8.server.json.parser.JsonObject json) {
		return json != null ? setJson(json).setEntity(null).read().getEntity() : null;
	}

	public List<Entity> fromJson(org.zenframework.z8.server.json.parser.JsonArray json) {
		if (json == null)
			return new ArrayList<Entity>(0);

		List<Entity> entities = new ArrayList<Entity>(json.size());
		for (int i = 0; i < json.size(); i++)
			entities.add(fromJson(json.getJsonObject(i)));

		return entities;
	}

	public EntityParser read() {
		return z8_read().get(); /* virtual */
	}

	public EntityParser write() {
		return z8_write().get(); /* virtual */
	}

	public Entity newEntity() {
		return z8_newEntity().get().register(registry); /* virtual */
	}

	public EntityParser readString(string key) {
		String value = key != null ? json.get().getString(key.get()) : null;
		if (value != null)
			getEntity().set(key, new string(value));
		return this;
	}

	public EntityParser readInt(string key) {
		integer value = key != null ? json.get().getInteger(key.get()) : null;
		if (value != null)
			getEntity().set(key, value);
		return this;
	}

	public EntityParser readDecimal(string key) {
		decimal value = key != null ? json.get().getDecimal(key.get()) : null;
		if (value != null)
			getEntity().set(key, value);
		return this;
	}

	public EntityParser readGuid(string key) {
		guid value = key != null ? json.get().getGuid(key.get()) : null;
		if (value != null)
			getEntity().set(key, value);
		return this;
	}

	public EntityParser readBool(string key) {
		Object value = key != null ? json.get().get(key.get()) : null;
		if (value != null)
			getEntity().set(key, new bool(value.equals(java.lang.Boolean.TRUE)));
		return this;
	}

	public EntityParser readDate(string key) {
		date value = key != null ? json.get().getDate(key.get()) : null;
		if (value != null)
			getEntity().set(key, value);
		return this;
	}

	public EntityParser readDatespan(string key) {
		String value = key != null ? json.get().getString(key.get()) : null;
		if (value != null)
			getEntity().set(key, new datespan(value));
		return this;
	}

	public EntityParser readJsonObject(string key) {
		org.zenframework.z8.server.json.parser.JsonObject value = key != null ? json.get().getJsonObject(key.get()) : null;
		if (value != null)
			getEntity().set(key, value);
		return this;
	}

	public EntityParser readJsonArray(string key) {
		org.zenframework.z8.server.json.parser.JsonArray value = key != null ? json.get().getJsonArray(key.get()) : null;
		if (value != null)
			getEntity().set(key, value);
		return this;
	}

	// TODO Formats
	public EntityParser readValue(string key, string type) {
		Object obj = json.get().get(key.get());

		if (obj == null || !type.equals(String) && obj instanceof String && ((String) obj).isEmpty())
			return this;

		primary value;

		if (type.equals(String))
			value = new string(obj.toString());
		else if (type.equals(Integer))
			value = obj instanceof Integer ? new integer((Integer) obj) : obj instanceof Long ? new integer((Long) obj)
					: new integer(obj.toString());
		else if (type.equals(Decimal))
			value = obj instanceof Double ? new decimal((Double) obj) : obj instanceof Float ? new decimal((Float) obj)
					: new decimal(obj.toString().replace(',', '.')); // TODO Decimal format
		else if (type.equals(Boolean))
			value = obj instanceof java.lang.Boolean ? (((java.lang.Boolean) obj) ? bool.True : bool.False) : new bool(obj.toString());
//		else if (type.equals(Datespan))
//			value = new datespan(obj.toString());
//		else if (type.equals(Guid))
//			value = obj instanceof guid ? (guid) obj : new guid(obj.toString());
//		else if (type.equals(Date))
//			value = obj instanceof date ? (date) obj : new date(obj.toString());
		else
			throw new IllegalArgumentException("Unsupported type '" + type + "'");

		getEntity().set(key, value);

		return this;
	}

	public EntityParser readValue(string key) {
		Object value = key != null ? json.get().get(key.get()) : null;
		if (value != null)
			getEntity().set(key, (primary) JsonUtils.wrap(value));
		return this;
	}

	public EntityParser readValues(Collection<string> keys) {
		if (keys != null)
			for (string key : keys)
				readValue(key);
		return this;
	}

	public EntityParser writeValue(string key) {
		return writeValue(key, getEntity().get(key));
	}

	public EntityParser writeValues(Collection<string> keys) {
		if (keys != null)
			for (string key : keys)
				writeValue(key);
		return this;
	}

	public EntityParser writeValue(string key, primary value) {
		if (value != null)
			getJson().set(key, value);
		return this;
	}

	public EntityParser writeValue(string key, org.zenframework.z8.server.json.parser.JsonArray value) {
		if (value != null)
			getJson().set(key, value);
		return this;
	}

	public EntityParser writeValue(string key, org.zenframework.z8.server.json.parser.JsonObject value) {
		if (value != null)
			getJson().set(key, value);
		return this;
	}

	public EntityParser writeNonEmpty(string key) {
		return writeNonEmpty(key, getEntity().get(key));
	}

	public EntityParser writeNonEmpty(Collection<string> keys) {
		if (keys != null)
			for (string key : keys)
				writeNonEmpty(key);
		return this;
	}

	public EntityParser writeNonEmpty(string key, primary value) {
		if (!isEmpty(value))
			getJson().set(key, value);
		return this;
	}

	public EntityParser writeNonEmpty(string key, org.zenframework.z8.server.json.parser.JsonArray value) {
		if (!isEmpty(value))
			getJson().set(key, value);
		return this;
	}

	public EntityParser writeNonEmpty(string key, org.zenframework.z8.server.json.parser.JsonObject value) {
		if (!isEmpty(value))
			getJson().set(key, value);
		return this;
	}

	public primary getValue(string key, String type) {
		return getValue(getJson(), key, type);
	}

	public static primary getValue(org.zenframework.z8.server.json.parser.JsonObject json, string key, String type) {
		if (key == null)
			return null;

		if ("boolean".equals(type))
			return json.getBool(key);
		if ("guid".equals(type))
			return json.getGuid(key);
		if ("date".equals(type))
			return json.getDate(key);
		if ("decimal".equals(type))
			return json.getDecimal(key);
		if ("int".equals(type))
			return new integer(json.getInt(key));
		if ("string".equals(type))
			return new string(json.getString(key));

		throw new RuntimeException("Unsupported type '" + type + "'");
	}

	public static primary getValue(org.zenframework.z8.server.json.parser.JsonArray json, int index, String type) {
		if ("boolean".equals(type))
			return json.getBool(index);
		if ("guid".equals(type))
			return json.getGuid(index);
		if ("date".equals(type))
			return json.getDate(index);
		if ("decimal".equals(type))
			return json.getDecimal(index);
		if ("int".equals(type))
			return json.getInteger(index);
		if ("string".equals(type))
			return new string(json.getString(index));

		throw new RuntimeException("Unsupported type '" + type + "'");
	}

	public static boolean isEmpty(primary value) {
		return value == null
				|| (value instanceof bool) && ((bool) value).equals(bool.False)
				|| (value instanceof date) && ((date) value).equals(date.Min)
				|| (value instanceof datespan) && ((datespan) value).get() == 0L
				|| (value instanceof decimal) && (((decimal) value).equals(decimal.Zero) /*|| ((decimal) value).equals(decimal.NaN)*/)
				|| (value instanceof guid) && ((guid) value).equals(guid.Null)
				|| (value instanceof integer) && ((integer) value).get() == 0
				|| (value instanceof string) && ((string) value).get().isEmpty();
	}

	public static boolean isEmpty(org.zenframework.z8.server.json.parser.JsonObject value) {
		return value == null || value.isEmpty();
	}

	public static boolean isEmpty(org.zenframework.z8.server.json.parser.JsonArray value) {
		return value == null || value.isEmpty();
	}

	/* BL */

	@SuppressWarnings("unchecked")
	public Registry.CLASS<? extends Registry> z8_getRegistry() {
		return (Registry.CLASS<? extends Registry>) getRegistry().getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_setRegistry(Registry.CLASS<? extends Registry> registry) {
		return (EntityParser.CLASS<? extends EntityParser>) setRegistry(registry != null ? registry.get() : null).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_getEntity() {
		return (Entity.CLASS<? extends Entity>) getEntity().getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_setEntity(Entity.CLASS<? extends Entity> entity) {
		return (EntityParser.CLASS<? extends EntityParser>) setEntity(entity != null ? entity.get() : null).getCLASS();
	}

	public JsonObject.CLASS<? extends JsonObject> z8_getJson() {
		return JsonObject.getJsonObject(getJson());
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_setJson(string json) {
		return (EntityParser.CLASS<? extends EntityParser>) setJson(json != null ? json.get() : null).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_setJson(JsonObject.CLASS<? extends JsonObject> json) {
		return (EntityParser.CLASS<? extends EntityParser>) setJson(json != null ? json.get().get() : null).getCLASS();
	}

	public JsonObject.CLASS<? extends JsonObject> z8_toJson(Entity.CLASS<? extends Entity> entity) {
		return JsonObject.getJsonObject(toJson(entity != null ? entity.get() : null));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JsonArray.CLASS<? extends JsonArray> z8_toJson(RCollection entities) {
		return JsonArray.getJsonArray(toJson(entities != null ? CLASS.<Entity>asList(entities) : null));
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_fromJson(JsonObject.CLASS<? extends JsonObject> json) {
		Entity entity = fromJson(json != null ? json.get().get() : null);
		return entity != null ? (Entity.CLASS<? extends Entity>) entity.getCLASS() : null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RCollection z8_fromJson(JsonArray.CLASS<? extends JsonArray> json) {
		List<Entity> entities = fromJson(json != null ? json.get().get() : null);
		RCollection result = new RCollection(entities.size(), false);
		for (Entity entity : entities)
			result.add(entity.getCLASS());
		return result;
	}

	/* virtual */ @SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_read() {
		return (EntityParser.CLASS<? extends EntityParser>) getCLASS();
	}

	/* virtual */ @SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_write() {
		return (EntityParser.CLASS<? extends EntityParser>) getCLASS();
	}

	/* virtual */ public Entity.CLASS<? extends Entity> z8_newEntity() {
		return new Entity.CLASS<Entity>(getContainer());
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_readString(string key) {
		return (EntityParser.CLASS<? extends EntityParser>) readString(key).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_readInt(string key) {
		return (EntityParser.CLASS<? extends EntityParser>) readInt(key).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_readDecimal(string key) {
		return (EntityParser.CLASS<? extends EntityParser>) readDecimal(key).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_readGuid(string key) {
		return (EntityParser.CLASS<? extends EntityParser>) readGuid(key).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_readBool(string key) {
		return (EntityParser.CLASS<? extends EntityParser>) readBool(key).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_readDate(string key) {
		return (EntityParser.CLASS<? extends EntityParser>) readDate(key).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_readDatespan(string key) {
		return (EntityParser.CLASS<? extends EntityParser>) readDatespan(key).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_readJsonObject(string key) {
		return (EntityParser.CLASS<? extends EntityParser>) readJsonObject(key).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_readJsonArray(string key) {
		return (EntityParser.CLASS<? extends EntityParser>) readJsonArray(key).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_readValue(string key, string type) {
		return (EntityParser.CLASS<? extends EntityParser>) readValue(key, type).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_readValue(string key) {
		return (EntityParser.CLASS<? extends EntityParser>) readValue(key).getCLASS();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EntityParser.CLASS<? extends EntityParser> z8_readValues(RCollection keys) {
		return (EntityParser.CLASS<? extends EntityParser>) readValues(keys).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_writeValue(string key) {
		return (EntityParser.CLASS<? extends EntityParser>) writeValue(key).getCLASS();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EntityParser.CLASS<? extends EntityParser> z8_writeValues(RCollection keys) {
		return (EntityParser.CLASS<? extends EntityParser>) writeValues(keys).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_writeValue(string key, primary value) {
		return (EntityParser.CLASS<? extends EntityParser>) writeValue(key, value).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_writeValue(string key, JsonObject.CLASS<? extends JsonObject> value) {
		return (EntityParser.CLASS<? extends EntityParser>) writeValue(key, value != null ? value.get().get() : null).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_writeValue(string key, JsonArray.CLASS<? extends JsonArray> value) {
		return (EntityParser.CLASS<? extends EntityParser>) writeValue(key, value != null ? value.get().get() : null).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_writeNonEmpty(string key) {
		return (EntityParser.CLASS<? extends EntityParser>) writeNonEmpty(key).getCLASS();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EntityParser.CLASS<? extends EntityParser> z8_writeNonEmpty(RCollection keys) {
		return (EntityParser.CLASS<? extends EntityParser>) writeNonEmpty(keys).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_writeNonEmpty(string key, primary value) {
		return (EntityParser.CLASS<? extends EntityParser>) writeNonEmpty(key, value).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_writeNonEmpty(string key, JsonObject.CLASS<? extends JsonObject> value) {
		return (EntityParser.CLASS<? extends EntityParser>) writeNonEmpty(key, value != null ? value.get().get() : null).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityParser.CLASS<? extends EntityParser> z8_writeNonEmpty(string key, JsonArray.CLASS<? extends JsonArray> value) {
		return (EntityParser.CLASS<? extends EntityParser>) writeNonEmpty(key, value != null ? value.get().get() : null).getCLASS();
	}

	public primary z8_getValue(string key, string type) {
		return getValue(key, type.get());
	}

	public static primary z8_getValue(JsonObject.CLASS<? extends JsonObject> json, string key, string type) {
		return getValue(json.get().get(), key, type.get());
	}

	public static primary z8_getValue(JsonArray.CLASS<? extends JsonArray> json, integer index, string type) {
		return getValue(json.get().get(), index.getInt(), type.get());
	}

	public static bool z8_isEmpty(primary value) {
		return isEmpty(value) ? bool.True : bool.False;
	}

	public static bool z8_isEmpty(JsonObject.CLASS<? extends JsonObject> value) {
		return value == null || value.get().get().isEmpty() ? bool.True : bool.False;
	}

	public static bool z8_isEmpty(JsonArray.CLASS<? extends JsonArray> value) {
		return value == null || value.get().get().isEmpty() ? bool.True : bool.False;
	}
}
