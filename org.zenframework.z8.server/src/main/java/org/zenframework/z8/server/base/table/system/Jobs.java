package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;

public class Jobs extends Table {
	final static public String TableName = "SystemJobs";

	static public class fieldNames {
	}

	static public class strings {
		public final static String Title = "Jobs.title";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
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

	public Jobs(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		id.get().length = new integer(256);
		name.get().length = new integer(256);
	}
}
