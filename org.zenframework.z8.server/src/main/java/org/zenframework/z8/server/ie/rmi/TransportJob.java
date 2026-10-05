package org.zenframework.z8.server.ie.rmi;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.zenframework.z8.server.base.Executable;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.base.table.system.MessageQueue;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.utils.ArrayUtils;
import org.zenframework.z8.server.utils.LogUtils;

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

	static private final String LoggerName = "TransportJob";
	static private final String LoggerFile = ".job.log";

	static private int lastPosition = 0;

	private Logger logger;

	public TransportJob(IObject container) {
		super(container);
		useTransaction = bool.False;
	}

	@Override
	protected void z8_execute(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
		logger = getLogger();
		sendMessages();
		LogUtils.close(logger);
	}

	private List<String> getAddresses() {
		List<String> result = TransportQueue.newInstance().getAddresses();
		Set<String> set = new HashSet<String>(result);

		for (String address : MessageQueue.newInstance().getAddresses()) {
			if (set.add(address))
				result.add(address);
		}

		return result;
	}

	private void sendMessages() {
		int maxTreadsCount = ServerConfig.transportJobThreads();

		if (Transport.getCount() >= maxTreadsCount)
			return;

		List<String> addresses = getAddresses();

		if (addresses.isEmpty())
			return;

		int startPosition = lastPosition = ArrayUtils.range(lastPosition, addresses.size());

		log("running addresses: {0} from {1}", addresses, startPosition);

		do {
			String address = addresses.get(lastPosition);

			Transport thread = Transport.get(address);

			if (thread == null)
				new Transport(address).start();

			lastPosition = ArrayUtils.range(lastPosition + 1, addresses.size());
		} while(lastPosition != startPosition && Transport.getCount() <= maxTreadsCount);

		log("stopped at: {0}", lastPosition);
	}

	private void log(String message, Object... args) {
		if (logger != null)
			logger.info("TransportJob: " + MessageFormat.format(message, args));
	}

	private static Logger getLogger() {
		File transportLogFolder = ServerConfig.transportLogFolder();

		if (transportLogFolder == null)
			return null;

		return LogUtils.builder().setName(LoggerName).setLogFile(new File(transportLogFolder, LoggerFile)).setLogFormat(ServerConfig.transportLogFormat()).build();
	}

}
