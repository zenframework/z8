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
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ErrorUtils;

public class TransportProcedure extends Procedure {

	private static final Log LOG = LogFactory.getLog(TransportProcedure.class);

	private static final String CONFIG_SEND = "send";
	private static final String CONFIG_RECEIVE = "receive";
	private static final String CONFIG_ENABLED = "enabled";
	private static final String CONFIG_FILTERS = "filters";

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

	protected final ExportMessages messages = ExportMessages.newInstance();
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

		JsonObject configuration = new JsonObject(((string) getParameter(IObject.Settings).get()).get());

		JsonObject sendConfig = configuration.has(CONFIG_SEND) ? configuration.getJsonObject(CONFIG_SEND) : new JsonObject();
		boolean sendEnabled = sendConfig.has(CONFIG_ENABLED) ? sendConfig.getBoolean(CONFIG_ENABLED) : true;

		JsonArray sendFilters = sendConfig.has(CONFIG_FILTERS) ? sendConfig.getJsonArray(CONFIG_FILTERS) : null;
		JsonObject receiveConfig = configuration.has(CONFIG_RECEIVE) ? configuration.getJsonObject(CONFIG_RECEIVE)
				: new JsonObject();
		boolean receiveEnabled = receiveConfig.has(CONFIG_ENABLED) ? receiveConfig.getBoolean(CONFIG_ENABLED) : true;

		ExportMessages messages = ExportMessages.newInstance();

		TransportRoutes transportRoutes = TransportRoutes.newInstance();

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

		transportRoutes.checkInactiveRoutes();

		if (sendEnabled) {

			// Обработка внутренней исходящей очереди
			List<guid> ids = messages.getExportMessages(selfAddress, sendFilters);
			for (guid id : ids) {

				Message message = messages.getMessage(id);
				if (message == null)
					continue;

				List<TransportRoute> routes = transportRoutes.readRoutes(message.getAddress(), transportCenter, false);

				for (TransportRoute route : routes) {

					Transport transport = engine.getTransport(context.get(), route.getProtocol());
					if (transport == null)
						continue;
					try {
						transport.connect(); // это долго, если сервера нет или сдох
					} catch (TransportException e) {
						log("Can't connect to '" + route.getDomain() + "' via '" + route.getTransportUrl() + "'", e);
						transport.close();
						transportRoutes.disableRoute(route.getRouteId(), ErrorUtils.getMessage(e));
						continue;
					}

					try {
						sendMessage(messages, message, transport, route);
						break;
					} catch (TransportException e) {
						transport.close();
						transportRoutes.disableRoute(route.getRouteId(), e.getMessage());
					}
				}

			}

		}

		if (receiveEnabled) {

			// Чтение входящих сообщений
			for (Transport transport : engine.getEnabledTransports(context.get())) {
				try {
					transport.connect();
					for (Message message = transport.receive(); message != null; message = transport.receive()) {
						receiveMessage(messages, message, transport);
					}
				} catch (TransportException e) {
					log("Can't import message via protocol '" + transport.getProtocol() + "'", e);
					transport.close();
				}
			}

			// Обработка внутренней входящей очереди
			List<guid> ids = messages.getImportMessages(selfAddress);
			for (guid id : ids) {
				Message message = messages.getMessage(id);
				try {
					Import.importMessage(messages, message, null);
				} catch (Throwable e) {
					LOG.error("Can't import message '" + message + "'", e);
					break;
				}
			}

		}

	}

	protected void z8_init() {}

	private static void sendMessage(ExportMessages messages, Message message, Transport transport, TransportRoute route)
			throws TransportException {
		Connection connection = ConnectionManager.get();
		try {
			connection.beginTransaction();
			messages.processed(new guid(message.getId()), route.getTransportUrl());
			message.getFiles().addAll(
					IeUtil.filesToFileInfos(message.getExportEntry().getFiles().getFile(), message.isSendFilesContent()));
			message.beforeExport();
			transport.send(message, route.getAddress());
			message.afterExport();
			transport.commit();
			connection.commit();
		} catch (Throwable e) {
			transport.rollback();
			connection.rollback();
			LOG.error("Can't send message '" + message + "' via '" + route.getTransportUrl() + "'", e);

			if (e instanceof TransportException)
				throw (TransportException) e;

			messages.setError(message, e);
		}
	}

	private static void receiveMessage(ExportMessages messages, Message message, Transport transport)
			throws TransportException {
		Connection connection = ConnectionManager.get();
		try {
			connection.beginTransaction();
			messages.addMessage(message, transport.getProtocol(), ExportMessages.Direction.IN);
			Import.importFiles(message);
			transport.commit();
			connection.commit();
		} catch (Throwable e) {
			connection.rollback();
			transport.rollback();
			LOG.error("Can't import message '" + message + "' from '" + transport.getUrl(message.getAddress()) + "'", e);
		}
	}

}
