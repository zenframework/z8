package org.zenframework.z8.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.zenframework.z8.server.types.guid;

public class ServerConfig extends Properties {

	private static final long serialVersionUID = 3564936578688816088L;

	public static final String ConfigurationFileName = "project.xml";

	public static final String RmiRegistryPortProperty = "rmi.registry.port";

	public static final String AuthorityCenterHostProperty = "authority.center.host";
	public static final String AuthorityCenterPortProperty = "authority.center.port";
	public static final String AuthorityCenterSessionTimeoutProperty = "authority.center.session.timeout";

	public static final String TransportRegistryHostProperty = "transport.registry.host";
	public static final String TransportRegistryPortProperty = "transport.registry.port";

	public static final String ApplicationServerIdProperty = "application.server.id";

	public static final String WebServerStartApplicationServerProperty = "web.server.start.application.server";
	public static final String WebServerStartAuthorityCenterProperty = "web.server.start.authority.center";
	public static final String WebServerStartTransportServiceProperty = "web.server.start.transport.service";
	public static final String WebServerStartTransportRegistryProperty = "web.server.start.transport.registry";
	public static final String WebServerFileSizeMaxProperty = "web.server.file.size.max";

	public static final String SchedulerEnabledProperty = "scheduler.enabled";

	public static final String TraceSqlProperty = "trace.sql";

	public static final int RegistryPortDefault = 7852;

	public static final String OfficeHomeProperty = "office.home";

	private final File configFile;

	private final int rmiRegistryPort;

	private final String authorityCenterHost;
	private final int authorityCenterPort;
	private final int authorityCenterSessionTimeout;

	private final String transportRegistryHost;
	private final int transportRegistryPort;

	private final String applicationServerId;

	private final boolean webServerStartApplicationServer;
	private final boolean webServerStartAuthorityCenter;
	private final boolean webServerStartTransportService;
	private final boolean webServerStartTransportRegistry;
	private final int webServerFileSizeMax;

	private final boolean schedulerEnabled;

	private final boolean traceSql;

	private final String officeHome;

	public ServerConfig(String configFilePath) {
		configFile = new File(configFilePath != null ? configFilePath : ConfigurationFileName);

		try {
			loadFromXML(new FileInputStream(configFile));
		} catch (Throwable e) {
			throw new RuntimeException();
		}

		applicationServerId = getProperty(ApplicationServerIdProperty, guid.create().toString());

		rmiRegistryPort = getProperty(RmiRegistryPortProperty, RegistryPortDefault);

		authorityCenterHost = getProperty(AuthorityCenterHostProperty, "");
		authorityCenterPort = getProperty(AuthorityCenterPortProperty, RegistryPortDefault);
		authorityCenterSessionTimeout = getProperty(AuthorityCenterSessionTimeoutProperty, 24 * 60);

		transportRegistryHost = getProperty(TransportRegistryHostProperty, "");
		transportRegistryPort = getProperty(TransportRegistryPortProperty, RegistryPortDefault);

		webServerStartApplicationServer = getProperty(WebServerStartApplicationServerProperty, true);
		webServerStartAuthorityCenter = getProperty(WebServerStartAuthorityCenterProperty, true);
		webServerStartTransportService = getProperty(WebServerStartTransportServiceProperty, true);
		webServerStartTransportRegistry = getProperty(WebServerStartTransportRegistryProperty, false);
		webServerFileSizeMax = getProperty(WebServerFileSizeMaxProperty, 5);

		traceSql = getProperty(TraceSqlProperty, false);

		schedulerEnabled = getProperty(SchedulerEnabledProperty, true);
		
		officeHome = getProperty(OfficeHomeProperty, "C:/Program Files (x86)/LibreOffice 4.0");
	}

	@Override
	public synchronized Object put(Object key, Object value) {
		String stringKey = (String) key;
		return super.put(stringKey.toUpperCase(), value);
	}

	@Override
	public String getProperty(String key) {
		return super.getProperty(key.toUpperCase());
	}

	@Override
	public final String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return value != null && !value.isEmpty() ? value : defaultValue;
	}

	public final boolean getProperty(String key, boolean defaultValue) {
		String value = getProperty(key);
		return value != null && !value.isEmpty() ? Boolean.parseBoolean(value) : defaultValue;
	}

	public final int getProperty(String key, int defaultValue) {
		try {
			return Integer.parseInt(getProperty(key));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public final File getConfigFile() {
		return configFile;
	}

	public final File getWorkingPath() {
		return configFile.getParentFile();
	}

	public final String getServerId() {
		return applicationServerId;
	}

	public int getRmiRegistryPort() {
		return rmiRegistryPort;
	}

	public final String getAuthorityCenterHost() {
		return authorityCenterHost;
	}

	public final int getAuthorityCenterPort() {
		return authorityCenterPort;
	}

	public final int getSessionTimeout() {
		return authorityCenterSessionTimeout;
	}

	public final String getTransportRegistryHost() {
		return transportRegistryHost;
	}

	public final int getTransportRegistryPort() {
		return transportRegistryPort;
	}

	public final boolean getTraceSql() {
		return traceSql;
	}

	public final boolean webServerStartApplicationServer() {
		return webServerStartApplicationServer;
	}

	public final boolean webServerStartAuthorityCenter() {
		return webServerStartAuthorityCenter;
	}

	public final boolean webServerStartTransportService() {
		return webServerStartTransportService;
	}

	public final boolean webServerStartTransportRegistry() {
		return webServerStartTransportRegistry;
	}

	public final int webServerFileSizeMax() {
		return webServerFileSizeMax;
	}

	public final boolean isSchedulerEnabled() {
		return schedulerEnabled;
	}

	public String getOfficeHome() {
		return officeHome;
	}

}
