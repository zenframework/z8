package org.zenframework.z8.server.ie.rmi;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.ie.DataMessage;
import org.zenframework.z8.server.ie.FileMessage;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.ProxyUtils;

public class Transport implements Runnable {
	static private Object lock = new Object();
	static private Map<String, Transport> workers = new HashMap<String, Transport>();

	private String domain;
	private IApplicationServer server;
	private Thread thread;

	private MessageQueue messageQueue = MessageQueue.newInstance();
	private TransportQueue transportQueue = TransportQueue.newInstance();

	static public Transport get(String domain) {
		synchronized(lock) {
			return workers.get(domain);
		}
	}

	static public int getCount() {
		return workers.size();
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
	}

	public void start() {
		thread = new Thread(this, domain);
		if(Scheduler.register(ApplicationServer.getDatabase(), thread))
			Transport.register(this);
	}

	@Override
	public void run() {
		try {
			if(ServerConfig.isMultitenant())
				throw new RuntimeException("Transport is incompatible with multitenacy ");
			ApplicationServer.setRequest(new Request(new Session(ApplicationServer.getSchema())));
			do {
				prepareMessages();
			} while (sendMessages());
		} catch(Throwable e) {
			Trace.logError(e);
		} finally {
			Scheduler.unregister(ApplicationServer.getDatabase(), thread);
			Transport.unregister(this);
			ApplicationServer.setRequest(null);
		}
	}

	private boolean prepareMessages() throws Throwable {
		Collection<Message> messages = messageQueue.getMessages(domain);

		for(Message message : messages)
			prepare(message);

		return !messages.isEmpty();
	}

	private void prepare(Message message) throws Throwable {
		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			messageQueue.beginProcessing(message.getId());
			if(message.prepare())
				connection.commit();
			else
				connection.rollback();
		} catch(Throwable e) {
			connection.rollback();
			throw e;
		}
	}

	private boolean sendMessages() throws Throwable {
		Collection<guid> ids = transportQueue.getMessages(domain);

		for(guid id : ids) {
			Message message = transportQueue.getMessage(id);
			if(!send(message))
				return false;
		}
		
		return !ids.isEmpty();
	}

	private IApplicationServer connect(Message message) throws Throwable {
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

	private boolean send(Message message) throws Throwable {
		if(server == null)
			server = connect(message);

		if(server == null)
			return false;

		try {
			return message instanceof FileMessage ? sendFile((FileMessage)message) : sendMessage((DataMessage)message);
		} catch(Throwable e) {
			String str = e.getMessage();
			if (ServerConfig.transportJobLogStackTrace()) {
				StringWriter buf = new StringWriter();
				PrintWriter out = new PrintWriter(buf);
				out.println(e.getMessage());
				e.printStackTrace(out);
				out.flush();
				str = buf.toString();
			}
			transportQueue.setInfo(message.getId(), str);
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
}
