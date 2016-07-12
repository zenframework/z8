package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.table.value.AttachmentExpression;
import org.zenframework.z8.server.ie.ExportMessages;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class ExportMessagesView extends ExportMessages {
	public static class CLASS<T extends ExportMessagesView> extends ExportMessages.CLASS<T> {

		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(ExportMessagesView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new ExportMessagesView(container);
		}

	}

	public static class MessageAttachmentExpression extends AttachmentExpression {

		public static class CLASS<T extends MessageAttachmentExpression> extends AttachmentExpression.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(MessageAttachmentExpression.class);
			}

			@Override
			public Object newObject(IObject container) {
				return new MessageAttachmentExpression(container);
			}
		}

		public MessageAttachmentExpression(IObject container) {
			super(container);
		}

		@Override
		protected String attachmentName() {
			return "message.xml";
		}

		@Override
		protected String contentFieldName() {
			return names.Xml;
		}
	}

	public final MessageAttachmentExpression.CLASS<MessageAttachmentExpression> attachment = new MessageAttachmentExpression.CLASS<MessageAttachmentExpression>(this);

	private ExportMessagesView(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		createdAt.setSystem(false);

		message.get().colspan = new integer(3);
		message.get().visible = new bool(false);

		attachment.setIndex("attachment");
		
		createdAt.get().visible = new bool(false);
		classId.get().visible = new bool(false);

		sortFields.add(ordinal);
		
		registerDataField(attachment);

		registerFormField(ordinal);
		registerFormField(createdAt);
		registerFormField(id);
		registerFormField(id1);
		registerFormField(name);
		registerFormField(description);
		registerFormField(classId);
		registerFormField(processed);
		registerFormField(bytesTransferred);
	}
}
