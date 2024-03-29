package org.zenframework.z8.server.base.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionEvent;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.string.Like;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.EventsLevel;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;

public class TreeTable extends Table implements Connection.Listener {
	static public class fieldNames {
		public final static String ParentId = "ParentId";
		public final static String Path = "Path";

		public final static String Parent1 = "Parent1";
		public final static String Parent2 = "Parent2";
		public final static String Parent3 = "Parent3";
		public final static String Parent4 = "Parent4";
		public final static String Parent5 = "Parent5";
		public final static String Parent6 = "Parent6";
	}

	public static class CLASS<T extends TreeTable> extends Table.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Table.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new TreeTable(container);
		}
	}

	private boolean updating = false;

	public Link.CLASS<? extends Link> parentId = new Link.CLASS<Link>(this);
	public StringField.CLASS<? extends StringField> path = new StringField.CLASS<StringField>(this);

	public GuidField.CLASS<? extends GuidField> parent1 = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> parent2 = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> parent3 = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> parent4 = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> parent5 = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> parent6 = new GuidField.CLASS<GuidField>(this);

	@SuppressWarnings("rawtypes")
	private GuidField.CLASS[] parentLinks = { parent1, parent2, parent3, parent4, parent5, parent6 };

	static private final ThreadLocal<Map<String, Map<guid, String>>> idToPathMap = new ThreadLocal<Map<String, Map<guid, String>>>();

	public TreeTable(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(parentId);
		objects.add(path);

		objects.add(parent1);
		objects.add(parent2);
		objects.add(parent3);
		objects.add(parent4);
		objects.add(parent5);
		objects.add(parent6);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		parentId.setName(fieldNames.ParentId);
		parentId.setAttribute(ParentKey, "");
		parentId.setIndex("parentId");
		parentId.get().indexed = bool.True;

		path.setName(fieldNames.Path);
		path.setIndex("path");
		path.setSystem(true);
		path.get().length = new integer(1000);
		path.get().indexed = bool.True;

		parent1.setName(fieldNames.Parent1);
		parent1.setIndex("parent1");
		parent1.setSystem(true);
		parent1.get().indexed = bool.True;

		parent2.setName(fieldNames.Parent2);
		parent2.setIndex("parent2");
		parent2.setSystem(true);
		parent2.get().indexed = bool.True;

		parent3.setName(fieldNames.Parent3);
		parent3.setIndex("parent3");
		parent3.setSystem(true);
		parent3.get().indexed = bool.True;

		parent4.setName(fieldNames.Parent4);
		parent4.setIndex("parent4");
		parent4.setSystem(true);
		parent4.get().indexed = bool.True;

		parent5.setName(fieldNames.Parent5);
		parent5.setIndex("parent5");
		parent5.setSystem(true);
		parent5.get().indexed = bool.True;

		parent6.setName(fieldNames.Parent6);
		parent6.setIndex("parent6");
		parent6.setSystem(true);
		parent6.get().indexed = bool.True;
	}

	@Override
	public Field[] parentKeys() {
		return new Field[] { parent1.get(), parent2.get(), parent3.get(), parent4.get(), parent5.get(), parent6.get() };
	}

	private guid[] parsePath(String path) {
		String[] levels = path.split("\\.");

		List<guid> result = new ArrayList<guid>();

		for(String level : levels)
			result.add(new guid(level));

		return result.toArray(new guid[0]);
	}

	private void setParents(guid[] path) {
		for(int i = 0; i < parentLinks.length; i++) {
			guid parent = i < path.length ? path[i] : guid.Null;
			Field field = (Field)parentLinks[i].get();
			field.set(parent);
		}
	}

	@Override
	public void on(Connection connection, ConnectionEvent event) {
		idToPathMap.remove();
	}

	@Override
	public void beforeCreate(guid recordId) {
		super.beforeCreate(recordId);

		if(!ApplicationServer.systemEventsEnabled())
			return;

		Field parentKey = this.parentId.get();

		guid parentId = parentKey.changed() ? parentKey.guid() : guid.Null;
		recordId = primaryKey().guid();

		if(recordId.isNull())
			return;

		Connection connection = ConnectionManager.get();

		String path = null;

		if(connection.inTransaction()) {
			Map<String, Map<guid, String>> map = idToPathMap.get();
			if(map == null) {
				map = new HashMap<String, Map<guid, String>>();
				idToPathMap.set(map);
				connection.addListener(this);
			}

			String tableName = name();
			Map<guid, String> pathMap = map.get(tableName);

			if(pathMap == null) {
				pathMap = new HashMap<guid, String>();
				map.put(tableName, pathMap);
			}

			path = pathMap.get(parentId);

			if(path == null) {
				path = getPath(parentId);
				pathMap.put(parentId, path);
			}

			path = path + (path.isEmpty() ? "" : ".") + recordId;
			pathMap.put(recordId, path);
		} else {
			path = getPath(parentId);
			path = path + (path.isEmpty() ? "" : ".") + recordId;
		}

		setParents(parsePath(path));
		this.path.get().set(path);
	}

	@Override
	public void beforeUpdate(guid recordId) {
		if(updating)
			return;

		super.beforeUpdate(recordId);

		if(!ApplicationServer.systemEventsEnabled())
			return;

		Field parentKey = this.parentId.get();
		if(!parentKey.changed())
			return;

		guid parentId = parentKey.guid();
		String newPath = getPath(parentId);
		newPath = newPath + (newPath.isEmpty() ? "" : ".") + recordId;

		String oldPath = getPath(recordId);

		Connection connection = ConnectionManager.get();

		try {
			updating = true;
			saveState();

			connection.beginTransaction();

			parentKey.set(parentId);
			update(recordId);

			Field path = this.path.get();

			SqlToken where = new Like(path, new sql_string(oldPath + '%'));

			read(Arrays.asList(path), where);

			while(next()) {
				String child = oldPath.isEmpty() ? newPath : path.string().get().replace(oldPath, newPath);
				setParents(parsePath(child));
				path.set(new string(child));
				update(recordId());
			}

			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		} finally {
			restoreState();
			updating = false;
		}
	}

	public String getPath(guid id) {
		if(id.isNull())
			return "";

		try {
			saveState();
			ApplicationServer.setEventsLevel(EventsLevel.SYSTEM);

			Field pathField = this.path.get();

			if(!readRecord(id, Arrays.asList(pathField)))
				throw new RuntimeException(Query.strings.ReadError);

			return pathField.string().get();
		} finally {
			restoreState();
			ApplicationServer.restoreEventsLevel();
		}
	}

	public RCollection<guid> z8_getParentsOf(guid recordId) {
		guid[] parents = parsePath(getPath(recordId));
		parents = Arrays.copyOf(parents, parents.length - 1);
		return new RCollection<guid>(parents);
	}

	public RCollection<guid> z8_getPathTo(guid recordId) {
		guid[] parents = parsePath(getPath(recordId));
		return new RCollection<guid>(parents);
	}

	public RCollection<guid> z8_getPath() {
		return new RCollection<guid>(parsePath(this.path.get().z8_get().get()));
	}
}