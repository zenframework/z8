package org.zenframework.z8.server.base.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class TreeTable extends Table {
	static public class names {
		public final static String ParentId = "ParentId";
		public final static String Path = "Path";

		public final static String Root = "Root";
		public final static String Parent1 = "Parent1";
		public final static String Parent2 = "Parent2";
		public final static String Parent3 = "Parent3";
		public final static String Parent4 = "Parent4";
		public final static String Parent5 = "Parent5";
		public final static String Parent6 = "Parent6";
	}

	static public class strings {
		public final static String ParentId = "TreeTable.parentId";
		public final static String Path = "TreeTable.path";
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

	public GuidField.CLASS<? extends GuidField> parentId = new GuidField.CLASS<GuidField>(this);
	public StringField.CLASS<? extends StringField> path = new StringField.CLASS<StringField>(this);

	public GuidField.CLASS<? extends GuidField> parent1 = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> parent2 = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> parent3 = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> parent4 = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> parent5 = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> parent6 = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> root = new GuidField.CLASS<GuidField>(this);

	public bool keepIntegrity = new bool(true);

	@SuppressWarnings("rawtypes")
	private GuidField.CLASS[] parentLinks = { parent1, parent2, parent3, parent4, parent5, parent6 };

	public TreeTable(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		parentId.setName(names.ParentId);
		parentId.setIndex("parentId");
		parentId.setDisplayName(strings.ParentId);
		parentId.setAttribute(ParentKey, "");
		parentId.get().indexed = new bool(true);

		path.setName(names.Path);
		path.setIndex("path");
		path.setDisplayName(strings.Path);

		parent1.setName(names.Parent1);
		parent1.setIndex("parent1");
		parent1.get().indexed = new bool(true);

		parent2.setName(names.Parent2);
		parent2.setIndex("parent2");
		parent2.get().indexed = new bool(true);

		parent3.setName(names.Parent3);
		parent3.setIndex("parent3");
		parent3.get().indexed = new bool(true);

		parent4.setName(names.Parent4);
		parent4.setIndex("parent4");
		parent4.get().indexed = new bool(true);

		parent5.setName(names.Parent5);
		parent5.setIndex("parent5");
		parent5.get().indexed = new bool(true);

		parent6.setName(names.Parent6);
		parent6.setIndex("parent6");
		parent6.get().indexed = new bool(true);

		root.setName(names.Root);
		root.setIndex("root");
		root.get().indexed = new bool(true);

		registerDataField(parentId);
		registerDataField(path);

		registerDataField(parent1);
		registerDataField(parent2);
		registerDataField(parent3);
		registerDataField(parent4);
		registerDataField(parent5);
		registerDataField(parent6);
		registerDataField(root);

		parentId.setSystem(true);
		path.setSystem(true);

		path.get().length = new integer(1000);
	}

	private guid[] parsePath(String path) {
		String[] levels = path.split("\\.");

		List<guid> result = new ArrayList<guid>();

		for(String level : levels)
			result.add(new guid(level));

		return result.toArray(new guid[0]);
	}

	@Override
	public void beforeCreate(guid recordId, guid parentId) {
		super.beforeCreate(recordId, parentId);

		parentId = parentKey().guid();
		recordId = primaryKey().guid();

		if(keepIntegrity.get() && parentId != null) {
			String path = recordId.toString();
			String parentPath = "";

			Field pathField = this.path.get();

			if(!parentId.isNull()) {
				try {
					saveState();

					Collection<Field> fields = Arrays.asList(pathField);
					readExistingRecord(parentId, fields);

					parentPath = pathField.get().string().get();
					path = parentPath + (parentPath.isEmpty() ? "" : ".") + recordId;
				} finally {
					restoreState();
				}

				guid[] parents = parsePath(parentPath);

				for(int i = 0; i < parentLinks.length && i < parents.length; i++) {
					guid parent = parents[i];
					Field link = (Field)parentLinks[i].get();
					link.set(parent);
				}
				root.get().set(parents.length > 0 ? parents[0] : guid.NULL);
			} else
				root.get().set(recordId);

			pathField.set(new string(path.toUpperCase()));
		}
	}

	private void readExistingRecord(guid parentId, Collection<Field> fields) {
		if(!readRecord(parentId, fields))
			throw new RuntimeException(Query.strings.ReadError);
	}

	public guid[] getPath(guid id) {
		if(id.isNull()) {
			return new guid[0];
		}

		String parentPath = "";

		Field pathField = this.path.get();

		try {
			saveState();

			Collection<Field> fields = Arrays.asList(pathField);
			readExistingRecord(id, fields);

			parentPath = pathField.get().string().get();
		} finally {
			restoreState();
		}

		return parsePath(parentPath);
	}

	public guid getParent(guid recordId) {
		try {
			saveState();

			Field parentId = this.parentId.get();

			Collection<Field> fields = Arrays.asList(parentId);
			readExistingRecord(recordId, fields);

			return parentId.get().guid();
		} finally {
			restoreState();
		}
	}

	public RCollection<guid> z8_getParentsOf(guid recordId) {
		guid[] parents = getPath(recordId);
		parents = Arrays.copyOf(parents, parents.length - 1);

		return new RCollection<guid>(parents);
	}

	public RCollection<guid> z8_getPathTo(guid recordId) {
		guid[] parents = getPath(recordId);
		return new RCollection<guid>(parents);
	}

	public RCollection<guid> z8_getPath() {
		return new RCollection<guid>(parsePath(this.path.get().z8_get().get()));
	}
}