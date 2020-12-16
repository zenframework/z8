package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Requests extends Table {
	final static public String TableName = "SystemRequests";

	static public class fieldNames {
		public final static String ClassId = "Class";
	}

	static public class strings {
		public final static String Title = "Requests.title";
		public final static String ClassId = "Requests.classId";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String ClassId = Resources.get(strings.ClassId);
	}
	
	public static class CLASS<T extends Requests> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Requests.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Requests(container);
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
			usedFields.add(((Requests.CLASS<Requests>)getContainer().getCLASS()).get().classId);
		}

		@SuppressWarnings("unchecked")
		public string z8_get() {
			super.z8_get();
			return Runtime.instance().getRequest(((Requests.CLASS<Requests>)getContainer().getCLASS()).get().classId.get().z8_get().get())
					.get().z8_displayName();
		}
	}

	public StringField.CLASS<StringField> classId = new StringField.CLASS<StringField>(this);

	public Requests(IObject container) {
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

		name.get().length = new integer(256);
	}
}
