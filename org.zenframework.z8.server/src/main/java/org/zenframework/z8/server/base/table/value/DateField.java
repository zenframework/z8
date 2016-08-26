package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.format.Format;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.string;

public class DateField extends DatetimeField {
	public static class CLASS<T extends DateField> extends DatetimeField.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(DateField.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new DateField(container);
		}
	}

	public DateField(IObject container) {
		super(container);
		aggregation = Aggregation.Max;
		format = new string(Format.date);
		stretch = new bool(false);
	}

	@Override
	public FieldType type() {
		return FieldType.Date;
	}
}
