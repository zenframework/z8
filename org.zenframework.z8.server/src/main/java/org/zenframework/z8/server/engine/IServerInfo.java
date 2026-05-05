package org.zenframework.z8.server.engine;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.Map;

public interface IServerInfo extends RmiSerializable, Serializable {
	Proxy getProxy();

	IApplicationServer getServer();
	String getId();
	String[] getDomains();

	String getWebAppUrl();
	String getDatabaseVersion();
	String getRuntimeVersion();
	String getGitHash();
	long getBuildTimestamp();

	Map<String, String> getSettings();
	String getSetting(String name) throws RemoteException;

	Map<String, String> getProperties();
	String getProperty(String name) throws RemoteException;

	boolean isAlive() throws RemoteException;	// temporary unavailable
	boolean isDead() throws RemoteException;	// dead
}
