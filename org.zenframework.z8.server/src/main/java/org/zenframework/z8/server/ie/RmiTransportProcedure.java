package org.zenframework.z8.server.ie;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class RmiTransportProcedure extends Procedure {

	static private Map<String, TransportThread> workers = new HashMap<String, TransportThread>();

	protected final ExportMessages messages = ExportMessages.newInstance();

	public static class CLASS<T extends RmiTransportProcedure> extends Procedure.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(RmiTransportProcedure.class);
			setAttribute(Job, "");
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
			Collection<guid> ids = messages.getExportMessages(domain);

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
			try {
				ServerConfig.interconnectionCenter().probe();
			} catch(Throwable e) {
				message.info("Interconnection Center is unavailable at " + ServerConfig.interconnectionCenterHost() + ":" + ServerConfig.interconnectionCenterPort());
				return null;
			}
			
			IApplicationServer server = null;
			
			try {
				server = ServerConfig.interconnectionCenter().connect(domain);
			} catch(Throwable e) {
				ServerConfig.resetInterconnectionCenter();
			} finally {
				if(server == null)
					message.info("Domain '" + domain  + "' is unavailable at Interconnection Center " + ServerConfig.interconnectionCenterHost() + ":" + ServerConfig.interconnectionCenterPort());
			}

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
					message.processed();
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
