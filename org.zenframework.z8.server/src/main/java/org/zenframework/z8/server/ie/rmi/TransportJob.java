package org.zenframework.z8.server.ie.rmi;

import java.util.Collection;

import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.system.MessagesQueue;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.ie.Import;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;

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
		useTransaction.set(false);
	}

	@Override
	protected void z8_exec(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
		sendMessages();
	}

	private void sendMessages() {
		Collection<String> addresses = MessagesQueue.newInstance().getAddresses();

		for(String address : addresses) {
			Transport thread = Transport.get(address);

			if(thread == null)
				new Transport(address).start();
		}
	}

	static public boolean accept(Object object) {
		return Import.importObject(object);
	}
}
