package org.zenframework.z8.server.base.entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.json.parser.JsonObject;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Entity extends OBJECT {
	public static class CLASS<T extends Entity> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Entity.class);
		}

		public Object newObject(IObject container) {
			return new Entity(container);
		}
	}

	public static final string EntityId = new string("recordId");
	public static final string OriginId = new string("originId");

	public static final String AttrEntityTable = "entityTable";

	private final Map<string, primary> defaultValues = new HashMap<string, primary>();
	private final Map<string, primary> values = new HashMap<string, primary>();

	private boolean initialized = false;
	private boolean persistent = false;
	private boolean deleted = false;

	private Registry registry = null;

	public Entity(IObject container) {
		super(container);
	}

	@Override
	public int hashCode() {
		guid id = getEntityId();
		return getClass().hashCode() ^ (id != null ? id.hashCode() : 0);
	}


	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		guid id = getEntityId();
		guid objId = ((Entity) obj).getEntityId();

		if (id == null && objId == null)
			return true;
		if (id == null || objId == null)
			return false;

		return id.equals(objId);
	}


	@Override
	public String toString() {
		return z8_toString().get();
	}

	public String toString(boolean defaults, boolean values, boolean changes) {
		guid id = getEntityId();
		StringBuilder str = new StringBuilder(1024);
		str.append(getClass().getSimpleName()).append('[').append(id != null ? id.toString() : "-").append(' ')
				.append(initialized ? 'i' : '-').append(persistent ? 'p' : '-').append(deleted ? 'd' : '-').append(isChanged() ? 'c' : '-').append(']');
		if (defaults)
			str.append("\n\tdefaults: ").append(defaultValues);
		if (values)
			str.append("\n\tvalues: ").append(this.values);
		if (changes)
			str.append("\n\tvalues: ").append(getChanges());
		return str.toString();
	}

	private Map<string, List<primary>> getChanges() {
		Map<string, List<primary>> changes = new HashMap<string, List<primary>>();
		for (Map.Entry<string, primary> entry : values.entrySet())
			changes.put(entry.getKey(), Arrays.asList(defaultValues.get(entry.getKey()), entry.getValue()));
		return changes;
	}

	public EntityTable getTable() {
		return (EntityTable) Loader.getInstance(getAttribute(AttrEntityTable));
	}

	public Registry getRegistry() {
		return registry;
	}

	public Entity register() {
		return register(Registry.getRegistry());
	}

	public Entity register(Registry registry) {
		if (this.registry != null)
			this.registry.removeEntity(this);

		if (registry != null)
			registry.addEntity(this);

		this.registry = registry;
		onRegister();
		return this;
	}

	public Entity unregister() {
		return register(null);
	}

	public boolean isNull() {
		guid entityId = getEntityId();
		return entityId == null || entityId.equals(guid.Null);
	}

	public boolean isRegistered() {
		return registry != null;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public Entity setPersistent(boolean persistent) {
		this.persistent = persistent;
		return this;
	}

	public boolean isChanged() {
		return !values.isEmpty();
	}

	public boolean isDeleted() {
		return deleted;
	}

	public Entity create() {
		return create(guid.create());
	}

	public Entity create(guid entityId) {
		if (!isNull())
			throw new RuntimeException("Entity.create(): already created with Id '" + getEntityId() + "'");
		set(EntityId, entityId);
		onCreate();
		return this;
	}

	public Entity delete() {
		this.deleted = true;
		onDelete();
		return this;
	}

	public Set<string> keys() {
		return values().keySet();
	}

	public Map<string, primary> values() {
		Map<string, primary> values = new RLinkedHashMap<string, primary>();
		values.putAll(defaultValues);
		values.putAll(values);
		return values;
	}

	public Map<string, primary> values(Collection<string> keys) {
		Map<string, primary> values = new RLinkedHashMap<string, primary>();

		for (string key : keys)
			values.put(key, get(key));

		return values;
	}

	public boolean has(string key) {
		return values.containsKey(key) || defaultValues.containsKey(key);
	}

	public boolean isChanged(string key) {
		return values.containsKey(key);
	}

	public primary get(string key) {
		return values.getOrDefault(key, defaultValues.get(key));
	}

	public Entity init(string key, primary value) {
		defaultValues.put(key, value);
		initialized = true;
		return this;
	}

	public Entity init(Map<string, primary> values) {
		defaultValues.putAll(values);
		initialized = true;
		return this;
	}

	public Entity set(string key, primary value) {
		initialized = true;

		if (EntityId.equals(key)) {
			defaultValues.put(EntityId, value);
			return this;
		}

		primary currentValue = values.get(key);
		if (currentValue == null)
			currentValue = defaultValues.get(key);

		if (value != currentValue && (value == null || !value.equals(currentValue))) {
			values.put(key, value);
			onChange(key);
		}

		return this;
	}

	public Entity set(string key, org.zenframework.z8.server.json.parser.JsonObject value) {
		return set(key, value != null ? new string(value.toString()) : null);
	}

	public Entity set(string key, org.zenframework.z8.server.json.parser.JsonArray value) {
		return set(key, value != null ? new string(value.toString()) : null);
	}

	public Entity set(Map<string, primary> values) {
		for (Map.Entry<string, primary> entry : values.entrySet())
			set(entry.getKey(), entry.getValue());
		return this;
	}

	public Entity set(string linkKey, Entity entity) {
		return set(linkKey, entity != null ? entity.getEntityId() : guid.Null);
	}

	public Entity touch(string key) {
		onChange(key);
		return this;
	}

	public Entity touch(Collection<string> keys) {
		for (string key : keys)
			touch(key);
		return this;
	}

	public Entity flushChanges() {
		defaultValues.putAll(values);
		values.clear();
		return this;
	}

	public string getString(string key) {
		return (string) get(key);
	}

	public integer getInt(string key) {
		return (integer) get(key);
	}

	public guid getGuid(string key) {
		primary value = get(key);
		return value == null || value instanceof guid ? (guid) value : new guid(value.toString());
	}

	public bool getBool(string key) {
		primary value = get(key);
		return value == null || value instanceof bool ? (bool) value : new bool(value.toString());
	}

	public date getDate(string key) {
		primary value = get(key);
		return value == null || value instanceof date ? (date) value : new date(value.toString());
	}

	public datespan getDatespan(string key) {
		primary value = get(key);
		return value == null || value instanceof datespan ? (datespan) value : new datespan(value.toString());
	}

	public org.zenframework.z8.server.json.parser.JsonObject getJsonObject(string key) {
		string value = getString(key);
		return value != null ? new org.zenframework.z8.server.json.parser.JsonObject(value.get()) : null;
	}

	public org.zenframework.z8.server.json.parser.JsonArray getJsonArray(string key) {
		string value = getString(key);
		return value != null ? new org.zenframework.z8.server.json.parser.JsonArray(value.get()) : null;
	}

	public Entity getEntity(string linkKey) {
		return getEntity(getGuid(linkKey));
	}

	public Entity getEntity(guid entityId) {
		return registry.getEntity(entityId);
	}

	public guid getEntityId() {
		return getGuid(EntityId);
	}

	public Entity setEntityId(guid entityId) {
		set(EntityId, entityId);
		return this;
	}

	public guid getOriginId() {
		return getGuid(OriginId);
	}

	public Entity setOriginId(guid originId) {
		set(OriginId, originId);
		return this;
	}

	public Entity getOrigin() {
		return getEntity(getOriginId());
	}

	public void visitEntities(Visitor visitor) {
		registry.visitEntities(visitor);
	}

	public void visitEntities(Visitor visitor, Filter filter) {
		registry.visitEntities(visitor, filter);
	}

	public Map<guid, Entity> findEntities(Filter filter) {
		return registry.findEntities(filter);
	}

	public Entity findEntity(Filter filter) {
		return registry.findEntity(filter);
	}

	public Entity findCopy(guid originId) {
		return registry.findCopy(originId);
	}

	public Entity copy(boolean withEntityId) {
		Entity copy = copy();

		if (withEntityId)
			copy.set(EntityId, getEntityId());

		return copy;
	}

	public Entity copy() {
		return z8_copy().get();
	}

	private Entity copy0() {
		Entity copy = (Entity) getCLASS().clone(getContainer()).get();
		copy.set(defaultValues).set(values).set(EntityId, (guid) null);
		copy.initialized = initialized;
		copy.setOriginId(getEntityId());
/*
		if (!isNull() && registry != null) {
			copy.values.put(EntityId, guid.create());
			copy.register(registry);
		}
*/
		return copy;
	}

	public void onChange(string key) {
		z8_onChange(key); /* virtual */
	}

	public void onRegister() {
		z8_onRegister(); /* virtual */
	}

	public void onCreate() {
		z8_onCreate(); /* virtual */
	}

	public void onDelete() {
		z8_onDelete(); /* virtual */
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <T extends IObject> RCollection asRCollection(Collection<T> collection) {
		RCollection result = new RCollection();

		if (collection != null) {
			for(T o : collection)
				result.add(o.getCLASS());
		}

		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <T extends IObject> RLinkedHashMap asRLinkedHashMap(Map<guid, T> map) {
		RLinkedHashMap result = new RLinkedHashMap();

		if (map != null) {
			for(Map.Entry<guid, T> entry : map.entrySet())
				result.put(entry.getKey(), entry.getValue().getCLASS());
		}

		return result;
	}

	static public Collection<guid> asIds(Collection<Entity> entities) {
		Collection<guid> ids = new RCollection<guid>(entities.size(), false);
		for (Entity entity : entities)
			ids.add(entity.getEntityId());
		return ids;
	}

	static public Collection<guid> asGuidValues(Collection<Entity> entities, string field) {
		return asValues(entities, field, new RCollection<guid>(entities.size(), false));
	}

	static public Collection<string> asStringValues(Collection<Entity> entities, string field) {
		return asValues(entities, field, new RCollection<string>(entities.size(), false));
	}

	static public Collection<primary> asValues(Collection<Entity> entities, string field) {
		return asValues(entities, field, new RCollection<primary>(entities.size(), false));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static private <T extends primary> Collection<T> asValues(Collection<Entity> entities, string field, Collection values) {
		for (Entity entity : entities)
			values.add(entity.get(field));
		return values;
	}
/*
	static protected Entity findCopy(Collection<Entity> copies, guid originId) {
		if (originId == null)
			return null;

		for (Entity copy : copies) {
			Entity origin = copy.getOrigin();
			if (origin != null && originId.equals(origin.getEntityId()))
				return copy;
		}

		return null;
	}
*/
	/* BL */

	public bool operatorEqu(Entity.CLASS<? extends Entity> value) {
		return value != null && equals(value.get()) ? bool.True : bool.False;
	}

	public bool operatorNotEqu(Entity.CLASS<? extends Entity> value) {
		return value == null || !equals(value.get()) ? bool.True : bool.False;
	}

	@Override
	public string z8_toString() {
		return new string(toString(false, false, false));
	}

	public string z8_toString(bool defaults, bool values, bool changes) {
		return new string(toString(defaults.get(), values.get(), changes.get()));
	}

	@SuppressWarnings("unchecked")
	public EntityTable.CLASS<? extends EntityTable> z8_getTable() {
		return (EntityTable.CLASS<? extends EntityTable>) getTable().getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Registry.CLASS<? extends Registry> z8_getRegistry() {
		return (Registry.CLASS<? extends Registry>) getRegistry().getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_register() {
		return (Entity.CLASS<Entity>) register().getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_register(Registry.CLASS<? extends Registry> registry) {
		return (Entity.CLASS<Entity>) register(registry.get()).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_unregister() {
		return (Entity.CLASS<Entity>) unregister().getCLASS();
	}

	public bool z8_isNull() {
		return isNull() ? bool.True : bool.False;
	}

	public bool z8_isRegistered() {
		return isRegistered() ? bool.True : bool.False;
	}

	public bool z8_isInitialized() {
		return isInitialized() ? bool.True : bool.False;
	}

	public bool z8_isPersistent() {
		return isPersistent() ? bool.True : bool.False;
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_setPersistent(bool persistent) {
		return (Entity.CLASS<Entity>) setPersistent(persistent.get()).getCLASS();
	}

	public bool z8_isChanged() {
		return isChanged() ? bool.True : bool.False;
	}

	public bool z8_isDeleted() {
		return isDeleted() ? bool.True : bool.False;
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_create() {
		return (Entity.CLASS<Entity>) create().getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_create(guid entityId) {
		return (Entity.CLASS<Entity>) create(entityId).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_delete() {
		return (Entity.CLASS<Entity>) delete().getCLASS();
	}

	@SuppressWarnings("rawtypes")
	public RCollection z8_keys() {
		return new RCollection<string>(keys());
	}

	@SuppressWarnings("rawtypes")
	public RLinkedHashMap z8_values() {
		return (RLinkedHashMap) values();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RLinkedHashMap z8_values(RCollection keys) {
		return (RLinkedHashMap) values(keys);
	}

	public bool z8_has(string key) {
		return has(key) ? bool.True : bool.False;
	}

	public bool z8_isChanged(string key) {
		return isChanged(key) ? bool.True : bool.False;
	}

	public primary z8_get(string key) {
		return get(key);
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_init(string key, primary value) {
		return (Entity.CLASS<Entity>) init(key, value).getCLASS();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Entity.CLASS<? extends Entity> z8_init(RLinkedHashMap values) {
		return (Entity.CLASS<Entity>) init(values).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_set(string key, primary value) {
		return (Entity.CLASS<Entity>) set(key, value).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_set(string linkKey, Entity.CLASS<? extends Entity> entity) {
		return (Entity.CLASS<Entity>) set(linkKey, entity != null ? entity.get() : null).getCLASS();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Entity.CLASS<? extends Entity> z8_set(RLinkedHashMap values) {
		return (Entity.CLASS<Entity>) set(values).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_set(string key, JsonObject.CLASS<? extends JsonObject> value) {
		return (Entity.CLASS<Entity>) set(key, value != null ? value.get().get() : null).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_set(string key, JsonArray.CLASS<? extends JsonArray> value) {
		return (Entity.CLASS<Entity>) set(key, value != null ? value.get().get() : null).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_touch(string key) {
		return (Entity.CLASS<Entity>) touch(key).getCLASS();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Entity.CLASS<? extends Entity> z8_touch(RCollection keys) {
		return (Entity.CLASS<Entity>) touch(keys).getCLASS();
	}

	public string z8_getString(string key) {
		return getString(key);
	}

	public integer z8_getInt(string key) {
		return getInt(key);
	}

	public guid z8_getGuid(string key) {
		return getGuid(key);
	}

	public bool z8_getBool(string key) {
		return getBool(key);
	}

	public date z8_getDate(string key) {
		return getDate(key);
	}

	public datespan z8_getDatespan(string key) {
		return getDatespan(key);
	}

	public JsonObject.CLASS<JsonObject> z8_getJsonObject(string key) {
		org.zenframework.z8.server.json.parser.JsonObject value = getJsonObject(key);
		return value != null ? JsonObject.getJsonObject(value) : null;
	}

	public JsonArray.CLASS<JsonArray> z8_getJsonArray(string key) {
		org.zenframework.z8.server.json.parser.JsonArray value = getJsonArray(key);
		return JsonArray.getJsonArray(value);
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_getEntity(string key) {
		Entity entity = getEntity(key);
		return entity != null ? (Entity.CLASS<Entity>) entity.getCLASS() : null;
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_getEntity(guid entityId) {
		Entity entity = getEntity(entityId);
		return entity != null ? (Entity.CLASS<Entity>) entity.getCLASS() : null;
	}

	public guid z8_getEntityId() {
		return getEntityId();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_setEntityId(guid entityId) {
		return (Entity.CLASS<Entity>) setEntityId(entityId).getCLASS();
	}

	public guid z8_getOriginId() {
		return getOriginId();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_setOriginId(guid originId) {
		return (Entity.CLASS<Entity>) setOriginId(originId).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_getOrigin() {
		Entity origin = getOrigin();
		return origin != null ? (Entity.CLASS<Entity>) origin.getCLASS() : null;
	}

	public void z8_visitEntities(Visitor.CLASS<? extends Visitor> visitor) {
		visitEntities(visitor != null ? visitor.get() : null);
	}

	public void z8_visitEntities(Visitor.CLASS<? extends Visitor> visitor, Filter.CLASS<? extends Filter> filter) {
		visitEntities(visitor != null ? visitor.get() : null, filter != null ? filter.get() : null);
	}

	@SuppressWarnings({ "rawtypes" })
	public RLinkedHashMap z8_findEntities(Filter.CLASS<? extends Filter> filter) {
		return Entity.asRLinkedHashMap(findEntities(filter != null ? filter.get() : null));
	}

	@SuppressWarnings({ "unchecked" })
	public Entity.CLASS<? extends Entity> z8_findEntity(Filter.CLASS<? extends Filter> filter) {
		Entity entity = findEntity(filter != null ? filter.get() : null);
		return entity != null ? (Entity.CLASS<? extends Entity>) entity.getCLASS() : null;
	}

	@SuppressWarnings({ "unchecked" })
	public Entity.CLASS<? extends Entity> z8_findCopy(guid originId) {
		Entity copy = findCopy(originId);
		return copy != null ? (Entity.CLASS<? extends Entity>) copy.getCLASS() : null;
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_copy(bool withEntityId) {
		return (Entity.CLASS<Entity>) copy(withEntityId.get()).getCLASS();
	}

	@SuppressWarnings("unchecked")
	/* virtual */ public Entity.CLASS<? extends Entity> z8_copy() {
		return (Entity.CLASS<Entity>) copy0().getCLASS();
	}

	/* virtual */ public void z8_onChange(string key) {}

	/* virtual */ public void z8_onRegister() {}

	/* virtual */ public void z8_onCreate() {}

	/* virtual */ public void z8_onDelete() {}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public RCollection z8_asIds(RCollection entities) {
		return (RCollection) asIds(CLASS.asList(entities));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public RCollection z8_asGuidValues(RCollection entities, string field) {
		return (RCollection) asGuidValues(CLASS.asList(entities), field);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public RCollection z8_asStringValues(RCollection entities, string field) {
		return (RCollection) asStringValues(CLASS.asList(entities), field);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public RCollection z8_asValues(RCollection entities, string field) {
		return (RCollection) asValues(CLASS.asList(entities), field);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public RCollection z8_sort(RCollection entities, string orderBy) {
		Collections.sort(entities, new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				if (o1 == o2)
					return 0;

				Entity.CLASS<? extends Entity> e1 = (Entity.CLASS<? extends Entity>) o1;
				Entity.CLASS<? extends Entity> e2 = (Entity.CLASS<? extends Entity>) o2;
				primary v1 = e1 != null ? e1.get().get(orderBy) : null;
				primary v2 = e2 != null ? e2.get().get(orderBy) : null;

				if (v1 == v2)
					return 0;
				if (v1 == null)
					return -1;
				if (v2 == null)
					return 1;

				return v1.compareTo(v2);
			}

		});
		return entities;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public Entity.CLASS<? extends Entity> z8_find(RCollection entities, guid entityId) {
		for (Entity.CLASS<? extends Entity> entity : (Collection<Entity.CLASS<? extends Entity>>) entities) {
			guid id = entity.get().getEntityId();
			if (id == entityId || id != null && id.equals(entityId))
				return entity;
		}
		return null;
	}
/*
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static protected Entity.CLASS<? extends Entity> z8_findCopy(RCollection copies, guid originId) {
		Entity copy = findCopy(CLASS.asList(copies), originId);
		return copy != null ? (Entity.CLASS<? extends Entity>) copy.getCLASS() : null;
	}
*/
}
