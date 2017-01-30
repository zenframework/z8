package org.zenframework.z8.server.ie.rmi;

import java.util.Collection;

import org.zenframework.z8.server.base.Procedure;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.base.table.system.MessageQueue;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;

public class TransportJob extends Procedure {
	public static class CLASS<T extends TransportJob> extends Procedure.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TransportJob.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new TransportJob(container);
		}
	}

	public TransportJob(IObject container) {
		super(container);
		useTransaction = bool.False;
	}

	@Override
	protected void z8_execute(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
		sendMessages();
	}

	Collection<String> getAddresses() {
		Collection<String> result = TransportQueue.newInstance().getAddresses();

		for(String address : MessageQueue.newInstance().getAddresses()) {
			if(!result.contains(address))
				result.add(address);
		}
		return result;
	}

	private void sendMessages() {
		Collection<String> addresses = getAddresses();

		for(String address : addresses) {
			Transport thread = Transport.get(address);

			if(thread == null)
				new Transport(address).start();
		}
	}
}
