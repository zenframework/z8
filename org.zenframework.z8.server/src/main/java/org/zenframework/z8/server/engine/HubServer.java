package org.zenframework.z8.server.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.utils.ErrorUtils;

abstract public class HubServer extends RmiServer implements IHubServer {
	private static final long serialVersionUID = -3444119932500940159L;

	private Collection<IServerInfo> servers = new ArrayList<IServerInfo>();

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

	protected IServerInfo[] getServers() {
		synchronized(this) {
			return servers.toArray(new IServerInfo[0]);
		}
	}

	private void add(IServerInfo server) {
		synchronized(this) {
			servers.add(server);
		}
	}

	private void remove(IServerInfo server) {
		synchronized(this) {
			servers.remove(server);
		}
	}

	protected void sendToBottom(IServerInfo server) {
		if(servers.size() > 1) {
			synchronized(this) {
				servers.remove(server);
				servers.add(server);
			}
		}
	}

	protected void addServer(IServerInfo server) {
		IServerInfo existing = findServer(server.getServer());

		if(existing != null) {
			existing.setId(server.getId());
			existing.setDomains(server.getDomains());
			existing.setServer(server.getServer());
		} else
			add(server);

		saveServers();
	}

	protected void removeServer(IApplicationServer server) {
		IServerInfo info = findServer(server);

		if(info != null)
			removeServer(info);
	}

	protected void removeServer(IServerInfo server) {
		remove(server);
		saveServers();
	}

	@SuppressWarnings("unlikely-arg-type")
	protected IServerInfo findServer(IApplicationServer server) {
		for(IServerInfo existing : getServers()) {
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

		try {
			OutputStream file = new FileOutputStream(cacheFile);
			ObjectOutputStream out = new ObjectOutputStream(file);

			out.writeLong(serialVersionUID);

			synchronized(this) {
				out.writeObject(servers);
			}

			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(file);

		} catch(Throwable e) {
			Trace.logEvent(ErrorUtils.getMessage(e));
		}
	}

	@SuppressWarnings("unchecked")
	private void restoreServers() {
		try {
			File file = cacheFile();

			if(file == null || !file.exists())
				return;

			InputStream fileIn = new FileInputStream(file);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);

			if(serialVersionUID == objectIn.readLong())
				servers = (Collection<IServerInfo>)objectIn.readObject();

			IOUtils.closeQuietly(objectIn);
			IOUtils.closeQuietly(fileIn);

		} catch(Throwable e) {
			Trace.logEvent(ErrorUtils.getMessage(e));
		}
	}
}
