package org.zenframework.z8.server.config;

import java.io.FileInputStream;
import java.util.Properties;

import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;

public class ServerConfig extends Properties {
    private static final long serialVersionUID = 3564936578688816088L;

    public static final String configurationFileName = "project.xml";

    public static final String AuthorityCenterHostProperty = "authority.center.host";
    public static final String AuthorityCenterPortProperty = "authority.center.port";

    public static final String ApplicationServerIdProperty = "application.server.id";
    public static final String ApplicationServerPortProperty = "application.server.port";

    public static final String WebServerStandaloneProperty = "web.server.standalone";

    public static final String SchedulerEnabledProperty = "scheduler.enabled";
    
    public static final String TraceSqlProperty = "trace.sql";

    
    private static String workingPath;

    private static String authorityCenterHost;
    private static int authorityCenterPort;
    
    private static String applicationServerId;
    private static int applicationServerPort;

    private static boolean webServerStandalone;
    
    private static boolean schedulerEnabled;

    private static boolean traceSql;

    public ServerConfig() {
        this(configurationFileName);
    }

    public ServerConfig(String fileProject) {
        if(workingPath != null)
            return;
        
        workingPath = System.getProperty(SystemProperty.ConfigFilePath);

        if(workingPath != null && workingPath.lastIndexOf(System.getProperty("file.separator")) != workingPath.length() - 1)
            workingPath += System.getProperty("file.separator");

        if(workingPath == null)
            workingPath = "";

        load(fileProject);
        init();
    }

    protected void init() {
        applicationServerId = getProperty(ApplicationServerIdProperty, guid.create().toString());

        authorityCenterHost = getProperty(AuthorityCenterHostProperty, Rmi.localhost);
        authorityCenterPort = getProperty(AuthorityCenterPortProperty, Rmi.randomPort());
        
        applicationServerPort = getProperty(ApplicationServerPortProperty, Rmi.randomPort());

        webServerStandalone = getProperty(WebServerStandaloneProperty, false);
        
        traceSql = getProperty(TraceSqlProperty, false);
        
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

    protected void load(String fileProject) {
        try {
            loadFromXML(new FileInputStream(workingPath + fileProject));
        }
        catch(Exception e) {
            Trace.logError(e);
            this.clear();
        }
    }

    public final String getWorkingPath() {
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

    public final int getApplicationServerPort() {
        return applicationServerPort;
    }

    public final boolean getTraceSql() {
        return traceSql;
    }

    public final boolean webServerStandalone() {
        return webServerStandalone;
    }

    public final boolean schedulerEnabled() {
        return schedulerEnabled;
    }
}
