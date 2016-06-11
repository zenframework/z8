package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ITransportCenter;
import org.zenframework.z8.server.engine.ITransportService;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.engine.RmiAddress;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.guid;

public class RmiTransportProcedure extends Procedure {

	private static final Log LOG = LogFactory.getLog(RmiTransportProcedure.class);

	public static final guid PROCEDURE_ID = new guid("E43F94C6-E918-405D-898C-B915CC51FFDF");

	public static final ExportMessages messages = ExportMessages.newInstance();

	public final TransportContext.CLASS<TransportContext> context = new TransportContext.CLASS<TransportContext>();

	public static class CLASS<T extends RmiTransportProcedure> extends Procedure.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(RmiTransportProcedure.class);
			setAttribute(Native, RmiTransportProcedure.class.getCanonicalName());
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
		z8_init();
	}

	public static class TransportThread extends Thread {
		private String address;
		private String sender;

		public TransportThread(String sender, String address) {
			super(address);
			this.sender = sender;
			this.address = address;
		}

		@Override
		public void run() {
			Collection<guid> ids = messages.getExportMessages(sender, address);

			for (guid id : ids) {
				Message message = messages.getMessage(id);

				if (message == null)
					continue;

				RmiTransportProcedure.sendMessage(message);
			}
		}
	}

	public static Map<String, TransportThread> workers = new HashMap<String, TransportThread>();

	@Override
	protected void z8_exec(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
		sendMessages();
	}

	private void register(String sender) {
		String transportCenter = Properties.getProperty(ServerRuntime.TransportCenterAddressProperty).trim();
		boolean registerInTransportCenter = Boolean.parseBoolean(Properties
				.getProperty(ServerRuntime.RegisterInTransportCenterProperty));

		if (registerInTransportCenter && !transportCenter.isEmpty()) {
			try {
				Rmi.get(ITransportService.class).checkRegistration(sender);
			} catch (Throwable e) {
				Trace.logError("Can't check transport server registration for '" + sender + "' in transport center '"
						+ transportCenter + "'", e);
			}
		}
	}

	private void sendMessages() {
		String sender = context.get().check().getProperty(TransportContext.SelfAddressProperty);

		register(sender);

		Collection<String> addresses = messages.getAddresses(sender);

		for (String address : addresses) {
			TransportThread thread = workers.get(address);

			if (thread == null || !thread.isAlive()) {
				TransportThread worker = new TransportThread(sender, address);
				workers.put(address, worker);
				worker.start();
			}
		}
	}

	protected void z8_init() {}

	public static void sendMessage(Message message) {
		Connection connection = ConnectionManager.get();
		try {
			connection.beginTransaction();
			messages.processed(new guid(message.getId()), null);
			message.getFiles().addAll(
					IeUtil.filesToFileInfos(message.getExportEntry().getFiles().getFile(), message.isSendFilesContent()));
			message.beforeExport();
			String transportCenter = Properties.getProperty(ServerRuntime.TransportCenterAddressProperty).trim();
			List<TransportRoute> routes = Rmi.get(ITransportCenter.class, new RmiAddress(transportCenter))
					.getTransportRoutes(message.getAddress());
			if (routes.size() != 1)
				throw new RuntimeException("bad routes");
			String rmiAddress = routes.get(0).getAddress();
			getServer(rmiAddress).sendMessage(message);
			message.afterExport();
			connection.commit();
		} catch (Throwable e) {
			connection.rollback();
			LOG.error("Can't send message '" + message, e);
			messages.setError(message, e);
		}
	}

	private static ITransportService getServer(String transportAddress) throws IOException, URISyntaxException {
		RmiAddress address = new RmiAddress(transportAddress);
		return (ITransportService) Rmi.get(ITransportService.class, address);
	}

}
