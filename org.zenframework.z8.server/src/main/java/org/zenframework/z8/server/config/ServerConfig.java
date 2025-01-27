package org.zenframework.z8.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.engine.IWebServer;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.utils.StringUtils;

public class ServerConfig extends Properties {

	static private final long serialVersionUID = 3564936578688816088L;

	static private final String Z8SystemPrefix = "z8.";

	static private final String localhost = "localhost";

	static private ServerConfig instance;

	static final public String DefaultInstanceId = "Z8 Server";
	static final public String DefaultConfigurationFileName = "server.properties";

	static final private String InstanceId = "instance.id";

	static final private String Multitenancy = "application.multitenancy";

	static final private String Language = "application.language";
	static final private String DefaultLanguage = "ru";

	static final private String DatabaseSchema = "application.database.schema";
	static final private String DatabaseUser = "application.database.user";
	static final private String DatabasePassword = "application.database.password";
	static final private String DatabaseConnection = "application.database.connection";
	static final private String DatabaseDriver = "application.database.driver";
	static final private String DatabaseCharset = "application.database.charset";
	
	static final private String EmailLogin = "application.email.login";
	static final private String EmailPassword = "application.email.password";

	static final private String ApplicationServerHost = "application.server.host";
	static final private String ApplicationServerPort = "application.server.port";
	static final private String ApplicationServerRequestTimeout = "application.server.request.timeout";

	static final private String AuthorityCenterHost = "authority.center.host";
	static final private String AuthorityCenterPort = "authority.center.port";
	static final private String AuthorityCenterCache = "authority.center.cache";
	static final private String AuthorityCenterSessionTimeout = "authority.center.session.timeout";

	static final private String InterconnectionCenterHost = "interconnection.center.host";
	static final private String InterconnectionCenterPort = "interconnection.center.port";
	static final private String InterconnectionCenterCache = "interconnection.center.cache";

	static final private String WebServerPort = "web.server.port";
	static final private String WebServerHttpHost = "web.server.http.host";
	static final private String WebServerHttpPort = "web.server.http.port";
	static final private String WebServerWebapp = "web.server.webapp";
	static final private String WebServerMappings = "web.server.content.map";
	static final private String WebServerUrlPatterns = "web.server.urlPatterns";
	static final private String WebServerUploadMax = "web.server.upload.max";
	static final private String WebServerUploadMemThreshold = "web.server.upload.mem.threshold";
	static final private String WebServerUseContainerSession = "web.server.useContainerSession";

	static final private String WebServerGzipPaths = "web.server.gzip.paths";
	static final private String WebServerGzipMethods = "web.server.gzip.methods";
	static final private String WebServerGzipMimeTypes = "web.server.gzip.mimeTypes";

	static final private String WebServerServletConfigPrefix = "web.server.servlet.";

	static final private String WebClientDownloadMax = "web.client.download.max";
	static final private String WebClientHashPassword = "web.client.hashPassword";

	static final private String SchedulerEnabled = "scheduler.enabled";
	static final private String MaintenenceJobCron = "maintenance.job.cron";
	static final private String TransportJobCron = "transport.job.cron";
	static final private String TransportJobThreads = "transport.job.threads";
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
	static final private String OfficePort = "office.port";

	static final private String SpnegoDomainRealm = "spnego.domainRealm";
	static final private String SpnegoPropertiesPath = "spnego.propertiesPath";
	
	static final private String LdapCheckLdapLogin = "ldap.checkLdapLogin";
	static final private String LdapUrl = "ldap.url";
	static final private String LdapPrincipalName = "ldap.principalName";
	static final private String LdapCredentials = "ldap.credentials";
	static final private String LdapSearchBase = "ldap.searchBase";
	static final private String LdapSearchUserFilter = "ldap.searchUserFilter";
	static final private String LdapSearchGroupFilter = "ldap.searchGroupFilter";

	static final private String LdapUsersIgnore = "ldap.users.ignore";
	static final private String LdapUsersCreateOnSuccessfulLogin = "ldap.users.createOnSuccessfulLogin";

	static final private String FtsConfiguration = "fts.configuration";

	static final private String SecurityLogFile = "security.log.file";
	static final private String SecurityLogFormat = "security.log.format";

	static final private String StoragePath = "storage.path";
	static final private String StoragePreviewPath = "storage.preview.path";

	static final private String AuthFailsLimit = "auth.fails.limit";
	static final private String AuthFailsBanSeconds = "auth.fails.banSeconds";

	static private File workingPath;

	static private String language;

	static private String instanceId;
	static private boolean multitenancy;

	static private String databaseSchema;
	static private String databaseUser;
	static private String databasePassword;
	static private String databaseConnection;
	static private String databaseDriver;
	static private encoding databaseCharset;
	
	static private String emailLogin;
	static private String emailPassword;

	static private String applicationServerHost;
	static private int applicationServerPort;
	static private int applicationServerRequestTimeout;

	static private String authorityCenterHost;
	static private int authorityCenterPort;
	static private boolean authorityCenterCache;
	static private int authorityCenterSessionTimeout;

	static private String interconnectionCenterHost;
	static private int interconnectionCenterPort;
	static private boolean interconnectionCenterCache;

	static private int webServerPort;
	static private String webServerHttpHost;
	static private int webServerHttpPort;
	static private File webServerWebapp;
	static private String webServerMappings;
	static private String webServerUrlPatterns;
	static private int webServerUploadMax;
	static private int webServerUploadMemThreshold;
	static private boolean webServerUseContainerSession;

	static private String[] webServerGzipPaths;
	static private String[] webServerGzipMethods;
	static private String[] webServerGzipMimeTypes;

	static private Map<String, String> webServerServletParams;

	static private int webClientDownloadMax;
	static private boolean webClientHashPassword;

	static private boolean schedulerEnabled;

	static private String maintenanceJobCron;

	static private String transportJobCron;
	static private int transportJobThreads;
	static private boolean transportJobLogStackTrace;

	static private String exchangeJobCron;
	static private File exchangeFolderIn;
	static private File exchangeFolderOut;
	static private File exchangeFolderErr;

	static private boolean traceSql;
	static private boolean traceSqlConnections;

	static private String officeHome;
	static private int officePort;

	static private String spnegoDomainRealm;
	static private String spnegoPropertiesPath;
	static private boolean ldapCheckLdapLogin;
	static private String ldapUrl;
	static private String ldapPrincipalName;
	static private String ldapCredentials;
	static private String ldapSearchBase;
	static private String ldapSearchUserFilter;
	static private String ldapSearchGroupFilter;

	static private Collection<String> ldapUsersIgnore;
	static private boolean ldapUsersCreateOnSuccessfulLogin;

	static private String ftsConfiguration;

	static private File securityLogFile;
	static private String securityLogFormat;

	static private File storagePath;
	static private File storagePreviewPath;

	static private int authFailsLimit;
	static private int authFailsBanSeconds;

	static public String[] textExtensions; // "txt, xml"
	static public String[] imageExtensions; // "tif, tiff, jpg, jpeg, gif, png, bmp"
	static public String[] emailExtensions; // "eml, mime"
	static public String[] officeExtensions; // "doc, docx, xls, xlsx, ppt, pptx, odt, odp, ods, odf, odg, wpd, sxw, sxi, sxc, sxd, stw, vsd"

	static private IApplicationServer applicationServer;
	static private IAuthorityCenter authorityCenter;
	static private IInterconnectionCenter interconnectionCenter;
	static private IWebServer webServer;

	private ServerConfig() {}

	public static void load(String configFilePath) throws IOException {
		if(instance != null)
			return;

		instance = new ServerConfig();

		File configFile = new File(configFilePath != null ? configFilePath : DefaultConfigurationFileName);
		workingPath = configFile.getCanonicalFile().getParentFile();

		try {
			instance.load(new FileInputStream(configFile));
		} catch(Throwable e) {
/*
			throw new RuntimeException(e);
*/
/* >>>>>>>>>>>>>>>>> to remove */
			try {
				instance.loadFromXML(new FileInputStream(new File(workingPath, "project.xml")));
			} catch(Throwable e1) {
				throw new RuntimeException(e);
			}
/* <<<<<<<<<<<<<<<<< to remove */
		}

		language = instance.getProperty(Language, DefaultLanguage);

		instanceId = instance.getProperty(InstanceId, DefaultInstanceId);
		multitenancy = instance.getProperty(Multitenancy, false);

		databaseSchema = instance.getProperty(DatabaseSchema);
		databaseUser = instance.getProperty(DatabaseUser);
		databasePassword = instance.getProperty(DatabasePassword);
		databaseConnection = instance.getProperty(DatabaseConnection);
		databaseDriver = instance.getProperty(DatabaseDriver);
		databaseCharset = encoding.fromString(instance.getProperty(DatabaseCharset));

		emailLogin = instance.getProperty(EmailLogin, "noreply@doczilla.ru");
		emailPassword = instance.getProperty(EmailPassword);

		applicationServerHost = instance.getHost(ApplicationServerHost, Rmi.localhost);
		applicationServerPort = instance.getProperty(ApplicationServerPort, 15000);
		applicationServerRequestTimeout = instance.getProperty(ApplicationServerRequestTimeout, 10);

		authorityCenterHost = instance.getHost(AuthorityCenterHost, Rmi.localhost);
		authorityCenterPort = instance.getProperty(AuthorityCenterPort, 10000);
		authorityCenterCache = instance.getProperty(AuthorityCenterCache, false);
		authorityCenterSessionTimeout = instance.getProperty(AuthorityCenterSessionTimeout, 24 * 60);

		interconnectionCenterHost = instance.getHost(InterconnectionCenterHost, Rmi.localhost);
		interconnectionCenterPort = instance.getProperty(InterconnectionCenterPort, 20000);
		interconnectionCenterCache = instance.getProperty(InterconnectionCenterCache, false);

		webServerPort = instance.getProperty(WebServerPort, 25000);
		webServerHttpHost = instance.getProperty(WebServerHttpHost, "0.0.0.0");
		webServerHttpPort = instance.getProperty(WebServerHttpPort, 9080);
		webServerWebapp = instance.getFile(WebServerWebapp, "..");
		webServerMappings = instance.getProperty(WebServerMappings);
		webServerUrlPatterns = instance.getProperty(WebServerUrlPatterns);
		webServerUploadMax = instance.getProperty(WebServerUploadMax, 5);
		webServerUploadMemThreshold = instance.getProperty(WebServerUploadMemThreshold, 1);
		webServerUseContainerSession = instance.getProperty(WebServerUseContainerSession, false);

		webServerGzipPaths = instance.getProperty(WebServerGzipPaths, new String[] { "/*" });
		webServerGzipMethods = instance.getProperty(WebServerGzipMethods, new String[] { "GET", "POST" });
		webServerGzipMimeTypes = instance.getProperty(WebServerGzipMimeTypes, new String[] {
				"text/html", "text/plain", "text/xml", "text/css", "text/javascript", "text/json",
				"application/javascript", "application/json", "application/octet-stream",
				"application/x-javascript", "application/xml", "application/xml+xhtml", "image/svg+xml"
		});

		webServerServletParams = instance.filterParameters(WebServerServletConfigPrefix);

		webClientDownloadMax = instance.getProperty(WebClientDownloadMax, 1);
		webClientHashPassword = instance.getProperty(WebClientHashPassword, true);

		traceSql = instance.getProperty(TraceSql, false);
		traceSqlConnections = instance.getProperty(TraceSqlConnections, false);

		schedulerEnabled = instance.getProperty(SchedulerEnabled, true);
		maintenanceJobCron = instance.getProperty(MaintenenceJobCron, "");
		transportJobCron = instance.getProperty(TransportJobCron, "");
		transportJobThreads = instance.getProperty(TransportJobThreads, 10);
		transportJobLogStackTrace = instance.getProperty(TransportJobLogStackTrace, false);

		exchangeJobCron = instance.getProperty(ExchangeJobCron, "");
		exchangeFolderIn = instance.getFile(ExchangeFolderIn, "exchange/in");
		exchangeFolderOut = instance.getFile(ExchangeFolderOut, "exchange/out");
		exchangeFolderErr = instance.getFile(ExchangeFolderErr, "exchange/err");

		textExtensions = instance.getProperty(TextExtensions, new String[] { "txt", "xml" });
		imageExtensions = instance.getProperty(ImageExtensions, new String[] { "tif", "tiff", "jpg", "jpeg", "gif", "png", "bmp" });
		emailExtensions = instance.getProperty(EmailExtensions, new String[] { "eml", "mime" });
		officeExtensions = instance.getProperty(OfficeExtensions, new String[] { "doc", "docx", "rtf", "xls", "xlsx", "ppt", "pptx", "odt", "odp", "ods", "odf", "odg", "wpd", "sxw", "sxi", "sxc", "sxd", "stw", "vsd" });

		officeHome = instance.getProperty(OfficeHome, "C:/Program Files (x86)/LibreOffice 4.0");
		officePort = instance.getProperty(OfficePort, 8100);

		spnegoDomainRealm = instance.getProperty(SpnegoDomainRealm, "");
		spnegoPropertiesPath = instance.getProperty(SpnegoPropertiesPath, "");
		ldapCheckLdapLogin = instance.getProperty(LdapCheckLdapLogin, false);
		ldapUrl = instance.getProperty(LdapUrl, "");
		ldapPrincipalName = instance.getProperty(LdapPrincipalName, "");
		ldapCredentials = instance.getProperty(LdapCredentials, "");
		ldapSearchBase = instance.getProperty(LdapSearchBase, "");
		ldapSearchUserFilter = instance.getProperty(LdapSearchUserFilter, "");
		ldapSearchGroupFilter = instance.getProperty(LdapSearchGroupFilter, "");
		ldapUsersIgnore = instance.getList(LdapUsersIgnore, "Admin", "\\,");
		ldapUsersCreateOnSuccessfulLogin = instance.getProperty(LdapUsersCreateOnSuccessfulLogin, false);

		ftsConfiguration = instance.getProperty(FtsConfiguration, (String) null);

		securityLogFile = instance.getFile(SecurityLogFile, null);
		securityLogFormat = instance.getProperty(SecurityLogFormat, "[%1$tF %1$tT] User{%2$s %3$s} Object{%4$s} Action{%5$s} Params%6$s Success: %7$b - %8$s %n");

		storagePath = instance.getFile(StoragePath, "storage");
		storagePreviewPath = instance.getFile(StoragePreviewPath, "pdf.cache");

		authFailsLimit = instance.getProperty(AuthFailsLimit, 0);
		authFailsBanSeconds = instance.getProperty(AuthFailsBanSeconds, 0);
	}

	// ///////////////////////////////////////////////////////////////
	// Properties overrides

	@Override
	public String getProperty(String key) {
		return System.getProperty(Z8SystemPrefix + key, super.getProperty(key));
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return value != null && !value.isEmpty() ? value : defaultValue;
	}

	@Override
	public Set<String> stringPropertyNames() {
		Set<String> result = new HashSet<String>(super.stringPropertyNames());
		for (String name : System.getProperties().stringPropertyNames()) {
			if(name.startsWith(Z8SystemPrefix))
				result.add(name.substring(Z8SystemPrefix.length()));
		}
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

		for (int i = 0; i < values.length; i++)
			result[i] = values[i].trim();

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

	public File getFile(String key, String defaultValue) {
		String value = getProperty(key, defaultValue);

		if (value == null)
			return null;

		File file = new File(value);

		try {
			return file.isAbsolute() ? file : new File(workingPath, value).getCanonicalFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getList(String key, String defaultValue, String delimiter) {
		return StringUtils.asList(getProperty(key, defaultValue), delimiter);
	}

	public Map<String, String> getMap(String key, String defaultValue, String pattern) {
		String value = getProperty(key, defaultValue);

		if (value == null)
			return Collections.emptyMap();

		Map<String, String> map = new HashMap<String, String>();
		Matcher matcher = Pattern.compile(pattern).matcher(value);
		while (matcher.find())
			map.put(matcher.group("key"), matcher.group("value"));

		return Collections.unmodifiableMap(map);
	}

	public String getHost(String key, String defaultValue) {
		String host = getProperty(key, Rmi.localhost);
		return localhost.equals(host) ? Rmi.localhost : host;
	}

	private Map<String, String> filterParameters(String prefix) {
		Map<String, String> filtered = new HashMap<String, String>();
		for (String name : stringPropertyNames())
			if (name.startsWith(prefix))
				filtered.put(name.substring(prefix.length()), getProperty(name));
		return filtered;
	}

	static public Properties getEffectiveProperties() {
		Properties effective = new Properties(instance);
		for (Object keyObj : System.getProperties().keySet()) {
			String key = (String) keyObj;
			if (key.startsWith(Z8SystemPrefix))
				effective.setProperty(key, System.getProperty(key));
		}
		return effective;
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

	static public String language() {
		return language;
	}

	static public String instanceId() {
		return instanceId;
	}

	static public boolean isMultitenant() {
		return multitenancy;
	}

	static public String databaseSchema() {
		return databaseSchema;
	}

	static public String databaseUser() {
		return databaseUser;
	}
	
	static public String emailLogin() {
		return emailLogin;
	}
	
	static public String emailPassword() {
		return emailPassword;
	}

	static public String databasePassword() {
		return databasePassword;
	}

	static public String databaseConnection() {
		return databaseConnection;
	}

	static public String databaseDriver() {
		return databaseDriver;
	}

	static public encoding databaseCharset() {
		return databaseCharset;
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

	static public int applicationServerRequestTimeout() {
		return applicationServerRequestTimeout;
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

	static public String webServerHttpHost() {
		return webServerHttpHost;
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

	static public int webServerUploadMax() {
		return webServerUploadMax;
	}

	static public int webServerUploadMemThreshold() {
		return webServerUploadMemThreshold;
	}

	static public boolean webServerUseContainerSession() {
		return webServerUseContainerSession;
	}

	static public String[] webServerGzipPaths() {
		return webServerGzipPaths;
	}

	static public String[] webServerGzipMethods() {
		return webServerGzipMethods;
	}

	static public String[] webServerGzipMimeTypes() {
		return webServerGzipMimeTypes;
	}

	static public Map<String, String> webServerServletParams() {
		return webServerServletParams;
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
		return transportJobThreads;
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

	static public int officePort() {
		return officePort;
	}

	static public boolean webClientHashPassword() {
		return webClientHashPassword;
	}

	static public String domainRealm() {
		return spnegoDomainRealm;
	}

	static public String spnegoPropertiesPath() {
		return spnegoPropertiesPath;
	}

	static public boolean checkLdapLogin() {
		return ldapCheckLdapLogin;
	}

	static public String ldapUrl() {
		return ldapUrl;
	}

	static public String principalName() {
		return ldapPrincipalName;
	}

	static public String credentials() {
		return ldapCredentials;
	}

	static public String searchBase() {
		return ldapSearchBase;
	}
	static public String searchUserFilter() {
		return ldapSearchUserFilter;
	}
	static public String searchGroupFilter() {
		return ldapSearchGroupFilter;
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

	static public File securityLogFile() {
		return securityLogFile;
	}

	static public String securityLogFormat() {
		return securityLogFormat;
	}

	static public File storagePath() {
		return storagePath;
	}

	static public File storagePreviewPath() {
		return storagePreviewPath;
	}

	static public int authFailsLimit() {
		return authFailsLimit;
	}

	static public int authFailsBanSeconds() {
		return authFailsBanSeconds;
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
