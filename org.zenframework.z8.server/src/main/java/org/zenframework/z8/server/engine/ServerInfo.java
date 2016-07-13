package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

	private long firstChecked = 0;
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

	public IApplicationServer getServer() {
		return server;
	}

	public String getId() {
		return id;
	}

	public String[] getDomains() {
		return domains;
	}

	public boolean isAlive() throws RemoteException {
		if(lastChecked != 0 && System.currentTimeMillis() - lastChecked < TenMinutes)
			return false;
		
		try {
			server.probe();
			firstChecked = lastChecked = 0;
			return true;
		} catch(NoSuchObjectException e) {
		} catch(ConnectException e) {
		}
		
		lastChecked = System.currentTimeMillis();
		return false;
	}

	public boolean isDead() throws RemoteException {
		return firstChecked != 0 && System.currentTimeMillis() - firstChecked > ThreeDays;
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

		RmiIO.writeLong(out, firstChecked);
		RmiIO.writeLong(out, lastChecked);
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		id = RmiIO.readString(in);
		domains = (String[])in.readObject();
		server = (IApplicationServer)in.readObject();

		firstChecked = RmiIO.readLong(in);
		lastChecked = RmiIO.readLong(in);
	}

	public String toString() {
		return "[id: " + id + ", " + server.toString() + "]";
	}
}
