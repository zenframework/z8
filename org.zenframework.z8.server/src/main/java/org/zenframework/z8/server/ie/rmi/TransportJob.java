package org.zenframework.z8.server.ie.rmi;

import java.util.Collection;

import org.zenframework.z8.server.base.Executable;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.base.table.system.MessageQueue;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.utils.ArrayUtils;

public class TransportJob extends Executable {
	public static class CLASS<T extends TransportJob> extends Executable.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TransportJob.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new TransportJob(container);
		}
	}

	static private int lastPosition = 0;

	public TransportJob(IObject container) {
		super(container);
		useTransaction = bool.False;
	}

	@Override
	protected void z8_execute(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
		sendMessages();
	}

	private Collection<String> getAddresses() {
		Collection<String> result = TransportQueue.newInstance().getAddresses();

		for(String address : MessageQueue.newInstance().getAddresses()) {
			if(!result.contains(address))
				result.add(address);
		}
		return result;
	}

	private void sendMessages() {
		int maxTreadsCount = ServerConfig.transportJobThreads();

		if(Transport.getCount() >= maxTreadsCount)
			return;

		String[] addresses = getAddresses().toArray(new String[0]);

		if(addresses.length == 0)
			return;

		int startPosition = lastPosition = ArrayUtils.range(lastPosition, addresses.length);

		do {
			String address = addresses[lastPosition];

			Transport thread = Transport.get(address);

			if(thread == null)
				new Transport(address).start();

			lastPosition = ArrayUtils.range(lastPosition + 1, addresses.length);
		} while(lastPosition != startPosition && Transport.getCount() <= maxTreadsCount);
	}
}
