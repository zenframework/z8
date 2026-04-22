package org.zenframework.z8.server.base.entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;

public class EntityTable extends Table {
	public static class CLASS<T extends EntityTable> extends Table.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(EntityTable.class);
		}

		public Object newObject(IObject container) {
			return new EntityTable(container);
		}
	}

	public static final String AttrEntity = "entity";
	public static final String AttrEntityValue = "entityValue";

	private Registry registry = null;
	private final Collection<EntityTable> dependencies = new LinkedList<EntityTable>();

	public EntityTable(IObject container) {
		super(container);
	}

	public EntityTable setRegistry(Registry registry) {
		this.registry = registry;
		return this;
	}

	public Registry getRegistry() {
		return registry != null ? registry : (registry = Registry.getRegistry());
	}

	private EntityTable setDependencies(Collection<EntityTable> dependencies) {
		this.dependencies.clear();
		this.dependencies.addAll(dependencies);
		return this;
	}

	public Entity newEntity(Map<string, primary> values) {
		Entity entity = z8_newEntity((RLinkedHashMap<string, primary>) values).get(); /* virtual */
		if (!entity.isInitialized())
			entity.init(values);
		return entity;
	}

	public Entity readEntity(guid entityId) {
		return readEntity(entityId, Collections.<EntityTable>emptyList());
	}

	public Entity readEntity(guid entityId, Collection<EntityTable> dependencies) {
		return entityId != null && !entityId.equals(guid.Null) && setDependencies(dependencies)
				.readFirst(collectEntityFields(dependencies), new Rel(recordId.get(), Operation.Eq, new SqlConst(entityId)))
						? getEntity() : null;
	}

	public Entity readEntity(SqlToken where) {
		return readEntity(Collections.<EntityTable>emptyList(), where);
	}

	public Entity readEntity(Collection<EntityTable> dependencies, SqlToken where) {
		return setDependencies(dependencies).readFirst(collectEntityFields(dependencies), where) ? getEntity() : null;
	}

	public Entity readEntity(Collection<EntityTable> dependencies, Collection<Field> sortFields, SqlToken where) {
		return setDependencies(dependencies).readFirst(collectEntityFields(dependencies), sortFields, where) ? getEntity() : null;
	}

	public EntityTable readEntities() {
		return readEntities(Collections.<EntityTable>emptyList(), null);
	}

	public EntityTable readEntities(SqlToken where) {
		return readEntities(Collections.<EntityTable>emptyList(), where);
	}

	public EntityTable readEntities(Collection<EntityTable> dependencies, SqlToken where) {
		return readEntities(dependencies, Collections.<Field>emptyList(), where);
	}

	public EntityTable readEntities(Collection<EntityTable> dependencies, Collection<Field> sortFields, SqlToken where) {
		setDependencies(dependencies).read(collectEntityFields(dependencies), sortFields, Arrays.asList(recordId.get()), where, null);
		return this;
	}

	public Entity getEntity() {
		Registry registry = getRegistry();
		Entity entity = registry.getEntity(recordId.get().get());

		if (entity == null) {
			entity = initDependencies(newEntity(getEntityValues()).setPersistent(true).register(registry), dependencies);
			entity.setAttribute(Entity.AttrEntityTable, classId());
			afterRead(entity);
		}

		return entity;
	}

	public Map<guid, Entity> getEntities() {
		Map<guid, Entity> result = new HashMap<guid, Entity>();

		while (next()) {
			Entity entity = getEntity();
			result.put(entity.getEntityId(), entity);
		}

		return result;
	}

	public Entity create(Entity entity) {
		guid entityId = entity.getEntityId();

		if (entityId == null || entityId.equals(guid.Null))
			entity.setEntityId(entityId = guid.create());

		initFields(entity, false);

		beforeCreate(entity);
		create(entityId);
		afterCreate(entity);

		return entity.flushChanges().setPersistent(true);
	}

	public Entity update(Entity entity) {
		return update(entity, true);
	}

	public Entity update(Entity entity, boolean changedOnly) {
		initFields(entity, changedOnly);

		beforeUpdate(entity);
		update(entity.getEntityId());
		afterUpdate(entity);

		return entity.flushChanges();
	}

	public Entity destroy(Entity entity) {
		beforeDestroy(entity);
		destroy(entity.getEntityId());
		afterDestroy(entity);

		return entity.setPersistent(false);
	}

	public boolean exists(Entity entity) {
		return hasRecord(entity.getEntityId());
	}

	public List<Field> getEntityFields() {
		List<Field> entityFields = new LinkedList<Field>();
		for (Field field : getDataFields())
			if (field.hasAttribute(AttrEntityValue))
				entityFields.add(field);
		return entityFields;
	}

	public List<EntityTable> getDependencies() {
		List<EntityTable> dependencies = new LinkedList<EntityTable>();
		for (Field.CLASS<Field> field : getLinks()) {
			ILink link = (ILink) field;
			Query query = link.getQuery();
			if (query instanceof EntityTable && query.hasAttribute(AttrEntity))
				dependencies.add((EntityTable) query);
		}
		return dependencies;
	}

	@SuppressWarnings("unchecked")
	public void beforeCreate(Entity entity) {
		z8_beforeCreate((Entity.CLASS<? extends Entity>) entity.getCLASS()); /* virtual */
	}

	@SuppressWarnings("unchecked")
	public void afterCreate(Entity entity) {
		z8_afterCreate((Entity.CLASS<? extends Entity>) entity.getCLASS()); /* virtual */
	}

	@SuppressWarnings("unchecked")
	public void beforeUpdate(Entity entity) {
		z8_beforeUpdate((Entity.CLASS<? extends Entity>) entity.getCLASS()); /* virtual */
	}

	@SuppressWarnings("unchecked")
	public void afterUpdate(Entity entity) {
		z8_afterUpdate((Entity.CLASS<? extends Entity>) entity.getCLASS()); /* virtual */
	}

	@SuppressWarnings("unchecked")
	public void beforeDestroy(Entity entity) {
		z8_beforeDestroy((Entity.CLASS<? extends Entity>) entity.getCLASS()); /* virtual */
	}

	@SuppressWarnings("unchecked")
	public void afterDestroy(Entity entity) {
		z8_afterDestroy((Entity.CLASS<? extends Entity>) entity.getCLASS()); /* virtual */
	}

	@SuppressWarnings("unchecked")
	public void afterRead(Entity entity) {
		z8_afterRead((Entity.CLASS<? extends Entity>) entity.getCLASS()); /* virtual */
	}

	public EntityTable initFields(Entity entity, boolean changedOnly) {
		for (Field field : getEntityFields()) {
			if (field == recordId.get())
				continue;
			string index = new string(getRelativeId(field));
			primary value = entity.get(index);
			if (value != null && (!changedOnly || entity.isChanged(index)))
				field.set(value);
		}
		return this;
	}

	private Entity initDependencies(Entity entity, Collection<EntityTable> dependencies) {
		Registry registry = getRegistry();

		for (EntityTable dependencyTable : dependencies) {
			if (!isMember(this, dependencyTable))
				continue;
			guid dependencyId = dependencyTable.recordId();
			Entity dependency = !dependencyId.equals(guid.Null) ? registry.getEntity(dependencyId) : null;
			if (dependency == null && !dependencyId.equals(guid.Null)) {
				dependency = dependencyTable.newEntity(dependencyTable.getEntityValues()).setPersistent(true).register(registry);
				dependencyTable.initDependencies(dependency, filterMembers(dependencyTable, dependencies));
			}
		}

		return entity;
	}

	private RLinkedHashMap<string, primary> getEntityValues() {
		RLinkedHashMap<string, primary> values = new RLinkedHashMap<string, primary>();
		List<Field> entityFields = getEntityFields();
		if (!entityFields.contains(recordId.get()))
			entityFields.add(recordId.get());
		for (Field field : entityFields)
			values.put(new string(getRelativeId(field)), field.get());
		return values;
	}

	public Collection<Field> collectEntityFields(Collection<EntityTable> dependencies) {
		Collection<Field> fields = new HashSet<Field>();
		fields.addAll(getEntityFields());
		for (EntityTable dependency : dependencies) {
			fields.addAll(dependency.getEntityFields());
			fields.add(dependency.recordId.get());
		}
		return fields;
	}

	private String getRelativeId(IObject object) {
		String path = id();
		return object.id().substring(path.isEmpty() ? 0 : path.length() + 1);
	}

	private boolean isMember(IObject parent, IObject member) {
		String parentId = parent.id();
		return member.id().equals(parentId.isEmpty() ? member.index() : parentId + '.' + member.index());
	}

	private <T extends IObject> Collection<T> filterMembers(IObject parent, Collection<T> members) {
		String id = parent.id();
		if (id.isEmpty())
			return members;

		id += '.';

		Collection<T> result = new HashSet<T>();
		for (T member : members)
			if (member.id().startsWith(id))
				result.add(member);

		return result;
	}

	/* BL */

	@SuppressWarnings("unchecked")
	public EntityTable.CLASS<? extends EntityTable> z8_setRegistry(Registry.CLASS<? extends Registry> registry) {
		return (EntityTable.CLASS<? extends EntityTable>) setRegistry(registry != null ? registry.get() : null).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Registry.CLASS<? extends Registry> z8_getRegistry() {
		return (Registry.CLASS<? extends Registry>) getRegistry().getCLASS();
	}

	/* virtual */ public Entity.CLASS<? extends Entity> z8_newEntity() {
		return new Entity.CLASS<Entity>(this);
	}

	@SuppressWarnings({ "rawtypes" })
	/* virtual */ public Entity.CLASS<? extends Entity> z8_newEntity(RLinkedHashMap values) {
		return z8_newEntity();
	}

	@SuppressWarnings({ "unchecked" })
	public Entity.CLASS<? extends Entity> z8_readEntity(guid entityId) {
		Entity entity = readEntity(entityId);
		return entity != null ? (Entity.CLASS<? extends Entity>) entity.getCLASS() : null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Entity.CLASS<? extends Entity> z8_readEntity(guid entityId, RCollection dependencies) {
		Entity entity = readEntity(entityId, CLASS.asList(dependencies));
		return entity != null ? (Entity.CLASS<? extends Entity>) entity.getCLASS() : null;
	}

	@SuppressWarnings({ "unchecked" })
	public Entity.CLASS<? extends Entity> z8_readEntity(sql_bool where) {
		Entity entity = readEntity(where);
		return entity != null ? (Entity.CLASS<? extends Entity>) entity.getCLASS() : null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Entity.CLASS<? extends Entity> z8_readEntity(RCollection dependencies, sql_bool where) {
		Entity entity = readEntity(CLASS.asList(dependencies), where);
		return entity != null ? (Entity.CLASS<? extends Entity>) entity.getCLASS() : null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Entity.CLASS<? extends Entity> z8_readEntity(RCollection dependencies, RCollection sortFields, sql_bool where) {
		Entity entity = readEntity(CLASS.asList(dependencies), CLASS.asList(sortFields), where);
		return entity != null ? (Entity.CLASS<? extends Entity>) entity.getCLASS() : null;
	}

	@SuppressWarnings("unchecked")
	public EntityTable.CLASS<? extends EntityTable> z8_readEntities() {
		return (EntityTable.CLASS<? extends EntityTable>) readEntities().getCLASS();
	}

	@SuppressWarnings("unchecked")
	public EntityTable.CLASS<? extends EntityTable> z8_readEntities(sql_bool where) {
		return (EntityTable.CLASS<? extends EntityTable>) readEntities(where).getCLASS();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public EntityTable.CLASS<? extends EntityTable> z8_readEntities(RCollection dependencies, sql_bool where) {
		return (EntityTable.CLASS<? extends EntityTable>) readEntities(CLASS.asList(dependencies), where).getCLASS();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public EntityTable.CLASS<? extends EntityTable> z8_readEntities(RCollection dependencies, RCollection sortFields, sql_bool where) {
		return (EntityTable.CLASS<? extends EntityTable>) readEntities(CLASS.asList(dependencies), CLASS.asList(sortFields), where).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_getEntity() {
		return (Entity.CLASS<? extends Entity>) getEntity().getCLASS();
	}

	@SuppressWarnings("rawtypes")
	public RLinkedHashMap z8_getEntities() {
		return Entity.asRLinkedHashMap(getEntities());
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_create(Entity.CLASS<? extends Entity> entity) {
		return (Entity.CLASS<? extends Entity>) create(entity.get()).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_update(Entity.CLASS<? extends Entity> entity) {
		return (Entity.CLASS<? extends Entity>) update(entity.get()).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Entity.CLASS<? extends Entity> z8_destroy(Entity.CLASS<? extends Entity> entity) {
		return (Entity.CLASS<? extends Entity>) destroy(entity.get()).getCLASS();
	}

	public bool z8_exists(Entity.CLASS<? extends Entity> entity) {
		return exists(entity.get()) ? bool.True : bool.False;
	}

	/* virtual */ public void z8_beforeCreate(Entity.CLASS<? extends Entity> entity) {}

	/* virtual */ public void z8_afterCreate(Entity.CLASS<? extends Entity> entity) {}

	/* virtual */ public void z8_beforeUpdate(Entity.CLASS<? extends Entity> entity) {}

	/* virtual */ public void z8_afterUpdate(Entity.CLASS<? extends Entity> entity) {}

	/* virtual */ public void z8_beforeDestroy(Entity.CLASS<? extends Entity> entity) {}

	/* virtual */ public void z8_afterDestroy(Entity.CLASS<? extends Entity> entity) {}

	/* virtual */ public void z8_afterRead(Entity.CLASS<? extends Entity> entity) {}

	@SuppressWarnings("unchecked")
	public EntityTable.CLASS<? extends EntityTable> z8_initFields(Entity.CLASS<? extends Entity> entity, bool changedOnly) {
		return (EntityTable.CLASS<? extends EntityTable>) initFields(entity.get(), changedOnly.get()).getCLASS();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RCollection z8_collectEntityFields(RCollection dependencies) {
		return Entity.asRCollection(collectEntityFields(CLASS.asList(dependencies)));
	}
}
