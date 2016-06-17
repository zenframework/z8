package org.zenframework.z8.server.engine;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.ie.RmiTransport;
import org.zenframework.z8.server.ie.TransportRoute;
import org.zenframework.z8.server.ie.TransportRoutes;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.FileKeyValue;
import org.zenframework.z8.server.utils.IKeyValue;

public class TransportCenter extends RmiServer implements ITransportCenter {

	private static TransportCenter INSTANCE;

	private static final long serialVersionUID = 5894774801318237091L;

	private static final StoreMode STORE_MODE = StoreMode.TABLE;

	private final Store store;

	protected TransportCenter(int unicastPort) throws RemoteException {
		super(unicastPort, ITransportCenter.class);
		store = STORE_MODE == StoreMode.TABLE ? new TableStore() : new FileStore();
	}

	@Override
	public void registerTransportService(String receiver, int localRegistryPort) throws RemoteException {
		try {
			String clientHost = Rmi.getClientHost();
			if (clientHost == null)
				clientHost = new RmiAddress(Properties.getProperty(ServerRuntime.TransportCenterAddressProperty)).getHost();
			store.setRoute(receiver, clientHost + ':' + localRegistryPort);
		} catch (Exception e) {
			throw new RemoteException("Can't register transport server '" + receiver + "'", e);
		}
	}

	@Override
	public List<TransportRoute> getTransportRoutes(String domain) throws RemoteException {
		return store.getRoutes(domain);
	}

	public static void start(ServerConfig config) throws RemoteException {
		if (INSTANCE == null) {
			INSTANCE = new TransportCenter(config.getUnicastTransportCenterPort());
			INSTANCE.start();
		}
	}

	private static enum StoreMode {

		FILE, TABLE

	}

	private static interface Store {

		void setRoute(String domain, String address);

		List<TransportRoute> getRoutes(String domain);

	}

	private static class FileStore implements Store {

		private final IKeyValue<String, String> store = new FileKeyValue(new File(Z8Context.getConfig().getWorkingPath(),
				"transport-servers.xml"));

		@Override
		public void setRoute(String domain, String address) {
			store.set(domain, address);
		}

		@Override
		public List<TransportRoute> getRoutes(final String domain) {
			return Arrays.<TransportRoute> asList(new TransportRoute(domain, RmiTransport.PROTOCOL, store.get(domain), 0,
					true));
		}

	}

	private static class TableStore implements Store {

		final TransportRoutes transportRoutes = TransportRoutes.newInstance();

		@Override
		public void setRoute(String domain, String address) {
			transportRoutes.setRoute(domain, RmiTransport.PROTOCOL, address, 0, true);
		}

		@Override
		public List<TransportRoute> getRoutes(String domain) {
			return transportRoutes.readRoutes(domain, null, false);
		}

	}

}
