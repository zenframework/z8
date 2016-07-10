package org.zenframework.z8.server.ie;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class RmiTransportProcedure extends Procedure {

	static private IInterconnectionCenter interconnectionCenter;
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
			} finally {
				workers.remove(domain);
			}
		}
		
		private boolean sendMessage(Message message) {
			Connection connection = ConnectionManager.get();
			try {
				if(server == null)
					server = getInterconnectionCenter().connect(domain);
	
				if(server == null) {
					messages.setError(message, "'" + domain + "' server is unavalable");
					return false;
				}

				if(!message.isFile()) {
					connection.beginTransaction();
					
					message.beforeExport();
	
					long total = java.lang.System.currentTimeMillis();
					long timing = server.accept(message);
					total = java.lang.System.currentTimeMillis() - total;
	
					message.afterExport();

					message.processed((total - timing) + "/" + timing + "/" + total + " ms (t/p/tt)");

					connection.commit();
				} else {
					file file = message.getFile().nextPart();

					while(file != null) {
						connection.beginTransaction();

						server.accept(file);
						
						message.transferred(file.offset());

						connection.commit();

						file = file.nextPart();
					}
					
					message.processed();
				}
				return true;
			} catch (Throwable e) {
				connection.rollback();
				Trace.logError(e);
				messages.setError(message, e);
				return false;
			}
		}
	}

	@Override
	protected void z8_exec(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
		sendMessages();
	}

	private void sendMessages() {
		if(!ApplicationServer.config().interconnectionEnabled())
			return;

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

	private IInterconnectionCenter getInterconnectionCenter() throws RemoteException {
		if(interconnectionCenter == null) {
			ServerConfig config = ApplicationServer.config(); 
			String host = config.interconnectionCenterHost();
			int port = config.interconnectionCenterPort();
	
			interconnectionCenter = Rmi.get(IInterconnectionCenter.class, host, port);
		}
		return interconnectionCenter;

	}
	
	static public void accept(Object object) {
		Import.importObject(object);
	}
}
