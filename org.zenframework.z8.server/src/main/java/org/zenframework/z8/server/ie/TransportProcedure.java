package org.zenframework.z8.server.ie;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.ITransportCenter;
import org.zenframework.z8.server.engine.ITransportService;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.engine.RmiAddress;
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

	@Override
	protected void z8_exec(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {

		String selfAddress = context.get().check().getProperty(TransportContext.SelfAddressProperty);
		Connection connection = ConnectionManager.get();

		final ExportMessages messages = ExportMessages.instance();
		TransportRoutes transportRoutes = TransportRoutes.instance();
		Files filesTable = Files.instance();

		String transportCenter = Properties.getProperty(ServerRuntime.TransportCenterAddressProperty).trim();
		boolean registerInTransportCenter = Boolean.parseBoolean(Properties.getProperty(ServerRuntime.RegisterInTransportCenterProperty));

		if (registerInTransportCenter && !transportCenter.isEmpty()) {
			try {
				Rmi.get(ITransportService.class).checkRegistration(selfAddress);
			} catch (Exception e) {
				LOG.error("Can't check transport server registrationa for '" + selfAddress + "' in transport center '"
						+ transportCenter + "'", e);
			}
		}

		// Обработка внутренней входящей очереди
		List<guid> ids = messages.getImportMessages(selfAddress);
		for (guid id : ids) {
			if (!messages.readMessage(id))
				continue;
			try {
				Message.CLASS<Message> message = messages.getMessage();
				connection.beginTransaction();
				messages.processCurrentMessage(null, preserveExportMessages);
				LOG.debug("Receive IE message [" + message.get().getId() + "] by " + messages.getTransportUrl());
				z8_beforeImport(message);
				ApplicationServer.disableEvents();
				try {
					Import.importRecords(message.get());
				} finally {
					ApplicationServer.enableEvents();
				}
				z8_afterImport(message);
				connection.commit();
			} catch (Throwable e) {
				connection.rollback();
				log("Transport messsage '" + id + "' is broken", e);
				messages.setError(id, e);
			}
		}

		// Обработка внутренней исходящей очереди
		ids = messages.getExportMessages(selfAddress);
		transportRoutes.checkInactiveRoutes();

		for (guid id : ids) {
			if (!messages.readMessage(id))
				continue;
			List<TransportRoute> routes;
			routes = transportRoutes.readActiveRoutes(messages.getReceiver());

			if (routes.isEmpty()) {
				try {
					routes = Rmi.get(ITransportCenter.class, new RmiAddress(transportCenter))
							.getTransportRoutes(messages.getReceiver());
					for (TransportRoute route : routes) {
						transportRoutes.setRoute(route);
					}
				} catch (Exception e) {
					LOG.error("Can't get routes from transport center '" + transportCenter + "'", e);
				}
			}

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
					connection.beginTransaction();
					messages.processCurrentMessage(route.getTransportUrl(), preserveExportMessages);
					Message.CLASS<Message> message = messages.getMessage();
					z8_beforeExport(message);
					transport.send(message.get(), route.getAddress());
					transport.commit();
					z8_afterExport(message);
					connection.commit();
					break;
				} catch (Throwable e) {
					connection.rollback();
					messages.setError(id, e);
					log("Can't send message '" + id + "' via '" + route.getAddress() + "'", e);
					if (e instanceof TransportException) {
						transport.close();
						transportRoutes.disableRoute(route.getRouteId(), e.getMessage());
					}
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
						messages.addMessage(message, transport.getUrl(message.getSender()));
						Import.importFiles(message, filesTable);
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

	protected void z8_init() {}

	protected void z8_beforeImport(Message.CLASS<? extends Message> message) {}

	protected void z8_afterImport(Message.CLASS<? extends Message> message) {}

	protected void z8_beforeExport(Message.CLASS<? extends Message> message) {}

	protected void z8_afterExport(Message.CLASS<? extends Message> message) {}

}
