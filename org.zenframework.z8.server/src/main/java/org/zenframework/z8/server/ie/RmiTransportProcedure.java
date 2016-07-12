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
					if(!sendMessage(message))
						return;
				}
			} catch(Throwable e) {
				Trace.logError(e);
			} finally {
				workers.remove(domain);
			}
		}
		
		private String interconnectionCenterUnavailableMessage() {
			return "Interconnection Center is unavailable at " + ServerConfig.interconnectionCenterHost() + ":" + ServerConfig.interconnectionCenterPort();
		}
		
		private String domainUnavailableMessage() {
			return "Domain '" + domain  + "' is unavailable at Interconnection Center " + ServerConfig.interconnectionCenterHost() + ":" + ServerConfig.interconnectionCenterPort();
		}

		private IApplicationServer connect() throws Throwable {
			try {
				ServerConfig.interconnectionCenter().probe();
			} catch(Throwable e) {
				throw new RuntimeException(interconnectionCenterUnavailableMessage());
			}
			
			try {
				IApplicationServer server = ServerConfig.interconnectionCenter().connect(domain);
				if(server == null)
					throw new RuntimeException(domainUnavailableMessage());
				return server;
			} catch(Throwable e) {
				ServerConfig.resetInterconnectionCenter();
				throw new RuntimeException(domainUnavailableMessage());
			}
		}
		
		private boolean sendMessage(Message message) throws Throwable {
			Connection connection = null;
			
			try {
				if(server == null) {
					try {
						server = connect();
					} catch(Throwable e) {
						message.info(e.getMessage());
						return false;
					}
				}
				
				connection = ConnectionManager.get();

				if(!message.isFile()) {
					connection.beginTransaction();
					
					message.beforeExport();
	
					if(server.accept(message)) {
						message.afterExport();
						message.processed();
					}
					
					connection.commit();
				} else {
					file file = message.getFile().nextPart();

					while(file != null) {
						connection.beginTransaction();

						boolean reset = !server.accept(file);
						
						message.transferred(reset ? 0 : file.offset());

						connection.commit();

						if(reset)
							return false;

						file = file.nextPart();
					}
					
					message.processed();
				}
				return true;
			} catch (Throwable e) {
				if(connection != null)
					connection.rollback();
				message.error(e.getMessage());
				throw e;
			}
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
