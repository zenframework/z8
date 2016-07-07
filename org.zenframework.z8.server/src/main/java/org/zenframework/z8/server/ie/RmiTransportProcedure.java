package org.zenframework.z8.server.ie;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.system.SystemDomains;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.security.IUser;
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

			for (guid id : ids) {
				Message message = messages.getMessage(id);

				if (message == null)
					continue;

				sendMessage(message);
			}
		}
		
		void sendMessage(Message message) {
			Connection connection = ConnectionManager.get();
			try {
				connection.beginTransaction();
				
				List<ExportEntry.Files.File> files = message.getExportEntry().getFiles().getFile();
				message.setFiles(IeUtil.filesToFileInfos(files, true));

				message.beforeExport();

				if(server == null)
					server = getInterconnectionCenter().connect(domain);
	
				long startAt = java.lang.System.currentTimeMillis();
				server.accept(message);
				messages.processed(new guid(message.getId()), (java.lang.System.currentTimeMillis() - startAt) + " ms");

				message.afterExport();
				
				connection.commit();
			} catch (Throwable e) {
				connection.rollback();
				Trace.logError(e);
				messages.setError(message, e);
			} finally {
				workers.remove(domain);
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
		Message message = (Message)object;
		
		IUser user = SystemDomains.newInstance().getDomain(message.getAddress()).getSystemUser();

		IRequest request = new Request(new Session("", user));

		ApplicationServer.setRequest(request);
		
		try {
			Import.importMessage(ExportMessages.newInstance(), message);
		} finally {
			ConnectionManager.release();
			ApplicationServer.setRequest(null);
		}
	}
	
}
