package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;

public class Fields extends Table {
	final static public String TableName = "SystemFields";

	static public class names {
		public final static String Table = "Table";
		public final static String DisplayName = "Display Name";
		public final static String Type = "Type";
		public final static String Position = "Position";
	}

	static public class strings {
		public final static String Title = "Fields.title";
		public final static String DisplayName = "Fields.displayName";
		public final static String Type = "Fields.type";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String DisplayName = Resources.get(strings.DisplayName);
		public final static String Type = Resources.get(strings.Type);
	}

	public Tables.CLASS<Tables> tables = new Tables.CLASS<Tables>(this);
	public Link.CLASS<Link> table = new Link.CLASS<Link>(this);

	public StringField.CLASS<? extends StringField> displayName = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<? extends StringField> type = new StringField.CLASS<StringField>(this);
	public IntegerField.CLASS<? extends IntegerField> position = new IntegerField.CLASS<IntegerField>(this);

	public static class CLASS<T extends Fields> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Fields.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Fields(container);
		}
	}

	public Fields(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		table.get(IClass.Constructor1).operatorAssign(tables);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		tables.setIndex("tables");

		table.setName(names.Table);
		table.setIndex("table");

		id.get().length = new integer(256);
		name.get().length = new integer(256);

		displayName.setName(names.DisplayName);
		displayName.setDisplayName(displayNames.DisplayName);
		displayName.setIndex("displayName");
		displayName.get().length = new integer(256);

		type.setName(names.Type);
		type.setDisplayName(displayNames.Type);
		type.setIndex("type");
		type.get().length = new integer(50);

		position.setName(names.Position);
		position.setIndex("position");

		registerDataField(table);
		registerDataField(displayName);
		registerDataField(type);
		registerDataField(position);
	}
}
