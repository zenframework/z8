package org.zenframework.z8.server.ie;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.system.MessagesQueue;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.ProxyUtils;

public class RmiTransportProcedure extends Procedure {

	static private Map<String, TransportThread> workers = new HashMap<String, TransportThread>();

	protected final MessagesQueue messages = MessagesQueue.newInstance();

	public static class CLASS<T extends RmiTransportProcedure> extends Procedure.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(RmiTransportProcedure.class);
			setAttribute(Job, "10");
		}

		@Override
		public Object newObject(IObject container) {
			return new RmiTransportProcedure(container);
		}
	}

	public RmiTransportProcedure(IObject container) {
		super(container);
		useTransaction.set(false);
	}

	@Override
	public void constructor2() {
		super.constructor2();
	}

	public class TransportThread extends Thread {
		private String domain;
		private IApplicationServer server;
		
		public TransportThread(String domain) {
			super(domain);
			this.domain = domain;
		}

		@Override
		public void run() {
			Collection<guid> ids = messages.getMessages(domain);

			try {
				for (guid id : ids) {
					Message message = messages.getMessage(id);
					if(!send(message))
						return;
				}
			} catch(Throwable e) {
				Trace.logError(e);
			} finally {
				workers.remove(domain);
			}
		}
		
		private IApplicationServer connect(Message message) throws Throwable {
			IInterconnectionCenter center = ServerConfig.interconnectionCenter();
			
			try {
				center.probe();
			} catch(Throwable e) {
				message.info("Interconnection Center is unavailable at " + ProxyUtils.getUrl(center));
				return null;
			}
			
			IApplicationServer server = ServerConfig.interconnectionCenter().connect(domain);
			
			if(server == null)
				message.info("Domain '" + domain  + "' is unavailable at Interconnection Center " + ProxyUtils.getUrl(center));

			return server;
		}
		
		private boolean send(Message message) throws Throwable {
			if(server == null)
				server = connect(message);

			if(server == null)
				return false;

			try {
				return message.isFile() ? sendFile(message) : sendMessage(message);
			} catch(Throwable e) {
				message.info(e.getMessage());
				throw e;
			}
		}

		private boolean sendMessage(Message message) throws Throwable {
			Connection connection = ConnectionManager.get();

			try {
				connection.beginTransaction();
				
				message.beforeExport();
	
				if(server.accept(message)) {
					message.afterExport();
					message.processed("OK");
				}
				
				connection.commit();
				return true;
				
			} catch (Throwable e) {
				connection.rollback();
				throw e;
			}
		}
		
		private boolean sendFile(Message message) throws Throwable {
			file file = message.getFile();
			
			if(server.hasFile(file)) {
				message.processed("Skipped");
				return true;
			}
				
			Connection connection = ConnectionManager.get();

			while((file = file.nextPart()) != null) {
				try {
					connection.beginTransaction();
	
					boolean reset = !server.accept(file);
					
					message.transferred(reset ? 0 : file.offset());
	
					connection.commit();
	
					if(reset) {
						message.info("Reset");
						return false;
					}
				} catch (Throwable e) {
					connection.rollback();
					throw e;
				}
			}

			message.processed("OK");
			return true;
		}
	}

	@Override
	protected void z8_exec(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
		sendMessages();
	}

	private void sendMessages() {
		Collection<String> addresses = messages.getAddresses();

		for (String address : addresses) {
			TransportThread thread = workers.get(address);

			if (thread == null) {
				TransportThread worker = new TransportThread(address);
				workers.put(address, worker);
				worker.start();
			}
		}
	}

	static public boolean accept(Object object) {
		return Import.importObject(object);
	}
}
