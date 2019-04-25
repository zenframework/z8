package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.runtime.IObject;

public class TimestampField extends DateField {
	public static class CLASS<T extends TimestampField> extends DateField.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TimestampField.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new TimestampField(container);
		}
	}

	public TimestampField(IObject container) {
		super(container);
	}
}
