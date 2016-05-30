package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;

public class Session implements ISession {
	private static final long serialVersionUID = -6111053710062684661L;

	private String id;
	private IUser user;
	private ServerInfo serverInfo;

	private long lastAccessTime;

	public Session() {
		this("system", User.system());
	}

	public Session(String id, IUser user) {
		this.id = id;
		this.user = user;
		this.lastAccessTime = System.currentTimeMillis();
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public IUser user() {
		return user;
	}

	@Override
	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}

	@Override
	public synchronized long getLastAccessTime() {
		return lastAccessTime;
	}

	public synchronized void access() {
		lastAccessTime = System.currentTimeMillis();
		cleanLoginInfo();
	}

	private void cleanLoginInfo() {
		user.setSettings(null);
		user.components().clear();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		out.writeLong(serialVersionUID);

		RmiIO.writeString(out, id);

		out.writeObject(user);
		out.writeObject(serverInfo);
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = in.readLong();

		id = RmiIO.readString(in);

		user = (IUser)in.readObject();
		serverInfo = (ServerInfo)in.readObject();
	}
}
