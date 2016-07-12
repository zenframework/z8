package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.table.system.MessagesQueue;
import org.zenframework.z8.server.base.table.value.AttachmentExpression;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class MessagesQueueView extends MessagesQueue {
	public static class CLASS<T extends MessagesQueueView> extends MessagesQueue.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(MessagesQueueView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new MessagesQueueView(container);
		}
	}

	public static class XmlExpression extends AttachmentExpression {
		public static class CLASS<T extends XmlExpression> extends AttachmentExpression.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(XmlExpression.class);
			}

			@Override
			public Object newObject(IObject container) {
				return new XmlExpression(container);
			}
		}

		public XmlExpression(IObject container) {
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

	public final XmlExpression.CLASS<XmlExpression> attachment = new XmlExpression.CLASS<XmlExpression>(this);

	private MessagesQueueView(IObject container) {
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
		registerFormField(sender);
		registerFormField(address);
		registerFormField(name);
		registerFormField(description);
		registerFormField(classId);
		registerFormField(processed);
		registerFormField(bytesTransferred);
	}
}
