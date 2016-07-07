package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;

public class ServerInfo implements IServerInfo {

	private static final long serialVersionUID = 5011706173964296365L;

	private IApplicationServer server;
	private String id;
	private String[] domains;

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

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	public boolean isAlive() throws RemoteException {
		try {
			server.probe();
			return true;
		} catch(NoSuchObjectException e) {
			return false;
		} catch(ConnectException e) {
			return false;
		}
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		RmiIO.writeLong(out, serialVersionUID);

		RmiIO.writeString(out, id);
		out.writeObject(domains);
		out.writeObject(server);
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		id = RmiIO.readString(in);
		domains = (String[])in.readObject();
		server = (IApplicationServer)in.readObject();
	}

	public String toString() {
		return "[id: " + id + ", " + server.toString() + "]";
	}
}
