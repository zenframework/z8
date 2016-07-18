package org.zenframework.z8.server.base.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class TreeTable extends Table {
	static public class names {
		public final static String ParentId = "ParentId";
		public final static String Depth = "Depth";
		public final static String ChildrenCount = "Children";
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
		public final static String Depth = "TreeTable.depth";
		public final static String ChildrenCount = "TreeTable.children";
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

	private guid parentIdValue = null;

	public GuidField.CLASS<? extends GuidField> parentId = new GuidField.CLASS<GuidField>(this);
	public IntegerField.CLASS<? extends IntegerField> depth = new IntegerField.CLASS<IntegerField>(this);
	public IntegerField.CLASS<? extends IntegerField> children = new IntegerField.CLASS<IntegerField>(this);
	public StringField.CLASS<? extends StringField> path = new StringField.CLASS<StringField>(this);

	public Link.CLASS<? extends Link> parent1 = new Link.CLASS<Link>(this);
	public Link.CLASS<? extends Link> parent2 = new Link.CLASS<Link>(this);
	public Link.CLASS<? extends Link> parent3 = new Link.CLASS<Link>(this);
	public Link.CLASS<? extends Link> parent4 = new Link.CLASS<Link>(this);
	public Link.CLASS<? extends Link> parent5 = new Link.CLASS<Link>(this);
	public Link.CLASS<? extends Link> parent6 = new Link.CLASS<Link>(this);
	public Link.CLASS<? extends Link> root = new Link.CLASS<Link>(this);

	public bool keepIntegrity = new bool(true);

	@SuppressWarnings("rawtypes")
	private Link.CLASS[] parentLinks = { parent1, parent2, parent3, parent4, parent5, parent6 };

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
		parentId.setAttribute(ForeignKey, "false");

		depth.setName(names.Depth);
		depth.setIndex("depth");
		depth.setDisplayName(strings.Depth);

		children.setName(names.ChildrenCount);
		children.setIndex("children");
		children.setDisplayName(strings.ChildrenCount);

		path.setName(names.Path);
		path.setIndex("path");
		path.setDisplayName(strings.Path);

		parent1.setName(names.Parent1);
		parent1.setForeignKey(false);
		parent1.setIndex("parent1");

		parent2.setName(names.Parent2);
		parent2.setForeignKey(false);
		parent2.setIndex("parent2");

		parent3.setName(names.Parent3);
		parent3.setForeignKey(false);
		parent3.setIndex("parent3");

		parent4.setName(names.Parent4);
		parent4.setForeignKey(false);
		parent4.setIndex("parent4");

		parent5.setName(names.Parent5);
		parent5.setForeignKey(false);
		parent5.setIndex("parent5");

		parent6.setName(names.Parent6);
		parent6.setForeignKey(false);
		parent6.setIndex("parent6");

		root.setName(names.Root);
		root.setForeignKey(false);
		root.setIndex("root");

		registerDataField(parentId);
		registerDataField(depth);
		registerDataField(children);
		registerDataField(path);

		registerDataField(parent1);
		registerDataField(parent2);
		registerDataField(parent3);
		registerDataField(parent4);
		registerDataField(parent5);
		registerDataField(parent6);
		registerDataField(root);

		parentId.setSystem(true);
		depth.setSystem(true);
		children.setSystem(true);
		path.setSystem(true);

		path.get().length = new integer(1000);

		showAsGrid.set(false);
	}

	private guid[] parsePath(String path) {
		String[] levels = path.split("\\.");

		List<guid> result = new ArrayList<guid>();

		for(String level : levels) {
			result.add(new guid(level));
		}

		return result.toArray(new guid[0]);
	}

	@Override
	protected void beforeCreate(Query query, guid recordId, guid parentId, Query model, guid modelRecordId) {
		super.beforeCreate(query, recordId, parentId, model, modelRecordId);

		parentId = parentKey().guid();
		recordId = primaryKey().guid();

		if(keepIntegrity.get() && query == this && parentId != null) {
			int depth = 0;

			String path = recordId.toString();
			String parentPath = "";

			Field depthField = this.depth.get();
			Field pathField = this.path.get();

			if(!parentId.isNull()) {
				try {
					saveState();

					Collection<Field> fields = new ArrayList<Field>();
					fields.add(depthField);
					fields.add(pathField);

					readExistingRecord(parentId, fields);

					depth = depthField.get().integer().getInt() + 1;
					parentPath = pathField.get().string().get();
					path = parentPath + (parentPath.isEmpty() ? "" : ".") + recordId;
				} finally {
					restoreState();
				}

				guid[] parents = parsePath(parentPath);

				for(int i = 0; i < parentLinks.length && i < parents.length; i++) {
					guid parent = parents[i];
					Link link = (Link)parentLinks[i].get();
					link.set(parent);
				}
				root.get().set(parents.length > 0 ? parents[0] : guid.NULL);
			} else {
				root.get().set(recordId);
			}

			depthField.set(new integer(depth));
			pathField.set(new string(path.toUpperCase()));
		}
	}

	private void readExistingRecord(guid parentId, Collection<Field> fields) {
		if(!readRecord(parentId, fields)) {
			throw new RuntimeException(Query.strings.ReadError);
		}
	}

	protected void repairTree() {
		TreeTable counter = (TreeTable)getCLASS().newInstance();
		read(Arrays.<Field> asList(recordId.get()));
		while(next()) {
			int count = counter.count(new Equ(counter.parentId.get(), recordId()));
			children.get().set(new integer(count));
			update(recordId());
		}
	}

	@Override
	protected void afterCreate(Query query, guid recordId, guid parentId, Query model, guid modelRecordId) {
		parentId = parentKey().guid();
		recordId = primaryKey().guid();

		super.afterCreate(query, recordId, parentId, model, modelRecordId);

		if(keepIntegrity.get() && query == this && parentId != null) {
			addChild(parentId, 1);
		}
	}

	@Override
	protected void beforeDestroy(Query query, guid recordId, Query model, guid modelRecordId) {
		super.beforeDestroy(query, recordId, model, modelRecordId);

		if(keepIntegrity.get()) {
			try {
				saveState();

				Field parentId = this.parentId.get();

				Collection<Field> fields = new ArrayList<Field>();
				fields.add(parentId);

				if(readRecord(recordId, fields)) {
					parentIdValue = parentId.get().guid();
				}
			} finally {
				restoreState();
			}
		}
	}

	@Override
	protected void afterDestroy(Query query, guid recordId, Query model, guid modelRecordId) {
		super.afterDestroy(query, recordId, model, modelRecordId);

		if(keepIntegrity.get() && parentIdValue != null) {
			addChild(parentIdValue, -1);
			parentIdValue = null;
		}
	}

	private void addChild(guid parentId, int count) {
		try {
			saveState();

			Field children = this.children.get();

			Collection<Field> fields = new ArrayList<Field>();
			fields.add(children);

			if(readRecord(parentId, fields)) {
				int value = children.get().integer().getInt();
				children.set(new integer(Math.max(0, value + count)));
				update(parentId);
			}
		} finally {
			restoreState();
		}
	}

	public guid[] getPath(guid id) {
		if(id.isNull()) {
			return new guid[0];
		}

		String parentPath = "";

		Field pathField = this.path.get();

		try {
			saveState();

			Collection<Field> fields = new ArrayList<Field>();
			fields.add(pathField);

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

			Collection<Field> fields = new ArrayList<Field>();
			fields.add(parentId);

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

	private void changeParent(guid recordId, guid parentId) {
		saveState();
		this.parentId.get().set(parentId);
		update(recordId);
		restoreState();
	}

	public void move(guid recordId, guid parentId) {
		guid[] path = getPath(parentId);

		if(recordId.equals(parentId)) {
			throw new RuntimeException(Resources.get("Exception.wrongParentNode"));
		}

		for(guid part : path) {
			if(part.equals(recordId)) {
				throw new RuntimeException(Resources.get("Exception.wrongParentNode"));
			}
		}

		guid oldParentId = getParent(recordId);

		changeParent(recordId, parentId);

		this.addChild(oldParentId, -1);
		this.addChild(parentId, 1);

	}
}