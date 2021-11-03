package org.zenframework.z8.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.engine.Rmi;

public class ServerConfig extends Properties {

	static private final long serialVersionUID = 3564936578688816088L;
	static private final String localhost = "localhost";

	static private ServerConfig instance;

	static public String DefaultInstanceId = "Z8 Server";
	static public String DefaultConfigurationFileName = "project.xml";

	static private String InstanceId = "z8.instance.id";

	static private String ApplicationServerHost = "application.server.host";
	static private String ApplicationServerPort = "application.server.port";

	static private String AuthorityCenterHost = "authority.center.host";
	static private String AuthorityCenterPort = "authority.center.port";
	static private String AuthorityCenterSessionTimeout = "authority.center.session.timeout";

	static private String InterconnectionCenterHost = "interconnection.center.host";
	static private String InterconnectionCenterPort = "interconnection.center.port";

	static private String WebServerStartApplicationServer = "web.server.start.application.server";
	static private String WebServerStartAuthorityCenter = "web.server.start.authority.center";
	static private String WebServerStartInterconnectionCenter = "web.server.start.interconnection.center";

	static private String WebServerUploadMax = "web.server.upload.max";
	static private String WebClientDownloadMax = "web.client.download.max";

	static private String SchedulerEnabled = "scheduler.enabled";
	
	static private String TransportJobRepeat = "transport.job.repeat";
	static private String TransportJobTreads = "transport.job.treads";
	static private String TransportJobIgnoreErrors = "transport.job.ignoreErrors";

	static private String TraceSql = "trace.sql";
	static private String TraceSqlConnections = "trace.sql.connections";

	static public String TextExtensions = "file.converter.text";
	static public String ImageExtensions = "file.converter.image";
	static public String EmailExtensions = "file.converter.email";
	static public String OfficeExtensions = "file.converter.office";

	static private String OfficeHome = "office.home";
	
	static final private String FtsConfiguration = "fts.configuration";

	static private File workingPath;

	static private String instanceId;

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

	static private int webServerUploadMax;
	static private int webClientDownloadMax;

	static private boolean schedulerEnabled;

	static private int transportJobRepeat;
	static private int transportJobTreads;
	static private boolean transportJobIgnoreErrors;

	static private boolean traceSql;
	static private boolean traceSqlConnections;

	static private String officeHome;

	static private Database database;
	
	static private String ftsConfiguration;

	static public String[] textExtensions; // "txt, xml"
	static public String[] imageExtensions; // "tif, tiff, jpg, jpeg, gif, png, bmp"
	static public String[] emailExtensions; // "eml, mime"
	static public String[] officeExtensions; // "doc, docx, rtf, xls, xlsx, ppt, pptx, odt, odp, ods, odf, odg, wpd, sxw, sxi, sxc, sxd, stw, vsd"

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

		instanceId = getProperty(InstanceId, DefaultInstanceId);

		applicationServerHost = getHost(ApplicationServerHost, Rmi.localhost);
		applicationServerPort = getProperty(ApplicationServerPort, 15000);

		authorityCenterHost = getHost(AuthorityCenterHost, Rmi.localhost);
		authorityCenterPort = getProperty(AuthorityCenterPort, 10000);
		authorityCenterSessionTimeout = getProperty(AuthorityCenterSessionTimeout, 24 * 60);

		interconnectionCenterHost = getHost(InterconnectionCenterHost, Rmi.localhost);
		interconnectionCenterPort = getProperty(InterconnectionCenterPort, 20000);

		webServerStartApplicationServer = getProperty(WebServerStartApplicationServer, true);
		webServerStartAuthorityCenter = getProperty(WebServerStartAuthorityCenter, true);
		webServerStartInterconnectionCenter = getProperty(WebServerStartInterconnectionCenter, false);

		webServerUploadMax = getProperty(WebServerUploadMax, 5);
		webClientDownloadMax = getProperty(WebClientDownloadMax, 1);

		traceSql = getProperty(TraceSql, false);
		traceSqlConnections = getProperty(TraceSqlConnections, false);

		schedulerEnabled = getProperty(SchedulerEnabled, true);

		transportJobRepeat = getProperty(TransportJobRepeat, 5 * 60);
		transportJobTreads = getProperty(TransportJobTreads, 10);
		transportJobIgnoreErrors = getProperty(TransportJobIgnoreErrors, false);

		textExtensions = getProperty(TextExtensions, new String[] { "txt", "xml" });
		imageExtensions = getProperty(ImageExtensions, new String[] { "tif", "tiff", "jpg", "jpeg", "gif", "png", "bmp" });
		emailExtensions = getProperty(EmailExtensions, new String[] { "eml", "mime" });
		officeExtensions = getProperty(OfficeExtensions, new String[] { "doc", "docx", "rtf", "xls", "xlsx", "ppt", "pptx", "odt", "odp", "ods", "odf", "odg", "wpd", "sxw", "sxi", "sxc", "sxd", "stw", "vsd" });

		officeHome = getProperty(OfficeHome, "C:/Program Files (x86)/LibreOffice 4.0");
		
		ftsConfiguration = getProperty(FtsConfiguration, "russian");

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

	public String getHost(String key, String defaultValue) {
		String host = getProperty(key, Rmi.localhost);
		return localhost.equals(host) ? Rmi.localhost : host;
	}

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

	static public String instanceId() {
		return instanceId;
	}

	static public File workingPath() {
		return workingPath;
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

	static public int webServerUploadMax() {
		return webServerUploadMax;
	}

	static public int webClientDownloadMax() {
		return webClientDownloadMax;
	}

	static public boolean isSchedulerEnabled() {
		return schedulerEnabled;
	}

	static public boolean transportJobEnabled() {
		return transportJobRepeat() != 0;
	}

	static public int transportJobThreads() {
		return transportJobTreads;
	}

	static public boolean transportJobIgnoreErrors() {
		return transportJobIgnoreErrors;
	}

	static public int transportJobRepeat() {
		return transportJobRepeat;
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
	
	static public String ftsConfiguration() {
		return ftsConfiguration;
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
		if(authorityCenter == null)
			authorityCenter = Rmi.get(IAuthorityCenter.class, authorityCenterHost(), authorityCenterPort());
		return authorityCenter;
	}

	static public IInterconnectionCenter interconnectionCenter() {
		if(interconnectionCenter == null)
			interconnectionCenter = Rmi.get(IInterconnectionCenter.class, interconnectionCenterHost(), interconnectionCenterPort());
		return interconnectionCenter;
	}
}
