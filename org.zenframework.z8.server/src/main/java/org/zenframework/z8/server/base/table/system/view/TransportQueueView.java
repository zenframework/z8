package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

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

		readOnly = bool.True;

		sortFields.add(ordinal);

		colCount = new integer(12);

		ordinal.get().colSpan = new integer(2);
		registerControl(ordinal);
		sender.get().colSpan = new integer(3);
		registerControl(sender);
		address.get().colSpan = new integer(3);
		registerControl(address);
		bytesTransferred.get().colSpan = new integer(2);
		registerControl(bytesTransferred);
		processed.get().colSpan = new integer(2);
		registerControl(processed);

		description.get().colSpan = new integer(12);
		registerControl(description);
	}
}
