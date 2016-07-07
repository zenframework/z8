package org.zenframework.z8.interconnection;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.HubServer;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.engine.IServerInfo;
import org.zenframework.z8.server.engine.ServerInfo;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.utils.ArrayUtils;

public class InterconnectionCenter extends HubServer implements IInterconnectionCenter {

	private static final long serialVersionUID = -2249011505751910023L;

	private static final String cache = "interconnection.center.cache";

	static private InterconnectionCenter instance = null;
	
	private ServerConfig config;

	public static IInterconnectionCenter launch(ServerConfig config) throws RemoteException {
		if(instance == null) {
			instance = new InterconnectionCenter(config);
			instance.start();
		}
		return instance;
	}

	private InterconnectionCenter(ServerConfig config) throws RemoteException {
		super(config.interconnectionCenterPort(), IInterconnectionCenter.class);
		this.config = config;
	}

	@Override
	public void start() throws RemoteException {
		super.start();

		Trace.logEvent("JVM startup options: " + ManagementFactory.getRuntimeMXBean().getInputArguments().toString() + "\n\t" + RequestDispatcher.getMemoryUsage());
	}

	@Override
	public synchronized void register(IApplicationServer server) throws RemoteException {
		String[] domains = server.domains();
		addServer(new ServerInfo(server, domains));
	}
	
	@Override
	public synchronized void unregister(IApplicationServer server) throws RemoteException {
		removeServer(server);
	}

	@Override
	public IApplicationServer connect(String domain) throws RemoteException {
		IServerInfo server = findServer(domain); 
		return server != null ? server.getServer() : null;
	}
	
	@Override
	protected File cacheFile() {
		return new File(config.getWorkingPath(), cache);
	}

	@Override
	protected long serialVersion() {
		return serialVersionUID;
	}
	
	private IServerInfo findServer(String domain) throws RemoteException {
		IServerInfo[] servers = this.getServers().toArray(new IServerInfo[0]);

		for(IServerInfo server : servers) {
			if(!ArrayUtils.contains(server.getDomains(), domain))
				continue;
			
			if(!server.isAlive()) {
				unregister(server.getServer());
				continue;
			}

			// меняем порядок, чтобы распределять запросы
			if(this.getServers().size() > 1) {
				this.getServers().remove(server);
				this.getServers().add(server);
			}

			return server;
		}

		return null;
	}
	
}
