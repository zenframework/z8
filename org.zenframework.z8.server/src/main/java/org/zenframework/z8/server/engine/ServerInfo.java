package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.types.datespan;

public class ServerInfo implements IServerInfo {

	private static final long serialVersionUID = 5011706173964296365L;

	private static final long TenMinutes = 10 * datespan.TicksPerMinute;
	private static final long ThreeDays = 3 * datespan.TicksPerDay;

	private Map<String, String> settings = new HashMap<String, String>();
	private Map<String, String> properties = new HashMap<String, String>();

	private IApplicationServer server;
	private String id;
	private String[] domains;

	private long firstFailure = 0;
	private long lastChecked = 0;

	public ServerInfo() {}

	public ServerInfo(IApplicationServer server) throws RemoteException {
		this.server = server;
		this.id = server.id();
	}

	@Override
	public Proxy getProxy() {
		return (Proxy)getProxy(server);
	}

	@Override
	public IApplicationServer getServer() {
		return server;
	}

	public ServerInfo setServer(IApplicationServer server) {
		this.server = server;
		lastChecked = 0;
		return this;
	}

	@Override
	public String getId() {
		return id;
	}

	public ServerInfo setId(String id) {
		this.id = id;
		return this;
	}

	@Override
	public String[] getDomains() {
		return domains;
	}

	public ServerInfo setDomains(String[] domains) {
		this.domains = domains;
		return this;
	}

	@Override
	public String getWebAppUrl() {
		return settings.get(ApplicationServer.WebAppUrl);
	}

	@Override
	public String getDatabaseVersion() {
		return settings.get(ApplicationServer.DatabaseVersion);
	}

	@Override
	public String getRuntimeVersion() {
		return settings.get(ApplicationServer.RuntimeVersion);
	}

	@Override
	public String getSetting(String name) throws RemoteException {
		return settings.get(name);
	}

	@Override
	public Map<String, String> getSettings() {
		return properties;
	}

	public ServerInfo setSettings(Map<String, String> settings) {
		this.settings.clear();
		this.settings.putAll(settings);
		return this;
	}

	@Override
	public String getProperty(String name) throws RemoteException {
		return properties.get(name);
	}

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

	public ServerInfo setProperties(Map<String, String> properties) {
		this.properties.clear();
		this.properties.putAll(properties);
		return this;
	}

	@Override
	public boolean isAlive() throws RemoteException {
		if(lastChecked != 0 && System.currentTimeMillis() - lastChecked < TenMinutes)
			return false;

		try {
			server.probe();
			firstFailure = lastChecked = 0;
			return true;
		} catch(RemoteException e) {}

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
		out.writeObject(server);
		out.writeObject(domains);
		out.writeObject(settings);
		out.writeObject(properties);

		RmiIO.writeLong(out, firstFailure);
		RmiIO.writeLong(out, lastChecked);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		id = RmiIO.readString(in);
		server = (IApplicationServer) in.readObject();
		domains = (String[]) in.readObject();
		settings = (Map<String, String>) in.readObject();
		properties = (Map<String, String>) in.readObject();

		firstFailure = RmiIO.readLong(in);
		lastChecked = RmiIO.readLong(in);
	}

	@Override
	public String toString() {
		IApplicationServer proxy = getProxy(server);
		return "[id: " + id + ", " + (proxy != null ? proxy.toString() : "") + "]";
	}

	static public IApplicationServer getProxy(IApplicationServer server) {
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
