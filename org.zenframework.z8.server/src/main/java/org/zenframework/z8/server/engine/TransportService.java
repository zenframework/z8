package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.Files;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.table.system.SystemDomains;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.ie.ExportMessages;
import org.zenframework.z8.server.ie.IeUtil;
import org.zenframework.z8.server.ie.Import;
import org.zenframework.z8.server.ie.ImportException;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.ie.RmiTransport;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.security.IUser;

public class TransportService extends RmiServer implements ITransportService, Properties.Listener {

	private static final long serialVersionUID = 6031141331643514419L;

	private static final Log LOG = LogFactory.getLog(TransportService.class);

	private static TransportService INSTANCE;

	private final Collection<String> registered = new LinkedList<String>();
	private final Collection<Registrator> registrators = new LinkedList<TransportService.Registrator>();

	private volatile RmiAddress transportCenter;

	private TransportService(ServerConfig config) throws RemoteException {
		super(ITransportService.class);
		try {
			transportCenter = getRmiAddress(Properties.getProperty(ServerRuntime.TransportCenterAddressProperty));
		} catch (URISyntaxException e) {
			LOG.error("Can't get transport center address", e);
		}
		Properties.addListener(this);
	}

	public static void start(ServerConfig config) throws RemoteException {
		if (INSTANCE == null) {
			INSTANCE = new TransportService(config);
			INSTANCE.start();
		}
	}

	@Override
	public void onPropertyChange(String key, String value) {
		if (ServerRuntime.TransportCenterAddressProperty.equalsKey(key)) {
			try {
				transportCenter = getRmiAddress(value);
			} catch (URISyntaxException e) {
				LOG.error("Can't get transport center address", e);
			}
			stopRegistrators();
			Collection<String> addrs;
			synchronized (registered) {
				addrs = new ArrayList<String>(registered);
				registered.clear();
			}
			for (String addr : addrs) {
				checkRegistration(addr);
			}
		}
	}

	@Override
	public void start() throws RemoteException {
		super.start();
		Trace.logEvent("TS: transport server started at '" + getUrl() + "'");
	}

	@Override
	public void stop() throws RemoteException {
		super.stop();
		stopRegistrators();
	}

	@Override
	public void checkRegistration(String selfAddress) {
		if (transportCenter != null && addIfNotContains(registered, selfAddress)) {
			Registrator registrator = new Registrator(transportCenter, selfAddress);
			registrator.start();
			synchronized (registrators) {
				registrators.add(registrator);
			}
		}
	}

	@Override
	public void sendMessage(Message message) throws ImportException {
		IUser user = SystemDomains.newInstance().getDomain(message.getAddress()).getSystemUser();
		IRequest request = new Request(new Session("", user));

		ApplicationServer.setRequest(request);
		try {
			String clientHost = Rmi.getClientHost();
			Import.importMessage(ExportMessages.newInstance(), message,
					IeUtil.getUrl(RmiTransport.PROTOCOL, clientHost != null ? clientHost : "localhost"));
		} finally {
			ApplicationServer.setRequest(null);
		}
	}

	@Override
	public FileInfo readFile(FileInfo fileInfo) throws IOException {
		return Files.newInstance().getFile(fileInfo);
	}

	private void stopRegistrators() {
		synchronized (registrators) {
			for (Registrator registrator : registrators) {
				registrator.active.set(false);
				registrator.interrupt();
			}
			registrators.clear();
		}
	}

	private static <T> boolean addIfNotContains(Collection<T> coll, T obj) {
		synchronized (coll) {
			if (coll.contains(obj))
				return false;
			coll.add(obj);
			return true;
		}
	}

	private static RmiAddress getRmiAddress(String addr) throws URISyntaxException {
		addr = addr.trim();
		return addr != null && !addr.isEmpty() ? new RmiAddress(addr) : null;
	}

	private static class Registrator extends Thread {

		final AtomicBoolean active = new AtomicBoolean(true);
		final RmiAddress transportCenter;
		final String address;

		Registrator(RmiAddress transportCenter, String address) {
			super("TransportRegistrator-" + address);
			this.transportCenter = transportCenter;
			this.address = address;
			setDaemon(true);
		}

		@Override
		public void run() {
			while (active.get()) {
				try {
					Rmi.get(ITransportCenter.class, transportCenter).registerTransportService(address,
							Z8Context.getConfig().getRmiRegistryPort());
					active.set(false);
				} catch (Exception e) {
					LOG.debug("Can't register transport server for '" + address + "'. Retrying...", e);
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						LOG.error("Can't register transport server for '" + address + "'. Thread interrupted", e1);
					}
				}
			}
		}

	}

}
