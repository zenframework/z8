package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.runtime.IObject;

public class FileExpression extends StringExpression {
	public static class CLASS<T extends FileExpression> extends StringExpression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(FileExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new FileExpression(container);
		}
	}

	public FileExpression(IObject container) {
		super(container);
	}

	@Override
	public FieldType type() {
		return FieldType.File;
	}
}
