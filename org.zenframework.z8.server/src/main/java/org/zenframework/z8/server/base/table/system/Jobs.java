package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Jobs extends Table {
	final static public String TableName = "SystemJobs";

	static public class fieldNames {
		public final static String Name = "Name";
		public final static String ClassId = "Class";
	}

	static public class strings {
		public final static String Title = "Jobs.title";
		public final static String Name = "Jobs.name";
		public final static String ClassId = "Jobs.classId";
	}

	static public class displayNames {
		public final static String Name = Resources.get(strings.Name);
		public final static String Title = Resources.get(strings.Title);
		public final static String ClassId = Resources.get(strings.ClassId);
	}
	
	public static class CLASS<T extends Jobs> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Jobs.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Jobs(container);
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
			//usedFields.add(((Jobs.CLASS<Jobs>)getContainer().getCLASS()).get().classId);
			usedFields.add(((Jobs.CLASS<Jobs>)getContainer().getCLASS()).get().recordId);
		}

		@Override
		@SuppressWarnings("unchecked")
		public string z8_get() {
			super.z8_get();
			//OBJECT.CLASS<? extends OBJECT> job = Runtime.instance().getJob(((Jobs.CLASS<Jobs>)getContainer().getCLASS()).get().classId.get().z8_get().get());
			OBJECT.CLASS<? extends OBJECT> job = Runtime.instance().getJobByKey(((Jobs.CLASS<Jobs>)getContainer().getCLASS()).get().recordId());
			return job.get().z8_displayName();
		}
	}

	public StringField.CLASS<? extends StringField> name = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> classId = new StringField.CLASS<StringField>(this);

	public Jobs(IObject container) {
		super(container);
		// TODO Fix NullPointerException
		//name = new NameField.CLASS<NameField>(this);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(name);
		objects.add(classId);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		classId.setIndex("classId");
		classId.setName(fieldNames.ClassId);
		classId.setDisplayName(displayNames.ClassId);
		classId.get().length = new integer(1024);

		name.setName(fieldNames.Name);
		name.setIndex("name");
		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(256);
	}
}
