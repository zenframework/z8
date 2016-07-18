package org.zenframework.z8.server.base.table.system.view;

import java.io.File;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.value.AttachmentExpression;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;

public class FilesView extends Files {
	static public class strings {
		public final static String Attachment = "FilesView.attachment";
	}

	static public class displayNames {
		public final static String Attachment = Resources.get(strings.Attachment);
	}

	public static class CLASS<T extends FilesView> extends Files.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(FilesView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new FilesView(container);
		}
	}

	private static class FileAttachmentExpression extends AttachmentExpression {
		public static class CLASS<T extends FileAttachmentExpression> extends AttachmentExpression.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(FileAttachmentExpression.class);
			}

			@Override
			public Object newObject(IObject container) {
				return new FileAttachmentExpression(container);
			}
		}

		public FileAttachmentExpression(IObject container) {
			super(container);
		}

		@Override
		protected String attachmentName() {
			Files container = (Files)getContainer();
			File path = new File(Folders.Base, container.path.get().get().string().get());
			return path.exists() ? container.name.get().get().string().get() : null;
		}

		@Override
		protected String contentFieldName() {
			return names.File;
		}
	}

	public final FileAttachmentExpression.CLASS<FileAttachmentExpression> attachment = new FileAttachmentExpression.CLASS<FileAttachmentExpression>(this);

	static public Files newInstance() {
		return new Files.CLASS<Files>().get();
	}

	public FilesView(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		attachment.setIndex("attachment");
		attachment.setDisplayName(displayNames.Attachment);

		registerDataField(attachment);

		registerFormField(createdAt);
		registerFormField(name);
		registerFormField(description);
		registerFormField(path);
		registerFormField(attachment);
	}
}