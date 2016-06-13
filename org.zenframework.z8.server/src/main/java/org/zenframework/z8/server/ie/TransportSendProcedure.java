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
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ErrorUtils;

public class TransportSendProcedure extends Procedure {

	private static final Log LOG = LogFactory.getLog(TransportSendProcedure.class);

	public static class CLASS<T extends TransportSendProcedure> extends Procedure.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TransportSendProcedure.class);
			setAttribute(Native, TransportSendProcedure.class.getCanonicalName());
			setAttribute(Job, "");
		}

		@Override
		public Object newObject(IObject container) {
			return new TransportSendProcedure(container);
		}
	}

	protected final ExportMessages messages = ExportMessages.newInstance();
	protected final TransportContext.CLASS<TransportContext> context = new TransportContext.CLASS<TransportContext>();
	protected final TransportEngine engine = TransportEngine.getInstance();

	public TransportSendProcedure(IObject container) {
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
		JsonArray configuration = new JsonArray(((string) getParameter(IObject.Settings).get()).get());

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

		// Обработка внутренней исходящей очереди
		ids = messages.getExportMessages(selfAddress, configuration);
		transportRoutes.checkInactiveRoutes();

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

}
