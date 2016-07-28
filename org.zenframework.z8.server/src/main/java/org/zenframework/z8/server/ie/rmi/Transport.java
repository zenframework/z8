package org.zenframework.z8.server.ie.rmi;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.table.system.MessageQueue;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.ie.BaseMessage;
import org.zenframework.z8.server.ie.DataMessage;
import org.zenframework.z8.server.ie.FileMessage;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.ProxyUtils;

public class Transport implements Runnable {
	static private Map<String, Transport> workers = new HashMap<String, Transport>();

	private String domain;
	private IApplicationServer server;
	private Thread thread;

	private MessageQueue messageQueue = MessageQueue.newInstance();
	private TransportQueue transportQueue = TransportQueue.newInstance();

	static public Transport get(String domain) {
		return workers.get(domain);
	}

	public Transport(String domain) {
		this.domain = domain;
	}

	public void start() {
		Thread thread = new Thread(this, domain);
		Scheduler.register(thread);
		workers.put(domain, this);
		thread.start();
	}

	@Override
	public void run() {
		try {
			prepareMessages();
			sendMessages();
		} catch(Throwable e) {
			Trace.logError(e);
		} finally {
			Scheduler.unregister(thread);
			workers.remove(domain);
		}
	}

	private void prepareMessages() throws Throwable {
		Collection<BaseMessage> messages = messageQueue.getMessages(domain);

		for(BaseMessage message : messages)
			prepare(message);
	}

	private void prepare(BaseMessage message) throws Throwable {
		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			messageQueue.beginProcessing(message.getId());
			message.prepare();
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw e;
		}
	}

	private void sendMessages() throws Throwable {
		Collection<guid> ids = transportQueue.getMessages(domain);

		for(guid id : ids) {
			BaseMessage message = transportQueue.getMessage(id);
			if(!send(message))
				return;
		}
	}

	private IApplicationServer connect(BaseMessage message) throws Throwable {
		IInterconnectionCenter center = ServerConfig.interconnectionCenter();

		try {
			center.probe();
		} catch(Throwable e) {
			transportQueue.setInfo(message.getId(), "Interconnection Center is unavailable at " + ProxyUtils.getUrl(center));
			return null;
		}

		IApplicationServer server = ServerConfig.interconnectionCenter().connect(domain);

		if(server == null) {
			transportQueue.setInfo(message.getId(), "Domain '" + domain + "' is unavailable at Interconnection Center " + ProxyUtils.getUrl(center));
			return null;
		}

		try {
			server.probe();
		} catch(RemoteException e) {
			transportQueue.setInfo(message.getId(), "Sending via Interconnection center: " + ProxyUtils.getUrl(center));
			return new ApplicationServerProxy(server);
		}

		return server;
	}

	private boolean send(BaseMessage message) throws Throwable {
		if(server == null)
			server = connect(message);

		if(server == null)
			return false;

		try {
			return message instanceof FileMessage ? sendFile((FileMessage)message) : sendMessage((DataMessage)message);
		} catch(Throwable e) {
			transportQueue.setInfo(message.getId(), e.getMessage());
			throw e;
		}
	}

	private boolean sendMessage(DataMessage message) throws Throwable {
		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();

			if(server.accept(message)) {
				messageQueue.endProcessing(message.getSourceId());
				transportQueue.setProcessed(message.getId(), "OK");
			}

			connection.commit();
			return true;

		} catch(Throwable e) {
			connection.rollback();
			throw e;
		}
	}

	private boolean sendFile(FileMessage message) throws Throwable {
		file file = message.getFile();
		long size = file.size.get();

		if(server.has(message)) {
			transportQueue.setProcessed(message.getId(), "Skipped");
			return true;
		}

		Connection connection = ConnectionManager.get();

		while((file = file.nextPart()) != null) {
			try {
				connection.beginTransaction();

				boolean reset = !server.accept(message);

				transportQueue.setBytesTrasferred(message.getId(), reset ? 0 : file.offset());

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

		transportQueue.setProcessed(message.getId(), "OK", size);
		return true;
	}
}
