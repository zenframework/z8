package org.zenframework.z8.server.engine;

import java.io.File;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.Arrays;
import java.util.List;

import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.table.system.SystemDomains;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.ie.TransportRoute;
import org.zenframework.z8.server.ie.RmiTransport;
import org.zenframework.z8.server.ie.TransportRoutes;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.sql.sql_string;
import org.zenframework.z8.server.utils.FileKeyValue;
import org.zenframework.z8.server.utils.IKeyValue;

public class TransportCenter extends RmiServer implements ITransportCenter {

	private static TransportCenter INSTANCE;

	private static final long serialVersionUID = 5894774801318237091L;

	private static final StoreMode STORE_MODE = StoreMode.TABLE;

	private final Store store;

	protected TransportCenter() throws RemoteException {
		super(ITransportCenter.class);
		store = STORE_MODE == StoreMode.TABLE ? new TableStore() : new FileStore();
	}

	@Override
	public void registerTransportService(String receiver, int localRegistryPort) throws RemoteException {
		String clientHost;
		try {
			// Try detect remote client host
			clientHost = RemoteServer.getClientHost();
		} catch (ServerNotActiveException e) {
			// If ServerNotActiveException, transport center was called locally
			try {
				clientHost = new RmiAddress(Properties.getProperty(ServerRuntime.TransportCenterAddressProperty)).getHost();
			} catch (URISyntaxException e1) {
				throw new RemoteException("Can't register transport server '" + receiver + "'", e);
			}
		}
		try {
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
			INSTANCE = new TransportCenter();
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
			return Arrays.<TransportRoute> asList(new TransportRoute(guid.create(), guid.create(), domain,
					RmiTransport.PROTOCOL, store.get(domain), 0, true));
		}

	}

	private static class TableStore implements Store {

		final SystemDomains domains = SystemDomains.newInstance();
		final TransportRoutes transportRoutes = TransportRoutes.newInstance();

		@Override
		public void setRoute(String domain, String address) {
			if (!domains.hasRecord(new Rel(domains.id.get(), Operation.Eq, new sql_string(domain))))
				throw new exception("Domain '" + domain + "' does not exist");
			transportRoutes.setRoute(guid.create(), domains.recordId(), domain, RmiTransport.PROTOCOL, address, 0, true);
		}

		@Override
		public List<TransportRoute> getRoutes(String domain) {
			return transportRoutes.readActiveRoutes(domain, null);
		}

	}

}
