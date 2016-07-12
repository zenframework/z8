package org.zenframework.z8.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.exceptions.AccessDeniedException;

public class ServerConfig extends Properties {

	static private final long serialVersionUID = 3564936578688816088L;

	static private ServerConfig instance;

	static private String DefaultInstanceId = "Z8 Server";
	static public String DefaultConfigurationFileName = "project.xml";

	static private String InstanceIdProperty = "z8.instance.id";

	static private String RmiEnabledProperty = "rmi.enabled";
	static private String RmiRegistryPortProperty = "rmi.registry.port";

	static private String ApplicationServerHostProperty = "application.server.host";
	static private String ApplicationServerPortProperty = "application.server.port";

	static private String AuthorityCenterHostProperty = "authority.center.host";
	static private String AuthorityCenterPortProperty = "authority.center.port";
	static private String AuthorityCenterSessionTimeoutProperty = "authority.center.session.timeout";

	static private String InterconnectionCenterHostProperty = "interconnection.center.host";
	static private String InterconnectionCenterPortProperty = "interconnection.center.port";

	static private String WebServerStartApplicationServerProperty = "web.server.start.application.server";
	static private String WebServerStartAuthorityCenterProperty = "web.server.start.authority.center";
	static private String WebServerStartInterconnectionCenterProperty = "web.server.start.interconnection.center";
	static private String WebServerFileSizeMaxProperty = "web.server.file.size.max";

	static private String SchedulerEnabledProperty = "scheduler.enabled";

	static private String TraceSqlProperty = "trace.sql";
	static private String TraceSqlConnectionsProperty = "trace.sql.connections";

	static public String TextExtensionsProperty = "file.converter.text";
	static public String ImageExtensionsProperty = "file.converter.image";
	static public String EmailExtensionsProperty = "file.converter.email";
	static public String OfficeExtensionsProperty = "file.converter.office";

	static private String OfficeHomeProperty = "office.home";

	static private File workingPath;

	static private String instanceId;

	static private boolean rmiEnabled;
	static private int rmiRegistryPort;

	static private String applicationServerHost;
	static private int applicationServerPort;

	static private String authorityCenterHost;
	static private int authorityCenterPort;
	static private int authorityCenterSessionTimeout;

	static private String interconnectionCenterHost;
	static private int interconnectionCenterPort;

	static private boolean webServerStartApplicationServer;
	static private boolean webServerStartAuthorityCenter;
	static private boolean webServerStartInterconnectionCenter;

	static private int webServerFileSizeMax;

	static private boolean schedulerEnabled;

	static private boolean traceSql;
	static private boolean traceSqlConnections;

	static private String officeHome;

	static private Database database;

	static public String[] textExtensions; // "txt, xml"
	static public String[] imageExtensions; // "tif, tiff, jpg, jpeg, gif, png, bmp"
	static public String[] emailExtensions; // "eml, mime"
	static public String[] officeExtensions; // "doc, docx, xls, xlsx, ppt, pptx, odt, odp, ods, odf, odg, wpd, sxw, sxi, sxc, sxd, stw, vsd"

	static private Object Lock = new Object();
	static private IAuthorityCenter authorityCenter;
	static private IInterconnectionCenter interconnectionCenter;

	public ServerConfig(String configFilePath) throws IOException {
		if(instance != null)
			return;

		File configFile = new File(configFilePath != null ? configFilePath : DefaultConfigurationFileName);
		workingPath = configFile.getCanonicalFile().getParentFile();

		try {
			loadFromXML(new FileInputStream(configFile));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}

		instanceId = getProperty(InstanceIdProperty, DefaultInstanceId);

		rmiEnabled = getProperty(RmiEnabledProperty, true);
		rmiRegistryPort = getProperty(RmiRegistryPortProperty, Rmi.defaultRegistryPort);

		applicationServerHost = getProperty(ApplicationServerHostProperty, Rmi.localhost);
		applicationServerPort = getProperty(ApplicationServerPortProperty, 15000);

		authorityCenterHost = getProperty(AuthorityCenterHostProperty, Rmi.localhost);
		authorityCenterPort = getProperty(AuthorityCenterPortProperty, 10000);
		authorityCenterSessionTimeout = getProperty(AuthorityCenterSessionTimeoutProperty, 24 * 60);

		interconnectionCenterHost = getProperty(InterconnectionCenterHostProperty, Rmi.localhost);
		interconnectionCenterPort = getProperty(InterconnectionCenterPortProperty, 20000);

		webServerStartApplicationServer = getProperty(WebServerStartApplicationServerProperty, true);
		webServerStartAuthorityCenter = getProperty(WebServerStartAuthorityCenterProperty, true);
		webServerStartInterconnectionCenter = getProperty(WebServerStartInterconnectionCenterProperty, false);

		webServerFileSizeMax = getProperty(WebServerFileSizeMaxProperty, 5);

		traceSql = getProperty(TraceSqlProperty, false);
		traceSqlConnections = getProperty(TraceSqlConnectionsProperty, false);

		schedulerEnabled = getProperty(SchedulerEnabledProperty, true);

		textExtensions = getProperty(TextExtensionsProperty, new String[] { "txt", "xml" });
		imageExtensions = getProperty(ImageExtensionsProperty, new String[] { "tif", "tiff", "jpg", "jpeg", "gif", "png", "bmp" });
		emailExtensions = getProperty(EmailExtensionsProperty, new String[] { "eml", "mime" });
		officeExtensions = getProperty(OfficeExtensionsProperty, new String[] { "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "odp", "ods", "odf", "odg", "wpd", "sxw", "sxi", "sxc", "sxd", "stw", "vsd" });

		officeHome = getProperty(OfficeHomeProperty, "C:/Program Files (x86)/LibreOffice 4.0");

		instance = this;
	}

	// ///////////////////////////////////////////////////////////////
	// Properties overrides

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
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return value != null && !value.isEmpty() ? value : defaultValue;
	}

	public boolean getProperty(String key, boolean defaultValue) {
		String value = getProperty(key);
		return value != null && !value.isEmpty() ? Boolean.parseBoolean(value) : defaultValue;
	}

	public int getProperty(String key, int defaultValue) {
		try {
			return Integer.parseInt(getProperty(key));
		} catch(NumberFormatException e) {
			return defaultValue;
		}
	}

	public String[] getProperty(String key, String[] defaultValue) {
		String value = getProperty(key);
		
		if(value == null || value.trim().isEmpty())
			return defaultValue;

		String[] values = value.split("\\,");
		
		String[] result = new String[values.length];
		
		for(int i = 0; i < values.length; i++)
			result[i] = values[i].trim().toLowerCase();
		
		return result;
	}

	public int[] getProperty(String key, int[] defaultValue) {
		String value = getProperty(key);
		
		if(value == null || value.trim().isEmpty())
			return defaultValue;

		String[] values = value.split("\\,");
		
		int[] result = new int[values.length];
		
		try {
			for(int i = 0; i < values.length; i++)
				result[i] = Integer.parseInt(values[i].trim());
		} catch(NumberFormatException e) {
			return defaultValue;
		}
		
		return result;
	}

	// Properties overrides
	// ///////////////////////////////////////////////////////////////

	// ///////////////////////////////////////////////////////////////
	// getters

	static public String get(String key) {
		return instance.getProperty(key);
	}

	static public String get(String key, String defaultValue) {
		return instance.getProperty(key, defaultValue);
	}

	static public boolean get(String key, boolean defaultValue) {
		return instance.getProperty(key, defaultValue);
	}

	static public int get(String key, int defaultValue) {
		return instance.getProperty(key, defaultValue);
	}

	// getters
	// ///////////////////////////////////////////////////////////////

	static public String instanceId() {
		return instanceId;
	}

	static public File workingPath() {
		return workingPath;
	}

	static public boolean rmiEnabled() {
		return rmiEnabled;
	}

	static public int rmiRegistryPort() {
		return rmiRegistryPort;
	}

	static public String applicationServerHost() {
		return applicationServerHost;
	}

	static public int applicationServerPort() {
		return applicationServerPort;
	}

	static public String authorityCenterHost() {
		return authorityCenterHost;
	}

	static public int authorityCenterPort() {
		return authorityCenterPort;
	}

	static public int sessionTimeout() {
		return authorityCenterSessionTimeout;
	}

	static public String interconnectionCenterHost() {
		return interconnectionCenterHost;
	}

	static public int interconnectionCenterPort() {
		return interconnectionCenterPort;
	}

	static public boolean traceSql() {
		return traceSql;
	}

	static public boolean traceSqlConnections() {
		return traceSqlConnections;
	}

	static public boolean webServerLaunchApplicationServer() {
		return webServerStartApplicationServer;
	}

	static public boolean webServerLaunchAuthorityCenter() {
		return webServerStartAuthorityCenter;
	}

	static public boolean webServerLaunchInterconnectionCenter() {
		return webServerStartInterconnectionCenter;
	}

	static public int webServerFileSizeMax() {
		return webServerFileSizeMax;
	}

	static public boolean isSchedulerEnabled() {
		return schedulerEnabled;
	}

	static public String[] textExtensions() {
		return textExtensions;
	}

	static public String[] imageExtensions() {
		return imageExtensions;
	}

	static public String[] emailExtensions() {
		return emailExtensions;
	}

	static public String[] officeExtensions() {
		return officeExtensions;
	}

	static public String officeHome() {
		return officeHome;
	}

	static public Database database() {
		if(database == null)
			database = new Database(instance);
		return database;
	}

	static public boolean isSystemInstalled() {
		return database().isSystemInstalled();
	}

	static public IAuthorityCenter authorityCenter() {
		if(authorityCenter != null)
			return authorityCenter;

		synchronized(Lock) {
			if(authorityCenter != null)
				return authorityCenter;

			try {
				return authorityCenter = (IAuthorityCenter)Rmi.get(IAuthorityCenter.class, authorityCenterHost(), authorityCenterPort());
			} catch(Throwable e) {
				throw new AccessDeniedException();
			}
		}
	}

	static public IInterconnectionCenter interconnectionCenter() {
		if(interconnectionCenter != null)
			return interconnectionCenter;

		synchronized(Lock) {
			if(interconnectionCenter != null)
				return interconnectionCenter;

			try {
				return interconnectionCenter = (IInterconnectionCenter)Rmi.get(IInterconnectionCenter.class, interconnectionCenterHost(), interconnectionCenterPort());
			} catch(Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	static public boolean hasInterconnectionCenter() {
		return interconnectionCenter != null;
	}

	static public void resetInterconnectionCenter() {
		interconnectionCenter = null;
	}

}
