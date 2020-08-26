package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;

import java.util.Optional;

public class Requests extends Table {
	final static public String TableName = "SystemRequests";

	static public class fieldNames {
		public final static String ClassId = "Class";
	}

	static public class strings {
		public final static String Title = "Requests.title";
		public final static String ClassId = "Requests.classId";
		public final static String Name = "Requests.name";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String ClassId = Resources.get(strings.ClassId);
	}
	static public class apiAttrs {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
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
			Optional.ofNullable(apiAttrs.Title)
					.ifPresent(attrVal -> setAttribute("APIDescription", attrVal));
		}

		@Override
		public Object newObject(IObject container) {
			return new Requests(container);
		}
	}

	public StringField.CLASS<StringField> classId = new StringField.CLASS<StringField>(this);

	public Requests(IObject container) {
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
		Optional.ofNullable(apiAttrs.Name)
				.ifPresent(attrVal -> name.setAttribute("APIDescription", attrVal));
	}
}
