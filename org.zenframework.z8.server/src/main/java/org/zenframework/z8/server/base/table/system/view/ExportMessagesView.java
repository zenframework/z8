package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.table.value.AttachmentExpression;
import org.zenframework.z8.server.ie.ExportMessages;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class ExportMessagesView extends ExportMessages {

	static public class strings {
		public final static String Title = "ExportMessages.title";
		public final static String Sender = "ExportMessages.sender";
		public final static String Receiver = "ExportMessages.receiver";
		public final static String Info = "ExportMessages.info";
		public final static String Transport = "ExportMessages.transport";
		public final static String Message = "ExportMessages.message";
		public final static String Ordinal = "ExportMessages.ordinal";
		public final static String ClassId = "ExportMessages.classId";
		public final static String Processed = "ExportMessages.processed";
		public final static String Error = "ExportMessages.error";
		public final static String BytesTransferred = "ExportMessages.bytesTransferred";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Sender = Resources.get(strings.Sender);
		public final static String Receiver = Resources.get(strings.Receiver);
		public final static String Info = Resources.get(strings.Info);
		public final static String Transport = Resources.get(strings.Transport);
		public final static String Message = "ExportMessages.message";
		public final static String Ordinal = Resources.get(strings.Ordinal);
		public final static String ClassId = Resources.get(strings.ClassId);
		public final static String Processed = Resources.get(strings.Processed);
		public final static String Error = Resources.get(strings.Error);
		public final static String BytesTransferred = Resources.get(strings.BytesTransferred);
	}

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

		registerDataField(attachment);

		registerFormField(createdAt);
		registerFormField(id);
		registerFormField(id1);
		registerFormField(name);
		registerFormField(description);
		registerFormField(ordinal);
		registerFormField(classId);
		registerFormField(processed);
		registerFormField(error);
		registerFormField(bytesTransferred);
	}
}
