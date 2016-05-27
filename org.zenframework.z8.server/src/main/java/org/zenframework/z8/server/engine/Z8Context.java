package org.zenframework.z8.server.engine;

import java.io.File;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.runtime.ServerRuntime;

public class Z8Context {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(Z8Context.class);

	private static final Object Lock = new Object();

	private static ServerConfig Config;
	private static String instanceId;

	private static IAuthorityCenter AuthorityCenter;

	private Z8Context() {}

	public static void init(ServerConfig config) throws RemoteException, UnknownHostException {
		if (Config != null)
			return;
		Config = config;
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

	public static ServerConfig getConfig() {
		return Config;
	}

	public static File getWorkingPath() {
		return Config.getWorkingPath();
	}

	public static IAuthorityCenter getAuthorityCenter() {
		if (AuthorityCenter != null)
			return AuthorityCenter;

		synchronized (Lock) {
			if (AuthorityCenter != null)
				return AuthorityCenter;

			try {
				AuthorityCenter = Rmi.get(IAuthorityCenter.class, Config.getAuthorityCenterHost(),
						Config.getAuthorityCenterPort());
				return AuthorityCenter;
			} catch (Throwable e) {
				throw new AccessDeniedException();
			}
		}
	}

	public static String getInstanceId() {
		if (instanceId == null) {
			instanceId = Properties.getProperty(ServerRuntime.InstanceIdProperty);
		}
		return instanceId;
	}

}
