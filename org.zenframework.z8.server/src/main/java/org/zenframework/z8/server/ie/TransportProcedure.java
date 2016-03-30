package org.zenframework.z8.server.ie;

import java.rmi.RemoteException;
import java.util.Arrays;
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
import org.zenframework.z8.server.engine.Z8Context;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.bool;
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
		TransportRoutes transportRoutes = null;
		Files filesTable = Files.instance();

		final String transportRegistryHost = Z8Context.getConfig().getTransportCenterHost();
		final int transportRegistryPort = Z8Context.getConfig().getTransportCenterPort();

		if (!transportRegistryHost.isEmpty()) {
			try {
				Rmi.get(ITransportService.class).checkRegistration(selfAddress);
			} catch (Exception e) {
				LOG.error("Can't check transport server registrationa for '" + selfAddress + "' in central registry '"
						+ Z8Context.getConfig().getTransportCenterHost() + ':'
						+ Z8Context.getConfig().getTransportCenterPort() + "'", e);
			}
		} else {
			transportRoutes = TransportRoutes.instance();
		}

		// Обработка внутренней входящей очереди
		messages.readImportMessages(selfAddress);
		while (messages.next()) {
			try {
				Message.CLASS<Message> message = messages.getMessage();
				connection.beginTransaction();
				beginProcessMessage(messages, null);
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
				log("Transport messsage '" + messages.recordId() + "' is broken", e);
				messages.setError(messages.recordId(), e.getMessage());
			}
		}

		// Обработка внутренней исходящей очереди
		messages.readExportMessages(selfAddress);
		if (transportRoutes != null) {
			transportRoutes.checkInactiveRoutes();
		}

		while (messages.next()) {
			List<AbstractRoute> routes;
			if (transportRoutes != null) {
				routes = transportRoutes.readActiveRoutes(messages.getReceiver());
			} else {
				routes = Arrays.<AbstractRoute> asList(new AbstractRoute() {

					@Override
					public String getReceiver() {
						return messages.getReceiver();
					}

					@Override
					public String getProtocol() {
						return RmiTransport.PROTOCOL;
					}

					@Override
					public String getAddress() {
						try {
							return Rmi.get(ITransportCenter.class, transportRegistryHost, transportRegistryPort)
									.getTransportServerAddress(getReceiver());
						} catch (RemoteException e) {
							throw new RuntimeException("Can't get transport address for '" + getReceiver() + "'", e);
						}
					}

				});
			}

			for (AbstractRoute route : routes) {

				Transport transport = engine.getTransport(context.get(), route.getProtocol());
				if (transport == null)
					continue;
				try {
					transport.connect(); // это долго, если сервера нет или сдох
				} catch (TransportException e) {
					log("Can't import message via protocol '" + transport.getProtocol() + "'", e);
					transport.close();
					if (transportRoutes != null) {
						transportRoutes.disableRoute(route.getRouteId(), e.getMessage());
					}
					continue;
				}

				try {
					connection.beginTransaction();
					beginProcessMessage(messages, route.getTransportUrl());
					Message.CLASS<Message> message = messages.getMessage();
					z8_beforeExport(message);
					transport.send(message.get(), route.getAddress());
					transport.commit();
					z8_afterExport(message);
					connection.commit();
					break;
				} catch (Throwable e) {
					connection.rollback();
					messages.setError(messages.recordId(), e.getMessage());
					log("Can't send messsage '" + messages.recordId() + "'", e);
					if (e instanceof TransportException) {
						transport.close();
						if (transportRoutes != null) {
							transportRoutes.disableRoute(route.getRouteId(), e.getMessage());
						}
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

	private static void beginProcessMessage(ExportMessages messages, String transportUrl) {
		if (preserveExportMessages) {
			if (transportUrl != null)
				messages.name.get().set(transportUrl);
			messages.processed.get().set(new bool(true));
			messages.update(messages.recordId());
		} else {
			messages.destroy(messages.recordId());
		}
	}

	protected void z8_init() {}

	protected void z8_beforeImport(Message.CLASS<? extends Message> message) {}

	protected void z8_afterImport(Message.CLASS<? extends Message> message) {}

	protected void z8_beforeExport(Message.CLASS<? extends Message> message) {}

	protected void z8_afterExport(Message.CLASS<? extends Message> message) {}

}
