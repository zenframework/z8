package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.runtime.IObject;

public class TransportQueueView extends TransportQueue {
	public static class CLASS<T extends TransportQueueView> extends TransportQueue.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(TransportQueueView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new TransportQueueView(container);
		}
	}

	private TransportQueueView(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		sortFields.add(ordinal);
		
		registerFormField(ordinal);
		registerFormField(sender);
		registerFormField(address);
		registerFormField(description);
		registerFormField(processed);
		registerFormField(bytesTransferred);
	}
}
