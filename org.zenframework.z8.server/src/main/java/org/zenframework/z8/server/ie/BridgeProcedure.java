package org.zenframework.z8.server.ie;

import java.net.URI;
import java.net.URISyntaxException;

import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.guid;

public class BridgeProcedure extends Procedure {

	public static final guid PROCEDURE_ID = new guid("3394E6B4-F5B2-48A4-B881-7BDB8EC1C6FD");

	public static class CLASS<T extends BridgeProcedure> extends Procedure.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(BridgeProcedure.class);
			setAttribute(Native, BridgeProcedure.class.getCanonicalName());
			setAttribute(Job, "");
		}

		@Override
		public Object newObject(IObject container) {
			return new BridgeProcedure(container);
		}
	}

	protected final TransportContext.CLASS<TransportContext> context = new TransportContext.CLASS<TransportContext>();
	protected final TransportEngine engine = TransportEngine.getInstance();

	public BridgeProcedure(IObject container) {
		super(container);
		useTransaction.set(false);
	}

	@Override
	public void constructor2() {
		super.constructor2();
		z8_init();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void z8_exec(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {

		for (String uri : Properties.getProperty(ServerRuntime.BridgeUrlsProperty).split("\\,")) {
			String uris[] = uri.split("/");
			if (uris.length != 2)
				throw new RuntimeException("Incorrect bridge URI '" + uri + "'");
			URI inUri, outUri;
			try {
				inUri = new URI(uris[0].trim());
				outUri = new URI(uris[1].trim());
			} catch (URISyntaxException e) {
				throw new RuntimeException("Incorrect bridge URI '" + uri + "'", e);
			}
			context.get().setProperty(TransportContext.SelfAddressProperty, inUri.getHost());
			context.get().check();
			Transport transIn = engine.getTransport(context.get(), inUri.getScheme());
			Transport transOut = engine.getTransport(context.get(), outUri.getScheme());

			if (transIn != null && transOut != null) {
				try {
					transIn.connect();
					transOut.connect();
					for (Message message = transIn.receive(); message != null; message = transIn.receive()) {
						try {
							message.getFiles().addAll(
									IeUtil.filesToFileInfos(message.getExportEntry().getFiles().getFile(), true));
							z8_beforeTransfer((Message.CLASS<Message>) message.getCLASS());
							transOut.send(message, outUri.getHost());
							transIn.commit();
							transOut.commit();
							z8_afterTransfer((Message.CLASS<Message>) message.getCLASS());
						} catch (Throwable e) {
							log("Can't transfer message '" + message.getId() + "' from '"
									+ transIn.getUrl(message.getAddress()) + "' to '"
									+ transOut.getUrl(message.getAddress()) + "'", e);
							transOut.rollback();
							transIn.rollback();
						}
					}
				} catch (TransportException e) {
					log("Can't create '" + transIn.getProtocol() + "-to-" + transOut.getProtocol() + "' bridge", e);
					transIn.close();
					transOut.close();
				}
			}
		}

	}

	protected void z8_init() {}

	protected void z8_beforeTransfer(Message.CLASS<? extends Message> message) {}

	protected void z8_afterTransfer(Message.CLASS<? extends Message> message) {}

}
