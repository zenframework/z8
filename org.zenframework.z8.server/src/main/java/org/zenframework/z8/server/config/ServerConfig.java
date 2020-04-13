package org.zenframework.z8.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.engine.IWebServer;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.utils.StringUtils;

public class ServerConfig extends Properties {

	static private final long serialVersionUID = 3564936578688816088L;

	static private final String Z8SystemPrefix = "z8.";

	static private final String localhost = "localhost";

	static private ServerConfig instance;

	static final public String DefaultInstanceId = "Z8 Server";
	static final public String DefaultConfigurationFileName = "project.xml";

	static final private String InstanceId = "z8.instance.id";

	static final private String ApplicationServerHost = "application.server.host";
	static final private String ApplicationServerPort = "application.server.port";

	static final private String AuthorityCenterHost = "authority.center.host";
	static final private String AuthorityCenterPort = "authority.center.port";
	static final private String AuthorityCenterCache = "authority.center.cache";
	static final private String AuthorityCenterSessionTimeout = "authority.center.session.timeout";

	static final private String InterconnectionCenterHost = "interconnection.center.host";
	static final private String InterconnectionCenterPort = "interconnection.center.port";
	static final private String InterconnectionCenterCache = "interconnection.center.cache";

	static final private String WebServerPort = "web.server.port";
	static final private String WebServerHttpPort = "web.server.http.port";
	static final private String WebServerWebapp = "web.server.webapp";
	static final private String WebServerMappings = "web.server.content.map";
	static final private String WebServerUrlPatterns = "web.server.urlPatterns";

	static final private String WebServerServletConfigPrefix = "web.server.servlet.";
	
	static final private String WebServerUploadMax = "web.server.upload.max";
	static final private String WebClientDownloadMax = "web.client.download.max";
	static final private String WebClientHashPassword = "web.client.hashPassword";

	static final private String SchedulerEnabled = "scheduler.enabled";
	static final private String MaintenenceJobCron = "maintenance.job.cron";
	static final private String TransportJobCron = "transport.job.cron";
	static final private String TransportJobTreads = "transport.job.treads";
	static final private String TransportJobLogStackTrace = "transport.job.logStackTrace";

	static final private String ExchangeJobCron = "exchange.job.cron";
	static final private String ExchangeFolderIn = "exchange.folder.in";
	static final private String ExchangeFolderOut = "exchange.folder.out";
	static final private String ExchangeFolderErr = "exchange.folder.err";

	static final private String TraceSql = "trace.sql";
	static final private String TraceSqlConnections = "trace.sql.connections";

	static final public String TextExtensions = "file.converter.text";
	static final public String ImageExtensions = "file.converter.image";
	static final public String EmailExtensions = "file.converter.email";
	static final public String OfficeExtensions = "file.converter.office";

	static final private String OfficeHome = "office.home";

	static final private String LdapUrl = "ldap.url";
	static final private String LdapBindDn = "ldap.bind.dn";
	static final private String LdapBindPassword = "ldap.bind.password";
	static final private String LdapDefaultDomain = "ldap.default.domain";
	static final private String LdapUsersIgnore = "ldap.users.ignore";
	static final private String LdapUsersCreateOnSuccessfulLogin = "ldap.users.createOnSuccessfulLogin";

	static final private String FtsConfiguration = "fts.configuration";

	static private File workingPath;

	static private String instanceId;

	static private String applicationServerHost;
	static private int applicationServerPort;

	static private String authorityCenterHost;
	static private int authorityCenterPort;
	static private boolean authorityCenterCache;
	static private int authorityCenterSessionTimeout;

	static private String interconnectionCenterHost;
	static private int interconnectionCenterPort;
	static private boolean interconnectionCenterCache;

	static private int webServerPort;
	static private int webServerHttpPort;
	static private File webServerWebapp;
	static private String webServerMappings;
	static private String webServerUrlPatterns;

	static private Map<String, String> webServerServletParams;

	static private int webServerUploadMax;
	static private int webClientDownloadMax;
	static private boolean webClientHashPassword;

	static private boolean schedulerEnabled;

	static private String maintenanceJobCron;

	static private String transportJobCron;
	static private int transportJobTreads;
	static private boolean transportJobLogStackTrace;

	static private String exchangeJobCron;
	static private File exchangeFolderIn;
	static private File exchangeFolderOut;
	static private File exchangeFolderErr;

	static private boolean traceSql;
	static private boolean traceSqlConnections;

	static private String officeHome;

	static private String ldapUrl;
	static private String ldapBindDn;
	static private String ldapBindPassword;
	static private String ldapDefaultDomain;
	static private Collection<String> ldapUsersIgnore;
	static private boolean ldapUsersCreateOnSuccessfulLogin;

	static private String ftsConfiguration;

	static private Database database;

	static public String[] textExtensions; // "txt, xml"
	static public String[] imageExtensions; // "tif, tiff, jpg, jpeg, gif, png, bmp"
	static public String[] emailExtensions; // "eml, mime"
	static public String[] officeExtensions; // "doc, docx, xls, xlsx, ppt, pptx, odt, odp, ods, odf, odg, wpd, sxw, sxi, sxc, sxd, stw, vsd"

	static private IApplicationServer applicationServer;
	static private IAuthorityCenter authorityCenter;
	static private IInterconnectionCenter interconnectionCenter;
	static private IWebServer webServer;

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
		authorityCenterCache = getProperty(AuthorityCenterCache, true);
		authorityCenterSessionTimeout = getProperty(AuthorityCenterSessionTimeout, 24 * 60);

		interconnectionCenterHost = getHost(InterconnectionCenterHost, Rmi.localhost);
		interconnectionCenterPort = getProperty(InterconnectionCenterPort, 20000);
		interconnectionCenterCache = getProperty(InterconnectionCenterCache, true);

		webServerPort = getProperty(WebServerPort, 30000);
		webServerHttpPort = getProperty(WebServerHttpPort, 9080);

		webServerWebapp = new File(getProperty(WebServerWebapp, ".."));
		if (!webServerWebapp.isAbsolute())
			webServerWebapp = new File(workingPath, getProperty(WebServerWebapp, ".."));

		webServerMappings = getProperty(WebServerMappings);
		webServerUrlPatterns = getProperty(WebServerUrlPatterns);

		webServerServletParams = new HashMap<String, String>();
		for (String name : stringPropertyNames())
			if (name.startsWith(WebServerServletConfigPrefix))
				webServerServletParams.put(name.substring(WebServerServletConfigPrefix.length()), getProperty(name));

		webServerUploadMax = getProperty(WebServerUploadMax, 5);
		webClientDownloadMax = getProperty(WebClientDownloadMax, 1);
		webClientHashPassword = getProperty(WebClientHashPassword, true);

		traceSql = getProperty(TraceSql, false);
		traceSqlConnections = getProperty(TraceSqlConnections, false);

		schedulerEnabled = getProperty(SchedulerEnabled, true);
		maintenanceJobCron = getProperty(MaintenenceJobCron, "");
		transportJobCron = getProperty(TransportJobCron, "");
		transportJobTreads = getProperty(TransportJobTreads, 10);
		transportJobLogStackTrace = getProperty(TransportJobLogStackTrace, false);

		exchangeJobCron = getProperty(ExchangeJobCron, "");
		exchangeFolderIn = new File(workingPath, getProperty(ExchangeFolderIn, "exchange/in"));
		exchangeFolderOut = new File(workingPath, getProperty(ExchangeFolderOut, "exchange/out"));
		exchangeFolderErr = new File(workingPath, getProperty(ExchangeFolderErr, "exchange/err"));

		textExtensions = getProperty(TextExtensions, new String[] { "txt", "xml" });
		imageExtensions = getProperty(ImageExtensions, new String[] { "tif", "tiff", "jpg", "jpeg", "gif", "png", "bmp" });
		emailExtensions = getProperty(EmailExtensions, new String[] { "eml", "mime" });
		officeExtensions = getProperty(OfficeExtensions, new String[] { "doc", "docx", "rtf", "xls", "xlsx", "ppt", "pptx", "odt", "odp", "ods", "odf", "odg", "wpd", "sxw", "sxi", "sxc", "sxd", "stw", "vsd" });

		officeHome = getProperty(OfficeHome, "C:/Program Files (x86)/LibreOffice 4.0");

		ldapUrl = getProperty(LdapUrl, "");
		ldapBindDn = getProperty(LdapBindDn, "");
		ldapBindPassword = getProperty(LdapBindPassword, "");
		ldapDefaultDomain = getProperty(LdapDefaultDomain, "");
		ldapUsersIgnore = StringUtils.asList(getProperty(LdapUsersIgnore, "Admin"), "\\,");
		ldapUsersCreateOnSuccessfulLogin = Boolean.parseBoolean(getProperty(LdapUsersCreateOnSuccessfulLogin, "false"));

		ftsConfiguration = getProperty(FtsConfiguration, (String) null);

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
		return System.getProperty(Z8SystemPrefix + key, super.getProperty(key.toUpperCase()));
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return value != null && !value.isEmpty() ? value : defaultValue;
	}

	@Override
	public Set<String> stringPropertyNames() {
		Set<String> result = new HashSet<String>(super.stringPropertyNames());
		for (String name : System.getProperties().stringPropertyNames())
			if (name.startsWith(Z8SystemPrefix))
				result.add(name.substring(Z8SystemPrefix.length()));
		return result;
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

	static public boolean authorityCenterCache() {
		return authorityCenterCache;
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

	static public boolean interconnectionCenterCache() {
		return interconnectionCenterCache;
	}

	static public boolean traceSql() {
		return traceSql;
	}

	static public boolean traceSqlConnections() {
		return traceSqlConnections;
	}

	static public int webServerPort() {
		return webServerPort;
	}

	static public int webServerHttpPort() {
		return webServerHttpPort;
	}

	static public File webServerWebapp() {
		return webServerWebapp;
	}

	static public String webServerMappings() {
		return webServerMappings;
	}

	static public String webServerUrlPatterns() {
		return webServerUrlPatterns;
	}

	static public Map<String, String> webServerServletParams() {
		return webServerServletParams;
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

	static public boolean maintenanceJobEnabled() {
		return !maintenanceJobCron().isEmpty();
	}

	static public String maintenanceJobCron() {
		return maintenanceJobCron;
	}

	static public boolean transportJobEnabled() {
		return !transportJobCron().isEmpty();
	}

	static public int transportJobThreads() {
		return transportJobTreads;
	}

	static public String transportJobCron() {
		return transportJobCron;
	}

	static public boolean transportJobLogStackTrace() {
		return transportJobLogStackTrace;
	}

	static public boolean exchangeJobEnabled() {
		return !exchangeJobCron().isEmpty();
	}

	static public String exchangeJobCron() {
		return exchangeJobCron;
	}

	static public File exchangeFolderIn() {
		return exchangeFolderIn;
	}

	static public File exchangeFolderOut() {
		return exchangeFolderOut;
	}

	static public File exchangeFolderErr() {
		return exchangeFolderErr;
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

	static public boolean webClientHashPassword() {
		return webClientHashPassword;
	}

	static public String ldapUrl() {
		return ldapUrl;
	}
	
	static public String ldapBindDn() {
		return ldapBindDn;
	}
	
	static public String ldapBindPassword() {
		return ldapBindPassword;
	}
	
	static public String ldapDefaultDomain() {
		return ldapDefaultDomain;
	}

	static public Collection<String> ldapUsersIgnore() {
		return ldapUsersIgnore;
	}

	static public boolean ldapUsersCreateOnSuccessfulLogin() {
		return ldapUsersCreateOnSuccessfulLogin;
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

	static public boolean isLatestVersion() {
		return database().isLatestVersion();
	}

	static public IApplicationServer applicationServer() {
		if(applicationServer == null)
			applicationServer = Rmi.get(IApplicationServer.class, applicationServerHost(), applicationServerPort());
		return applicationServer;
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

	static public IWebServer webServer() {
		if(webServer == null)
			webServer = Rmi.get(IWebServer.class, Rmi.localhost, webServerPort());
		return webServer;
	}
}
