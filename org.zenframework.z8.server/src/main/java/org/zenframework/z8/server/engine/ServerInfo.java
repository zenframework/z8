package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;

import org.zenframework.z8.server.types.datespan;

public class ServerInfo implements IServerInfo {

	private static final long serialVersionUID = 5011706173964296365L;
	private static final long TenMinutes = 10 * datespan.TicksPerMinute;
	private static final long ThreeDays = 3 * datespan.TicksPerDay;

	private IApplicationServer server;
	private String id;
	private String[] domains;

	private long firstFailure = 0;
	private long lastChecked = 0;
	
	public ServerInfo() {
	}

	public ServerInfo(IApplicationServer server, String id) {
		this.server = server;
		this.id = id;
	}

	public ServerInfo(IApplicationServer server, String[] domains) {
		this.server = server;
		this.domains = domains;
	}

	@Override
	public Proxy getProxy() {
		return (Proxy)getProxy(server);
	}

	@Override
	public IApplicationServer getServer() {
		return server;
	}
	
	@Override
	public void setServer(IApplicationServer server) {
		this.server = server;
		lastChecked = 0;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String[] getDomains() {
		return domains;
	}

	@Override
	public void setDomains(String[] domains) {
		this.domains = domains;
	}

	@Override
	public boolean isAlive() throws RemoteException {
		if(lastChecked != 0 && System.currentTimeMillis() - lastChecked < TenMinutes)
			return false;
		
		try {
			server.probe();
			firstFailure = lastChecked = 0;
			return true;
		} catch(NoSuchObjectException e) {
		} catch(ConnectException e) {
		}
		
		long time = System.currentTimeMillis();
		if(lastChecked == 0)
			firstFailure = time;
		
		lastChecked = time;
		return false;
	}

	@Override
	public boolean isDead() throws RemoteException {
		return firstFailure != 0 && System.currentTimeMillis() - firstFailure > ThreeDays;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		RmiIO.writeLong(out, serialVersionUID);

		RmiIO.writeString(out, id);
		out.writeObject(domains);
		out.writeObject(server);

		RmiIO.writeLong(out, firstFailure);
		RmiIO.writeLong(out, lastChecked);
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		id = RmiIO.readString(in);
		domains = (String[])in.readObject();
		server = (IApplicationServer)in.readObject();

		firstFailure = RmiIO.readLong(in);
		lastChecked = RmiIO.readLong(in);
	}

	public String toString() {
		IApplicationServer proxy = getProxy(server);
		return "[id: " + id + ", " + (proxy != null ? proxy.toString() : "") + "]";
	}
	
	static private IApplicationServer getProxy(IApplicationServer server) {
		if(server instanceof RmiServer)
			return (IApplicationServer)((RmiServer)server).proxy();
		return server;
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object)
			return true;
		
		IApplicationServer server1 = getProxy(server);
		IApplicationServer server2 = null;
		
		if(object instanceof ServerInfo)
			server2 = getProxy(((ServerInfo)object).getServer());
		
		if(object instanceof IApplicationServer)
			server2 = getProxy((IApplicationServer)object);

		return server1 == server2 || server1 != null && server1.equals(server2) || server2 != null && server2.equals(server1);
	}
}
