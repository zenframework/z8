package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;

public class Tables extends Table {
	final static public String TableName = "SystemTables";

	static public class names {
		public final static String DisplayName = "Display Name";
	}

	static public class strings {
		public final static String Title = "Tables.title";
		public final static String DisplayName = "Tables.displayName";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String DisplayName = Resources.get(strings.DisplayName);
	}

	public StringField.CLASS<? extends StringField> displayName = new StringField.CLASS<StringField>(this);
	
	public static class CLASS<T extends Tables> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Tables.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Tables(container);
		}
	}

	public Tables(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		id.get().length = new integer(256);
		name.get().length = new integer(256);

		displayName.setName(names.DisplayName);
		displayName.setDisplayName(displayNames.DisplayName);
		displayName.setIndex("displayName");
		displayName.get().length = new integer(256);
		displayName.setDisplayName(displayNames.DisplayName);

		registerDataField(displayName);
	}
}
