package org.zenframework.z8.server.ie;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ITransportService;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.guid;

public class TransportProcedure extends Procedure {

	private static final Log LOG = LogFactory.getLog(TransportProcedure.class);

	public static final guid PROCEDURE_ID = new guid("E43F94C6-E918-405D-898C-B915CC51FFDF");

	public static class CLASS<T extends TransportProcedure> extends Procedure.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TransportProcedure.class);
			setAttribute(Native, TransportProcedure.class.getCanonicalName());
			setAttribute(Job, "");
		}

		@Override
		public Object newObject(IObject container) {
			return new TransportProcedure(container);
		}
	}

	private static class PreserveExportMessagesListener implements Properties.Listener {

		@Override
		public void onPropertyChange(String key, String value) {
			if (ServerRuntime.PreserveExportMessagesProperty.equalsKey(key))
				preserveExportMessages = Boolean.parseBoolean(value);
		}

	}

	static {
		Properties.addListener(new PreserveExportMessagesListener());
	}

	private static volatile boolean preserveExportMessages = Boolean.parseBoolean(Properties
			.getProperty(ServerRuntime.PreserveExportMessagesProperty));

	protected final TransportContext.CLASS<TransportContext> context = new TransportContext.CLASS<TransportContext>();
	protected final TransportEngine engine = TransportEngine.getInstance();

	public TransportProcedure(IObject container) {
		super(container);
		useTransaction.set(false);
	}

	@Override
	public void constructor2() {
		super.constructor2();
		z8_init();
	}

	private Message newMessage() {
		return z8_newMessage().get();
	}
	
	public Message.CLASS<? extends Message> z8_newMessage() {
		return new Message.CLASS<Message>();
	}
	
	@Override
	protected void z8_exec(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {

		String selfAddress = context.get().check().getProperty(TransportContext.SelfAddressProperty);
		Connection connection = ConnectionManager.get();

		ExportMessages messages = new ExportMessages.CLASS<ExportMessages>().get();

		TransportRoutes transportRoutes = TransportRoutes.instance();

		String transportCenter = Properties.getProperty(ServerRuntime.TransportCenterAddressProperty).trim();
		boolean registerInTransportCenter = Boolean.parseBoolean(Properties
				.getProperty(ServerRuntime.RegisterInTransportCenterProperty));

		if (registerInTransportCenter && !transportCenter.isEmpty()) {
			try {
				Rmi.get(ITransportService.class).checkRegistration(selfAddress);
			} catch (Exception e) {
				LOG.error("Can't check transport server registration for '" + selfAddress + "' in transport center '"
						+ transportCenter + "'", e);
			}
		}

		// Обработка внутренней входящей очереди
		List<guid> ids = messages.getImportMessages(selfAddress);
		for (guid id : ids) {
			Message message = messages.getMessage(id, newMessage());
			try {
				importMessage(message);
			} catch(Throwable e) {
				Trace.logError(e);
				break;
			}
		}

		// Обработка внутренней исходящей очереди
		ids = messages.getExportMessages(selfAddress);
		transportRoutes.checkInactiveRoutes();

		for (guid id : ids) {

			Message message = messages.getMessage(id, newMessage());
			if (message == null)
				continue;

			List<TransportRoute> routes = transportRoutes.readActiveRoutes(message.getAddress(), transportCenter);

			for (TransportRoute route : routes) {

				Transport transport = engine.getTransport(context.get(), route.getProtocol());
				if (transport == null)
					continue;
				try {
					transport.connect(); // это долго, если сервера нет или сдох
				} catch (TransportException e) {
					log("Can't import message via protocol '" + transport.getProtocol() + "'", e);
					transport.close();
					transportRoutes.disableRoute(route.getRouteId(), e.getMessage());
					continue;
				}

				try {
					exportMessage(message, transport, route);
					transport.commit();
					break;
				} catch (TransportException e) {
					transport.close();
					transportRoutes.disableRoute(route.getRouteId(), e.getMessage());
				}
			}
		
		}

		// Чтение входящих сообщений
		for (Transport transport : engine.getEnabledTransports(context.get())) {
			try {
				transport.connect();
				for (Message message = transport.receive(); message != null; message = transport.receive()) {
					try {
						connection.beginTransaction();
						messages.addMessage(message, transport.getUrl(message.getSender()), ExportMessages.Direction.IN);
						Import.importFiles(message);
						connection.commit();
						transport.commit();
					} catch (Throwable e) {
						log("Can't save incoming message " + message.getId() + " from '"
								+ transport.getUrl(message.getAddress()) + "'", e);
						connection.rollback();
						transport.rollback();
					}
				}
			} catch (TransportException e) {
				log("Can't import message via protocol '" + transport.getProtocol() + "'", e);
				transport.close();
			}
		}

	}

	static public void importMessage(Message message) throws Throwable {
		Connection connection = ConnectionManager.get();
		
		try {
			connection.beginTransaction();
			message.runImport(null, preserveExportMessages);
			connection.commit();
		} catch (Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}

	static public void exportMessage(Message message, Transport transport, TransportRoute route) throws TransportException {
		Connection connection = ConnectionManager.get();
		
		try {
			connection.beginTransaction();
			message.runExport(transport, route, preserveExportMessages);
			connection.commit();
		} catch (Throwable e) {
			connection.rollback();
			Trace.logError(e);

			if(e instanceof TransportException)
				throw (TransportException)e;
			
			message.setError(e);
		}
	}
	
	protected void z8_init() {}
}
