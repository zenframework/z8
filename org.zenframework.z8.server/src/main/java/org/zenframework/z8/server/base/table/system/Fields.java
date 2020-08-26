package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;

import java.util.Optional;

public class Fields extends Table {
	final static public String TableName = "SystemFields";

	static public class fieldNames {
		public final static String Table = "Table";
		public final static String ClassId = "Class";
		public final static String DisplayName = "Display Name";
		public final static String Type = "Type";
		public final static String Position = "Position";
	}

	static public class strings {
		public final static String Title = "Fields.title";
		public final static String Table = "Fields.table";
		public final static String ClassId = "Fields.classId";
		public final static String Name = "Fields.name";
		public final static String DisplayName = "Fields.displayName";
		public final static String Type = "Fields.type";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String ClassId = Resources.get(strings.ClassId);
		public final static String DisplayName = Resources.get(strings.DisplayName);
		public final static String Type = Resources.get(strings.Type);
	}

	static public class apiAttrs {
		public final static String Title = Resources.getOrNull(strings.Title + ".APIDescription");
		public final static String Table = Resources.getOrNull(strings.Table + ".APIDescription");
		public final static String ClassId = Resources.getOrNull(strings.ClassId + ".APIDescription");
		public final static String Name = Resources.getOrNull(strings.Name + ".APIDescription");
		public final static String DisplayName = Resources.getOrNull(strings.DisplayName + ".APIDescription");
		public final static String Type = Resources.getOrNull(strings.Type + ".APIDescription");
	}

	public static class CLASS<T extends Fields> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Fields.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
			Optional.ofNullable(apiAttrs.Title)
					.ifPresent(attrVal -> setAttribute("APIDescription", attrVal));
		}

		@Override
		public Object newObject(IObject container) {
			return new Fields(container);
		}
	}

	public Tables.CLASS<Tables> tables = new Tables.CLASS<Tables>(this);
	public Link.CLASS<Link> table = new Link.CLASS<Link>(this);

	public StringField.CLASS<? extends StringField> classId = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<? extends StringField> displayName = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<? extends StringField> type = new StringField.CLASS<StringField>(this);
	public IntegerField.CLASS<? extends IntegerField> position = new IntegerField.CLASS<IntegerField>(this);

	public Fields(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		table.get(IClass.Constructor1).operatorAssign(tables);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(classId);
		objects.add(table);
		objects.add(displayName);
		objects.add(type);
		objects.add(position);

		objects.add(tables);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		tables.setIndex("tables");

		table.setName(fieldNames.Table);
		table.setIndex("table");
		Optional.ofNullable(apiAttrs.Table)
				.ifPresent(attrVal -> table.setAttribute("APIDescription", attrVal));

		name.get().length = new integer(256);
		Optional.ofNullable(apiAttrs.Name)
				.ifPresent(attrVal -> name.setAttribute("APIDescription", attrVal));

		classId.setIndex("classId");
		classId.setName(fieldNames.ClassId);
		classId.setDisplayName(displayNames.ClassId);
		classId.get().length = new integer(256);
		Optional.ofNullable(apiAttrs.ClassId)
				.ifPresent(attrVal -> classId.setAttribute("APIDescription", attrVal));

		displayName.setName(fieldNames.DisplayName);
		displayName.setDisplayName(displayNames.DisplayName);
		displayName.setIndex("displayName");
		displayName.get().length = new integer(256);
		Optional.ofNullable(apiAttrs.DisplayName)
				.ifPresent(attrVal -> displayName.setAttribute("APIDescription", attrVal));

		type.setName(fieldNames.Type);
		type.setDisplayName(displayNames.Type);
		type.setIndex("type");
		type.get().length = new integer(50);
		Optional.ofNullable(apiAttrs.Type)
				.ifPresent(attrVal -> type.setAttribute("APIDescription", attrVal));

		position.setName(fieldNames.Position);
		position.setIndex("position");
	}
}
