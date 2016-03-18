package org.zenframework.z8.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.types.guid;

public class ServerConfig extends Properties {
	private static final long serialVersionUID = 3564936578688816088L;

	public static final String configurationFileName = "project.xml";

	public static final String AuthorityCenterHostProperty = "authority.center.host";
	public static final String AuthorityCenterPortProperty = "authority.center.port";
	public static final String AuthorityCenterSessionTimeoutProperty = "authority.center.session.timeout";

	public static final String ApplicationServerIdProperty = "application.server.id";
	public static final String ApplicationServerPortProperty = "application.server.port";

	public static final String WebServerStartApplicationServerProperty = "web.server.start.application.server";
	public static final String WebServerStartAuthorityCenterProperty = "web.server.start.authority.center";
	public static final String WebServerFileSizeMaxProperty = "web.server.file.size.max";
	
	public static final String SchedulerEnabledProperty = "scheduler.enabled";

	public static final String TraceSqlProperty = "trace.sql";
	public static final String FileConverterProperty = "file.converter";

	private static File workingPath;

	private static String authorityCenterHost;
	private static int authorityCenterPort;
	private static int authorityCenterSessionTimeout;

	private static String applicationServerId;
	private static int applicationServerPort;

	private static boolean webServerStartApplicationServer;
	private static boolean webServerStartAuthorityCenter;
	private static int webServerFileSizeMax;
	
	private static boolean schedulerEnabled;

	private static boolean traceSql;
	private static String fileConverter;

	public ServerConfig() {
		this(null);
	}

	public ServerConfig(String configFile) {
		if(configFile == null)
			configFile = configurationFileName;
		
		if(workingPath != null)
			return;

		String configFilePath = System.getProperty(SystemProperty.ConfigFilePath);
		String absolutePath = new File(configFilePath == null ? "" : configFilePath).getAbsolutePath();
		workingPath = new File(absolutePath);

		load(new File(workingPath, configFile));
		init();
	}

	protected void init() {
		applicationServerId = getProperty(ApplicationServerIdProperty, guid.create().toString());

		authorityCenterHost = getProperty(AuthorityCenterHostProperty, Rmi.localhost);
		authorityCenterPort = getProperty(AuthorityCenterPortProperty, Rmi.randomPort());
		authorityCenterSessionTimeout = getProperty(AuthorityCenterSessionTimeoutProperty, 24 * 60);

		applicationServerPort = getProperty(ApplicationServerPortProperty, Rmi.randomPort());

		webServerStartApplicationServer = getProperty(WebServerStartApplicationServerProperty, true);
		webServerStartAuthorityCenter = getProperty(WebServerStartAuthorityCenterProperty, true);
		webServerFileSizeMax = getProperty(WebServerFileSizeMaxProperty, 5);
		
		traceSql = getProperty(TraceSqlProperty, false);
		fileConverter = getProperty(FileConverterProperty, "");

		schedulerEnabled = getProperty(SchedulerEnabledProperty, true);
	}

	@Override
	public synchronized Object put(Object key, Object value) {
		String stringKey = (String)key;
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
		} catch(NumberFormatException e) {
			return defaultValue;
		}
	}

	protected void load(File config) {
		try {
			loadFromXML(new FileInputStream(config));
		} catch(Throwable e) {
			throw new RuntimeException();
		}
	}

	public final File getWorkingPath() {
		return workingPath;
	}

	public final String getServerId() {
		return applicationServerId;
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

	public final int getApplicationServerPort() {
		return applicationServerPort;
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

	public final int webServerFileSizeMax() {
		return webServerFileSizeMax;
	}

	public final boolean schedulerEnabled() {
		return schedulerEnabled;
	}

	public final String fileConverter() {
		return fileConverter;
	}
}
