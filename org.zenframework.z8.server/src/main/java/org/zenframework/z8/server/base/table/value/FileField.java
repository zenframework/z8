package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.file.AttachmentProcessor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.runtime.IObject;

public class FileField extends TextField {
	public static class CLASS<T extends FileField> extends TextField.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(FileField.class);
			setSystem(true);
		}

		@Override
		public Object newObject(IObject container) {
			return new FileField(container);
		}
	}

	public FileField(IObject container) {
		super(container);
	}

	@Override
	public FieldType type() {
		return FieldType.File;
	}

	public AttachmentProcessor getAttachmentProcessor() {
		return new AttachmentProcessor(this);
	}

	public AttachmentProcessor.CLASS<? extends AttachmentProcessor> z8_getAttachmentProcessor() {
		AttachmentProcessor.CLASS<AttachmentProcessor> processor = new AttachmentProcessor.CLASS<AttachmentProcessor>();
		processor.get().set(this);
		return processor;
	}

}
