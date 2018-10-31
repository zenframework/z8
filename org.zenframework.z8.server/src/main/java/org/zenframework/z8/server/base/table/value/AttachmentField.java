package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.runtime.IObject;

public class AttachmentField extends FileField {
	public static class CLASS<T extends AttachmentField> extends FileField.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(AttachmentField.class);
			setSystem(true);
		}

		@Override
		public Object newObject(IObject container) {
			return new AttachmentField(container);
		}
	}

	public AttachmentField(IObject container) {
		super(container);
	}

	@Override
	public FieldType type() {
		return FieldType.Attachments;
	}
}
