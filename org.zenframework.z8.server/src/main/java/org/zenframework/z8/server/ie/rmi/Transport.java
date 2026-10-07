package org.zenframework.z8.server.ie.rmi;

import java.io.File;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.base.table.system.MessageQueue;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.ie.DataMessage;
import org.zenframework.z8.server.ie.FileMessage;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.ErrorUtils;
import org.zenframework.z8.server.utils.LogUtils;
import org.zenframework.z8.server.utils.ProxyUtils;

public class Transport implements Runnable {
	static private Object lock = new Object();
	static private Map<String, Transport> workers = new HashMap<String, Transport>();

	static public class Info {
		private final String domain;
		private final int messageQueueSize;
		private final int transportQueueSize;

		private Info(String domain, int messageQueueSize, int transportQueueSize) {
			this.domain = domain;
			this.messageQueueSize = messageQueueSize;
			this.transportQueueSize = transportQueueSize;
		}

		public String getDomain() {
			return domain;
		}

		public int getMessageQueueSize() {
			return messageQueueSize;
		}

		public int getTransportQueueSize() {
			return transportQueueSize;
		}
	}

	private final String domain;
	private final Logger logger;
	private IApplicationServer server;
	private Thread thread;

	private MessageQueue messageQueue = MessageQueue.newInstance();
	private TransportQueue transportQueue = TransportQueue.newInstance();

	private int messageQueueSize;
	private int transportQueueSize;
	private int messagesPrepared = 0;
	private int messagesSent = 0;

	static public Set<String> getActiveAddresses() {
		synchronized(lock) {
			return workers.keySet();
		}
	}

	static public void register(Transport transport) {
		synchronized(lock) {
			workers.put(transport.domain, transport);
		}
	}

	static public void unregister(Transport transport) {
		synchronized(lock) {
			workers.remove(transport.domain);
		}
	}

	public Transport(String domain) {
		this.domain = domain;
		this.logger = getLogger(domain);
	}

	public void start() {
		thread = new Thread(this, domain);
		if (Scheduler.register(ApplicationServer.getDatabase(), thread))
			Transport.register(this);
	}

	@Override
	public void run() {
		try {
			if (ServerConfig.isMultitenant())
				throw new RuntimeException("Transport is incompatible with multitenacy mode");

			debug("started");

			ApplicationServer.setRequest(new Request(new Session(ApplicationServer.getSchema())));

			Domains.newInstance().updateLastSend(domain);

			int count = ServerConfig.transportJobIterations();
			boolean success = true;

			for (int i = 0; i < count; i++) {
				debug("cycle {0}/{1}", i + 1, count);
				prepareMessages();
				if (!sendMessages()) {
					success = false;
					break;
				}
			}

			debug("{0}. Messages (prepared/sent): {1}/{2}", success ? "finished successfully" : "interrupted", messagesPrepared, messagesSent);
		} catch (Throwable e) {
			debug("failed. Messages (prepared/sent): {0}/{1}", messagesPrepared, messagesSent);
			error(e, "error: " + e.getMessage());
		} finally {
			Scheduler.unregister(ApplicationServer.getDatabase(), thread);
			Transport.unregister(this);
			ApplicationServer.setRequest(null);
			ConnectionManager.release();
			LogUtils.close(logger);
		}
	}

	private void prepareMessages() throws Throwable {
		messageQueueSize = messageQueue.count(domain);

		Collection<Message> messages = messageQueue.getMessages(domain);

		for (Message message : messages)
			prepare(message);
	}

	private void prepare(Message message) throws Throwable {
		Connection connection = ConnectionManager.get();
		boolean success;

		try {
			connection.beginTransaction();
			messageQueue.beginProcessing(message.getId());
			success = message.prepare();

			if (success) {
				connection.commit();
				messagesPrepared++;
			}

			debug("prepare message {0} {1}", message, success ? "successfully" : "falls to retry");
		} catch (Throwable e) {
			connection.rollback();
			error(e, "prepare message {0} failed", message);
			throw e;
		}

		if (!success)
			connection.rollback();
	}

	private boolean sendMessages() throws Throwable {
		transportQueueSize = transportQueue.count(domain);

		Collection<guid> ids = transportQueue.getMessages(domain);

		for (guid id : ids) {
			Message message = transportQueue.getMessage(id);
			if (!send(message))
				return false;
		}

		return !ids.isEmpty();
	}

	private IApplicationServer connect(Message message) throws Throwable {
		IInterconnectionCenter center = ServerConfig.interconnectionCenter();
		String centerUrl = ProxyUtils.getUrl(center);

		debug("connecting to Interconnection Center {0}", centerUrl);

		try {
			center.probe();
			debug("Interconnection Center {0} probed successfully", centerUrl);
		} catch (Throwable e) {
			error(e, "Interconnection Center {0} probe failed", centerUrl);
			transportQueue.setInfo(message.getId(), "Interconnection Center is unavailable at " + centerUrl);
			return null;
		}

		IApplicationServer server = ServerConfig.interconnectionCenter().connect(domain);

		if (server == null) {
			debug("domain [{0}] is unavailable at Interconnection Center {1}", domain, centerUrl);
			transportQueue.setInfo(message.getId(), "Domain '" + domain + "' is unavailable at Interconnection Center " + centerUrl);
			return null;
		}

		String serverUrl = ProxyUtils.getUrl(server);

		try {
			server.probe();
			debug("Application Server [{0}] ({1}) probed successfully", domain, serverUrl);
			return server;
		} catch(RemoteException e) {
			error(e, "Application Server [{0}] ({1}) probe failed", domain, serverUrl);

			if (ServerConfig.transportFallbackProxy()) {
				debug("trying to get Application Server [{0}] ({1}) via Interconnection Center proxy {1}", domain, serverUrl, centerUrl);
				transportQueue.setInfo(message.getId(), "Sending via Interconnection center: " + ProxyUtils.getUrl(center) + "\nFallback from:\n" + ErrorUtils.getStackTrace(e));
				return new ApplicationServerProxy(server);
			}

			transportQueue.setInfo(message.getId(), "Connection to '" + domain + "' failed\n" + ErrorUtils.getStackTrace(e));
			return null;
		}
	}

	private boolean send(Message message) throws Throwable {
		if (server == null)
			server = connect(message);

		if (server == null)
			return false;

		try {
			boolean success = message instanceof FileMessage ? sendFile((FileMessage) message) : sendMessage((DataMessage) message);

			debug("send message {0} {1}", message, success ? "successfully" : "falls to retry");

			if (success)
				messagesSent++;

			return success;
		} catch (Throwable e) {
			error(e, "send message {0} failed", message);
			transportQueue.setInfo(message.getId(), ErrorUtils.getStackTrace(e));
			throw e;
		}
	}

	private boolean sendMessage(DataMessage message) throws Throwable {
		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();

			if(server.accept(message)) {
				messageQueue.endProcessing(message.getSourceId());
				transportQueue.setProcessed(message.getId());
			}

			connection.commit();
			return true;

		} catch(Throwable e) {
			connection.rollback();
			throw e;
		}
	}

	private boolean sendFile(FileMessage message) throws Throwable {
		if(server.has(message)) {
			transportQueue.setProcessed(message.getId());
			return true;
		}

		Connection connection = ConnectionManager.get();

		file file = message.getFile();

		while((file = file.nextPart()) != null) {
			try {
				connection.beginTransaction();

				boolean reset = !server.accept(message);

				transportQueue.setBytesTrasferred(message.getId(), !reset ? file.offset() + file.partLength() : 0);

				connection.commit();

				if(reset) {
					transportQueue.setInfo(message.getId(), "Reset");
					return false;
				}
			} catch(Throwable e) {
				connection.rollback();
				throw e;
			}
		}

		transportQueue.setProcessed(message.getId());
		return true;
	}

	public Info getInfo() {
		return new Info(domain, messageQueueSize, transportQueueSize);
	}

	public String getId() {
		return domain + '/' + (thread != null ? thread.getId() : "-");
	}

	private String logHeader() {
		return "Transport thread [" + getId() + "] ";
	}

	private void debug(String message, Object... args) {
		if (logger != null)
			logger.log(Level.INFO, logHeader() + MessageFormat.format(message, args));
	}

	private void error(Throwable e, String message, Object... args) {
		if (logger != null)
			logger.log(Level.SEVERE, logHeader() + MessageFormat.format(message, args), e);
	}

	public static List<Info> getTransportsInfo() {
		Collection<Transport> transports;

		synchronized(lock) {
			transports = new ArrayList<Transport>(workers.values());
		}

		List<Info> infos = new ArrayList<Transport.Info>(transports.size());

		for (Transport transport : transports)
			infos.add(transport.getInfo());

		return infos;
	}

	private static Logger getLogger(String domain) {
		File transportLogFolder = ServerConfig.transportLogFolder();

		if (transportLogFolder == null)
			return null;

		return LogUtils.builder().setName(domain).setLogFile(new File(transportLogFolder, domain + ".log")).setLogFormat(ServerConfig.transportLogFormat()).build();
	}

}
