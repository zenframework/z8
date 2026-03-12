package org.zenframework.z8.server.base.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;

public class Registry extends OBJECT {
	public static class CLASS<T extends Registry> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(OBJECT.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Registry(container);
		}
	}

	private static final ThreadLocal<Registry> LOCAL = new ThreadLocal<Registry>();

	private IRequest request = null;
	private boolean logEnabled = false;

	public Registry(IObject container) {
		super(container);
	}

	public static Registry getRegistry() {
		Registry registry = LOCAL.get();

		if (registry == null) {
			registry = new Registry.CLASS<Registry>(null).get();
			LOCAL.set(registry);
		}

		registry.updateRequest();

		return registry;
	}

	private final Map<String, EntityTable> tables = new HashMap<String, EntityTable>();
	private final Map<guid, Entity> entities = new HashMap<guid, Entity>();
	private final Collection<Entity> newEntities = new LinkedList<Entity>();

	private boolean checked = true;

	public Registry setLogEnabled(boolean logEnabled) {
		this.logEnabled = logEnabled;
		return this;
	}

	public boolean isLogEnabled() {
		return logEnabled;
	}

	public Entity getEntity(guid entityId) {
		if (entityId == null || entityId.equals(guid.Null))
			return null;

		checkEntities();

		return entities.get(entityId);
	}

	public Entity addEntity(Entity entity) {
		guid entityId = entity.getEntityId();

		if (entityId == null || entityId.equals(guid.Null)) {
			newEntities.add(entity);
			checked = false;
		} else if (!entities.containsKey(entityId)) {
			entities.put(entityId, entity);
		} else {
			throw new RuntimeException("Duplicate entity " + entity);
		}

		return entity;
	}

	public void visitEntities(Visitor visitor) {
		visitEntities(visitor, null);
	}

	public void visitEntities(Visitor visitor, Filter filter) {
		if (visitor == null)
			return;

		checkEntities();

		for (Entity entity : entities.values()) {
			if (filter == null || filter.accept(entity))
				visitor.visit(entity);
		}
	}

	public Map<guid, Entity> findEntities(Filter filter) {
		return findEntities(filter, -1);
	}

	private Map<guid, Entity> findEntities(Filter filter, int limit) {
		Map<guid, Entity> result = new HashMap<guid, Entity>();

		if (filter == null)
			return result;

		checkEntities();

		for (Map.Entry<guid, Entity> entry : entities.entrySet()) {
			if (filter.accept(entry.getValue()))
				result.put(entry.getKey(), entry.getValue());
			if (result.size() == limit)
				break;
		}

		return result;
	}

	public Entity findEntity(Filter filter) {
		Map<guid, Entity> entities = findEntities(filter, 1);
		return entities.size() > 0 ? entities.values().iterator().next() : null;
	}

	public Entity findCopy(guid originId) {
		return originId != null ? findEntity(new Filter(null) {
			@Override
			public boolean accept(Entity entity) {
				return originId.equals(entity.getOriginId());
			}
		}) : null;
	}

	public Entity removeEntity(Entity entity) {
		newEntities.remove(entity);
		entities.remove(entity.getEntityId());
		return entity;
	}

	public Registry reset() {
		newEntities.clear();
		entities.clear();
		tables.clear();
		checked = true;
		return this;
	}

	public Registry flush() {
		if (logEnabled)
			log(">>> REGISTRY TRANSACTION BEGIN >>>");

		checkEntities();

		for (Map.Entry<guid, Entity> entry : entities.entrySet()) {
			if (!entry.getValue().isPersistent() && !entry.getValue().isDeleted()) {
				EntityTable table = getTable(entry.getValue().setPersistent(true));
				if (logEnabled)
					log("REGISTRY CREATE: " + entry.getValue().toString() + " in " + table.getClass().getCanonicalName() + " (" + table.name() + ')');
				table.create(entry.getKey());
			}
		}

		ConnectionManager.get().flush();

		for (Entity entity : entities.values()) {
			if (entity.isChanged()) {
				EntityTable table = getTable(entity);
				if (logEnabled)
					log("REGISTRY UPDATE: " + entity.toString(false, false, true) + " in " + table.getClass().getCanonicalName() + " (" + table.name() + ')');
				table.update(entity, true);
			}
		}

		ConnectionManager.get().flush();

		for (Entity entity : entities.values()) {
			if (entity.isPersistent() && entity.isDeleted()) {
				EntityTable table = getTable(entity);
				if (logEnabled)
					log("REGISTRY DESTROY: " + entity.toString() + " in " + table.getClass().getCanonicalName() + " (" + table.name() + ')');
				table.destroy(entity);
			}
		}

		ConnectionManager.get().flush();

		if (logEnabled)
			log("<<< REGISTRY TRANSACTION END <<<");

		return this;
	}

	private void updateRequest() {
		IRequest request = ApplicationServer.getRequest();
		if (this.request != request) {
			this.request = request;
			reset();
		}
	}

	private void checkEntities() {
		if (checked)
			return;

		checked = true;

		Iterator<Entity> it = newEntities.iterator();
		while (it.hasNext()) {
			Entity entity = it.next();
			if (!entity.isNull()) {
				entities.put(entity.getEntityId(), entity);
				it.remove();
			}
		}
	}

	private EntityTable getTable(Entity entity) {
		String className = entity.getAttribute(Entity.AttrEntityTable);
		EntityTable table = tables.get(className);
		if (table == null)
			tables.put(className, table = entity.getTable());
		return table;
	}

	private void log(String message) {
		ApplicationServer.getMonitor().logInfo(message);
		//java.lang.System.out.println(message);
	}

	/* BL */

	@SuppressWarnings("unchecked")
	public static Registry.CLASS<? extends Registry> z8_getRegistry() {
		return (Registry.CLASS<? extends Registry>) getRegistry().getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Registry.CLASS<? extends Registry> z8_setLogEnabled(bool logEnabled) {
		return (Registry.CLASS<? extends Registry>) setLogEnabled(logEnabled.get()).getCLASS();
	}

	public bool z8_isLogEnabled() {
		return isLogEnabled() ? bool.True : bool.False;
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_getEntity(guid entityId) {
		Entity entity = getEntity(entityId);
		return entity != null ? (Entity.CLASS<? extends Entity>) entity.getCLASS() : null;
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
	public Registry.CLASS<? extends Registry> z8_reset() {
		return (Registry.CLASS<? extends Registry>) reset().getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Registry.CLASS<? extends Registry> z8_flush() {
		return (Registry.CLASS<? extends Registry>) flush().getCLASS();
	}
}
