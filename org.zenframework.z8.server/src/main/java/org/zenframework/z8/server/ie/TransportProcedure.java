package org.zenframework.z8.server.ie;

import java.util.List;

import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.system.MessagesQueue;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ErrorUtils;

public class TransportProcedure extends Procedure {
	private static final String CONFIG_SEND = "send";
	private static final String CONFIG_RECEIVE = "receive";
	private static final String CONFIG_ENABLED = "enabled";
	private static final String CONFIG_FILTERS = "filters";

	public static class CLASS<T extends TransportProcedure> extends Procedure.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TransportProcedure.class);
			setAttribute(Job, "");
		}

		@Override
		public Object newObject(IObject container) {
			return new TransportProcedure(container);
		}
	}

	protected final MessagesQueue messages = MessagesQueue.newInstance();
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

		MessagesQueue messages = MessagesQueue.newInstance();

		TransportRoutes transportRoutes = TransportRoutes.newInstance();

		transportRoutes.checkInactiveRoutes();

		if (sendEnabled) {

			// Обработка внутренней исходящей очереди
			List<guid> ids = messages.getExportMessages(selfAddress, sendFilters);
			for (guid id : ids) {

				Message message = messages.getMessage(id);
				if (message == null)
					continue;

				List<TransportRoute> routes = transportRoutes.readRoutes(message.getAddress(), true);

				for (TransportRoute route : routes) {

					Transport transport = engine.getTransport(context.get(), route.getProtocol());
					if (transport == null)
						continue;
					try {
						transport.connect(); // это долго, если сервера нет или сдох
					} catch (TransportException e) {
						log("Can't connect to '" + route.getDomain() + "' via '" + route.getTransportUrl() + "'", e);
						transport.close();
						transportRoutes.disableRoute(route, ErrorUtils.getMessage(e));
						continue;
					}

					try {
						sendMessage(messages, message, transport, route);
						break;
					} catch (TransportException e) {
						transport.close();
						transportRoutes.disableRoute(route, e.getMessage());
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
					Import.importMessage(message);
				} catch (Throwable e) {
					Trace.logError(e);
					message.info(ErrorUtils.getMessage(e));
				}
			}

		}

	}

	protected void z8_init() {}

	private static void sendMessage(MessagesQueue messages, Message message, Transport transport, TransportRoute route)
			throws TransportException {
		Connection connection = ConnectionManager.get();
		try {
			connection.beginTransaction();
			message.processed(route.getTransportUrl());
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
			Trace.logError(e);

			if (e instanceof TransportException)
				throw (TransportException) e;

			message.info(ErrorUtils.getMessage(e));
		}
	}

	private static void receiveMessage(MessagesQueue messages, Message message, Transport transport)
			throws TransportException {
		Connection connection = ConnectionManager.get();
		try {
			connection.beginTransaction();
			messages.addMessage(message, transport.getProtocol(), MessagesQueue.Direction.IN);
			Import.importFiles(message);
			transport.commit();
			connection.commit();
		} catch (Throwable e) {
			connection.rollback();
			transport.rollback();
			Trace.logError(e);
		}
	}

}
