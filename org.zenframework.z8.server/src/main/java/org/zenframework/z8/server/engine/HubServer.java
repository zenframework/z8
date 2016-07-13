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
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.utils.ErrorUtils;

abstract public class HubServer extends RmiServer implements IHubServer {
	private static final long serialVersionUID = -3444119932500940157L;

	private Collection<IServerInfo> innerServers = new ArrayList<IServerInfo>();
	private Collection<IServerInfo> servers = Collections.synchronizedCollection(innerServers);

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
		return getServers().toArray(new IServerInfo[0]);
	}
	
	protected Collection<IServerInfo> getServers() {
		return servers;
	}
	
	protected void addServer(IServerInfo server) {
		IServerInfo existing = findServer(server.getServer());
		
		if(existing != null)
			servers.remove(existing);
		
		servers.add(server);
		
		saveServers();
	}

	protected void removeServer(IApplicationServer server) {
		IServerInfo info = findServer(server);
		
		if(info != null)
			removeServer(info);
	}

	protected void removeServer(IServerInfo server) {
		servers.remove(server);
		saveServers();
	}

	protected IServerInfo findServer(IApplicationServer server) {
		for(IServerInfo existing : servers) {
			if(existing.equals(server))
				return existing;
		}
		
		return null;
	}
	
	abstract protected File cacheFile();
	
	private synchronized void saveServers() {
		try {
			OutputStream file = new FileOutputStream(cacheFile());
			ObjectOutputStream out = new ObjectOutputStream(file);

			out.writeLong(serialVersionUID);
			out.writeObject(innerServers);

			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(file);

		} catch(Throwable e) {
			Trace.logEvent(ErrorUtils.getMessage(e));
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized void restoreServers() {
		try {
			File file = cacheFile();

			if(!file.exists())
				return;

			InputStream fileIn = new FileInputStream(file);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);

			if(serialVersionUID == objectIn.readLong()) {
				innerServers = (Collection<IServerInfo>)objectIn.readObject();
				servers = Collections.synchronizedCollection(innerServers);
			}

			IOUtils.closeQuietly(objectIn);
			IOUtils.closeQuietly(fileIn);

		} catch(Throwable e) {
			Trace.logEvent(ErrorUtils.getMessage(e));
		}
	}
}
