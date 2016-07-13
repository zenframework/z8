/*package org.zenframework.z8.server.engine;

import java.io.File;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.runtime.ServerRuntime;

public class Z8Context {
	private static final Object Lock = new Object();

	private static ServerConfig config;
	private static String instanceId;

	private static IAuthorityCenter AuthorityCenter;

	private Z8Context() {}

	static public void init(ServerConfig config) throws RemoteException, UnknownHostException {
		Z8Context.config = config;
		
		Rmi.init(config);
		
		Properties.addListener(new Properties.Listener() {
			@Override
			public void onPropertyChange(String key, String value) {
				if (ServerRuntime.InstanceIdProperty.equalsKey(key)) {
					instanceId = value;
				}
			}
		});
	}

	static public ServerConfig getConfig() {
		return config;
	}

	static public File getWorkingPath() {
		return config.getWorkingPath();
	}

	static public IAuthorityCenter getAuthorityCenter() {
		if (AuthorityCenter != null)
			return AuthorityCenter;

		synchronized (Lock) {
			if (AuthorityCenter != null)
				return AuthorityCenter;

			try {
				AuthorityCenter = Rmi.get(IAuthorityCenter.class, config.getAuthorityCenterHost(),
						config.getAuthorityCenterPort());
				return AuthorityCenter;
			} catch (Throwable e) {
				throw new AccessDeniedException();
			}
		}
	}

	static public String getInstanceId() {
		if (instanceId == null) {
			instanceId = Properties.getProperty(ServerRuntime.InstanceIdProperty);
		}
		return instanceId;
	}
}
*/