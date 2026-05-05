package org.zenframework.z8.server.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.utils.ErrorUtils;
import org.zenframework.z8.server.utils.IOUtils;

abstract public class HubServer extends RmiServer implements IHubServer {
	private static final long serialVersionUID = -3444119932500940158L;

	private Collection<ServerInfo> servers = new ArrayList<ServerInfo>();

	protected HubServer(int port) throws RemoteException {
		super(port);
	}

	@Override
	public void start() throws RemoteException {
		super.start();

		restoreServers();
	}

	@Override
	public IServerInfo[] servers() throws RemoteException {
		return getServers();
	}

	protected ServerInfo[] getServers() {
		synchronized(this) {
			return servers.toArray(new ServerInfo[0]);
		}
	}

	private void add(ServerInfo server) {
		synchronized(this) {
			servers.add(server);
		}
	}

	private void remove(ServerInfo server) {
		synchronized(this) {
			servers.remove(server);
		}
	}

	protected void sendToBottom(ServerInfo server) {
		if(servers.size() > 1) {
			synchronized(this) {
				servers.remove(server);
				servers.add(server);
			}
		}
	}

	protected void addServer(ServerInfo server) {
		ServerInfo existing = findServer(server.getServer());

		if(existing != null)
			existing.setId(server.getId()).setServer(server.getServer()).setDomains(server.getDomains())
					.setSettings(server.getSettings()).setProperties(server.getProperties());
		else
			add(server);

		saveServers();
	}

	protected void removeServer(IApplicationServer server) {
		ServerInfo info = findServer(server);

		if(info != null)
			removeServer(info);
	}

	protected void removeServer(ServerInfo server) {
		remove(server);
		saveServers();
	}

	protected ServerInfo findServer(IApplicationServer server) {
		for(ServerInfo existing : getServers()) {
			if(existing.equals(server))
				return existing;
		}

		return null;
	}

	abstract protected File cacheFile();

	private void saveServers() {
		File cacheFile = cacheFile();

		if (cacheFile == null)
			return;

		ObjectOutputStream out = null;

		try {
			out = new ObjectOutputStream(new FileOutputStream(cacheFile));

			out.writeLong(serialVersionUID);

			synchronized(this) {
				out.writeObject(servers);
			}
		} catch(Throwable e) {
			Trace.logEvent(ErrorUtils.getMessage(e));
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	@SuppressWarnings("unchecked")
	private void restoreServers() {
		File file = cacheFile();

		if(file == null || !file.exists())
			return;

		ObjectInputStream in = null;

		try {
			in = new ObjectInputStream(new FileInputStream(file));

			if(serialVersionUID != in.readLong())
				return;

			synchronized(this) {
				servers = (Collection<ServerInfo>)in.readObject();
			}
		} catch(Throwable e) {
			Trace.logEvent(ErrorUtils.getMessage(e));
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	protected static ServerInfo newServerInfo(IApplicationServer server) throws RemoteException {
		ServerInfo serverInfo = new ServerInfo(server);

		try {
			serverInfo.setSettings(server.settings()).setProperties(server.properties());
		} catch (Throwable e) {
			// Old version support
			Trace.logError("Can't get application server '" + serverInfo.getId() + "' properties. The application server seems to be older version", e);
		}

		return serverInfo;
	}
}
