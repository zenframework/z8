package org.zenframework.z8.server.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.zenframework.z8.server.security.IAccount;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

public class Session implements ISession {
	private static final long serialVersionUID = -6111053710062684661L;

	private String id;
	private IUser user;
	private IAccount account;
	private IServerInfo serverInfo;

	private long lastAccessTime;

	public Session() {
		this("system", User.system());
	}

	public Session(String id, IUser user) {
		this.id = id;
		setUser(user);
	}

	public Session(String id, IAccount account) {
		this.id = id;
		setAccount(account);
	}

	public Session(ISession session) {
		this.id = session.id();
		this.user = session.user();
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public IUser user() {
		return account != null ? account.user() : user;
	}

	@Override
	public IAccount account() {
		return account;
	}

	@Override
	public void setUser(IUser user) {
		this.user = user;
		lastAccessTime = System.currentTimeMillis();
	}

	@Override
	public void setAccount(IAccount account) {
		this.account = account;
		lastAccessTime = System.currentTimeMillis();
	}

	@Override
	public IServerInfo getServerInfo() {
		return serverInfo;
	}

	@Override
	public void setServerInfo(IServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}

	@Override
	public long getLastAccessTime() {
		return lastAccessTime;
	}

	@Override
	public void access() {
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
		objects.writeObject(account);
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

		user = (IUser)objects.readObject();
		account = (IAccount)objects.readObject();
		serverInfo = (ServerInfo)objects.readObject();

		objects.close();
	}
}
