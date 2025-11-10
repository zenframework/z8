package org.zenframework.z8.server.base.form.report;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Condition extends OBJECT {
	static public class CLASS<T extends Condition> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Condition.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Condition(container);
		}
	}

	public Condition(IObject container) {
		super(container);
	}

	public sql_bool z8_expression() {
		return new sql_bool(true);
	}
}
