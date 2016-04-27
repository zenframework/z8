package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.runtime.IObject;

public class AttachmentExpression extends TextExpression {
	public static class CLASS<T extends AttachmentExpression> extends TextExpression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(AttachmentExpression.class);
			setAttribute(Native, AttachmentExpression.class.getCanonicalName());
		}

		@Override
		public Object newObject(IObject container) {
			return new AttachmentExpression(container);
		}
	}

	public AttachmentExpression(IObject container) {
		super(container);
	}
}
