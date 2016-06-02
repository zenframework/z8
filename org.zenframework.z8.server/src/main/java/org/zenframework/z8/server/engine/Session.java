package org.zenframework.z8.server.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

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

		ByteArrayOutputStream bytes = new ByteArrayOutputStream(32 * NumericUtils.Kilobyte);
		ObjectOutputStream objects = new ObjectOutputStream(bytes);

		RmiIO.writeString(objects, id);

		objects.writeObject(user);
		objects.writeObject(serverInfo);

		objects.close();

		RmiIO.writeBytes(out, IOUtils.zip(bytes.toByteArray()));
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = in.readLong();

		ByteArrayInputStream bytes = new ByteArrayInputStream(IOUtils.unzip(RmiIO.readBytes(in)));
		ObjectInputStream objects = new ObjectInputStream(bytes);

		id = RmiIO.readString(objects);

		user = (IUser)objects.readObject();
		serverInfo = (ServerInfo)objects.readObject();
		
		objects.close();
	}
}
