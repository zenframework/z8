package org.zenframework.z8.server.base.table.system;

import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Entries extends Table {
	final static public String TableName = "SystemEntries";

	static public class fieldNames {
		public final static String ClassId = "Class";
	}

	static public class strings {
		public final static String Title = "Entries.title";
		public final static String ClassId = "Entries.classId";
		public final static String Name = "Entries.name";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String ClassId = Resources.get(strings.ClassId);
		public final static String Name = Resources.get(strings.Name);
	}

	public static class CLASS<T extends Entries> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Entries.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Entries(container);
		}
	}

	// I18N support
	public static class NameField extends StringField {
		public static class CLASS<T extends NameField> extends StringField.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(NameField.class);
			}

			@Override
			public Object newObject(IObject container) {
				return new NameField(container);
			}
		}

		public NameField(IObject container) {
			super(container);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void constructor2() {
			super.constructor2();
			usedFields.add(((Entries.CLASS<Entries>)getContainer().getCLASS()).get().classId);
		}

		@Override
		@SuppressWarnings("unchecked")
		public string z8_get() {
			super.z8_get();
			OBJECT.CLASS<? extends OBJECT> entry = Runtime.instance().getEntry(((Entries.CLASS<Entries>)getContainer().getCLASS()).get().classId.get().z8_get().get());
			return entry != null ? entry.get().z8_displayName() : new string("<entry removed>");
		}
	}

	public StringField.CLASS<StringField> classId = new StringField.CLASS<StringField>(this);

	public Entries(IObject container) {
		super(container);
		name = new NameField.CLASS<NameField>(this);
	}

	@Override
	public void initMembers() {
		super.initMembers();
		objects.add(classId);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		classId.setIndex("classId");
		classId.setName(fieldNames.ClassId);
		classId.setDisplayName(displayNames.ClassId);
		classId.get().length = new integer(1024);

		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(1024);
	}

	@Override
	public void initStaticRecords() {
		LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
		record.put(name.get(), new string(SystemTools.displayNames.Title));
		record.put(classId.get(), new string(SystemTools.ClassId));
		addRecord(SystemTools.Id, record);
	}

	@Override
	public integer z8_destroy(guid id) {
		if(id.equals(SystemTools.Id))
			throw new exception("Unable to delete builtin system entrypoint !");

		return super.z8_destroy(id);
	}

}
