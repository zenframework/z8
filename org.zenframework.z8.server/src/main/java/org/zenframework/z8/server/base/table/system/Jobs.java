package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;

public class Jobs extends Table {
	final static public String TableName = "SystemJobs";

	static public class fieldNames {
		public final static String ClassId = "Class";
	}

	static public class strings {
		public final static String Title = "Jobs.title";
		public final static String ClassId = "Jobs.classId";
	}

	static public class displayNames {
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

	public StringField.CLASS<StringField> classId = new StringField.CLASS<StringField>(this);

	public Jobs(IObject container) {
		super(container);
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

		name.get().length = new integer(256);
	}
}
