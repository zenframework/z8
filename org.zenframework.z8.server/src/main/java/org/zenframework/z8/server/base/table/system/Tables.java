package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Tables extends Table {
	final static public String TableName = "SystemTables";

	static public class fieldNames {
		public final static String ClassId = "Class";
		public final static String DisplayName = "Display Name";
	}

	static public class strings {
		public final static String Title = "Tables.title";
		public final static String ClassId = "Tables.classId";
		public final static String Name = "Tables.name";
		public final static String DisplayName = "Tables.displayName";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
		public final static String ClassId = Resources.get(strings.ClassId);
		public final static String DisplayName = Resources.get(strings.DisplayName);
	}

	public StringField.CLASS<? extends StringField> classId = new StringField.CLASS<StringField>(this);
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

	// I18N support
	public static class DisplayNameField extends StringField {
		public static class CLASS<T extends DisplayNameField> extends StringField.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(DisplayNameField.class);
			}

			@Override
			public Object newObject(IObject container) {
				return new DisplayNameField(container);
			}
		}

		public DisplayNameField(IObject container) {
			super(container);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void constructor2() {
			super.constructor2();
			usedFields.add(((Tables.CLASS<Tables>)getContainer().getCLASS()).get().classId);
		}

		@SuppressWarnings("unchecked")
		public string z8_get() {
			super.z8_get();
			return Runtime.instance().getTable(((Tables.CLASS<Tables>)getContainer().getCLASS()).get().classId.get().z8_get().get())
					.get().z8_displayName();
		}
	}

	public Tables(IObject container) {
		super(container);
		displayName = new DisplayNameField.CLASS<DisplayNameField>(this);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(classId);
		objects.add(displayName);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.get().length = new integer(256);
		name.setDisplayName(displayNames.Name);

		classId.setIndex("classId");
		classId.setName(fieldNames.ClassId);
		classId.setDisplayName(displayNames.ClassId);
		classId.get().length = new integer(256);

		displayName.setIndex("displayName");
		displayName.setName(fieldNames.DisplayName);
		displayName.setDisplayName(displayNames.DisplayName);
		displayName.get().length = new integer(256);
	}
}
