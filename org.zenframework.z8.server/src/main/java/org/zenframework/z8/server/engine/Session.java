package org.zenframework.z8.server.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

public class Session implements RmiSerializable, Serializable {
	private static final long serialVersionUID = -6111053710062684661L;

	private String id;
	private User user;
	private ServerInfo serverInfo;

	private long lastAccessTime;

	public Session() {
	}

	public Session(String scheme) {
		this("system", User.system(scheme));
	}

	public Session(String id, User user) {
		this.id = id;
		setUser(user);
	}

	public Session(Session session) {
		id = session.getId();
		user = session.getUser();
	}

	public String getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
		lastAccessTime = System.currentTimeMillis();
	}

	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void touch() {
		lastAccessTime = System.currentTimeMillis();
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
		long version = RmiIO.readLong(in);

		ByteArrayInputStream bytes = new ByteArrayInputStream(IOUtils.unzip(RmiIO.readBytes(in)));
		ObjectInputStream objects = new ObjectInputStream(bytes);

		id = RmiIO.readString(objects);

		user = (User)objects.readObject();
		serverInfo = (ServerInfo)objects.readObject();

		objects.close();
	}
}
