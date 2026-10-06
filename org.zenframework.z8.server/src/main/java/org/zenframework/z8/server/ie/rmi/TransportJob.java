package org.zenframework.z8.server.ie.rmi;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.zenframework.z8.server.base.Executable;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
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

	private void sendMessages() {
		Set<String> activeAddresses = Transport.getActiveAddresses();
		int maxTreadsCount = ServerConfig.transportJobThreads();
		int newThreadsCount = maxTreadsCount - activeAddresses.size();

		if (newThreadsCount <= 0)
			return;

		List<String> addresses = Domains.newInstance().getExpiredAddresses(newThreadsCount, activeAddresses);

		if (addresses.isEmpty())
			return;

		log("running new addresses: {0}", addresses);

		for (String address : addresses)
			new Transport(address).start();
	}

	private void log(String message, Object... args) {
		if (logger != null)
			logger.info("TransportJob " + MessageFormat.format(message, args));
	}

	private static Logger getLogger() {
		File transportLogFolder = ServerConfig.transportLogFolder();

		if (transportLogFolder == null)
			return null;

		return LogUtils.builder().setName(LoggerName).setLogFile(new File(transportLogFolder, LoggerFile)).setLogFormat(ServerConfig.transportLogFormat()).build();
	}

}
