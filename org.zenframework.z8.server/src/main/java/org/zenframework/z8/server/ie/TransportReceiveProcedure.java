package org.zenframework.z8.server.ie;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.guid;

public class TransportReceiveProcedure extends Procedure {

	private static final Log LOG = LogFactory.getLog(TransportReceiveProcedure.class);

	public static class CLASS<T extends TransportReceiveProcedure> extends Procedure.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TransportReceiveProcedure.class);
			setAttribute(Native, TransportReceiveProcedure.class.getCanonicalName());
			setAttribute(Job, "");
		}

		@Override
		public Object newObject(IObject container) {
			return new TransportReceiveProcedure(container);
		}
	}

	protected final ExportMessages messages = ExportMessages.newInstance();
	protected final TransportContext.CLASS<TransportContext> context = new TransportContext.CLASS<TransportContext>();
	protected final TransportEngine engine = TransportEngine.getInstance();

	public TransportReceiveProcedure(IObject container) {
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

	protected void z8_init() {}

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
