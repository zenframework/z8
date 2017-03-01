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

		columnCount = new integer(12);

		ordinal.get().colspan = new integer(2);
		registerControl(ordinal);
		sender.get().colspan = new integer(3);
		registerControl(sender);
		address.get().colspan = new integer(3);
		registerControl(address);
		bytesTransferred.get().colspan = new integer(2);
		registerControl(bytesTransferred);
		processed.get().colspan = new integer(2);
		registerControl(processed);

		description.get().colspan = new integer(12);
		registerControl(description);
	}
}
