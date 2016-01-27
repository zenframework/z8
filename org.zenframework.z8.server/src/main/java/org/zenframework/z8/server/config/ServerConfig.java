package org.zenframework.z8.server.config;

import java.io.FileInputStream;
import java.util.Properties;

import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;

abstract public class ServerConfig extends Properties {
    private static final long serialVersionUID = 3564936578688816088L;

    public static final String configurationFileName = "project.xml";

    public static final String DEFAULT_SEPARATOR = "/";

    public static final String ServerId = "serverId";

    public static final String AuthorityCenterHost = "authsrv_addr";
    public static final String AuthorityCenterPort = "authsrv_port";
    public static final String TraceSql = "TraceSql";

    private String workingPath;

    protected String serverId;
    protected String authorityCenterHost;
    protected int authorityCenterPort;
    
    protected boolean traceSql;

    protected ServerConfig() {
        this(configurationFileName);
    }

    public ServerConfig(String fileProject) {
        workingPath = System.getProperty(SystemProperty.ConfigFilePath);

        if(workingPath != null && workingPath.lastIndexOf(System.getProperty("file.separator")) != workingPath.length() - 1) {
            workingPath += System.getProperty("file.separator");
        }

        if(workingPath == null) {
            workingPath = "";
        }

        load(fileProject);
        init();
    }

    protected void init() {
        serverId = getProperty(ServerId, guid.create().toString());

        authorityCenterHost = getProperty(AuthorityCenterHost, Rmi.localhost);
        authorityCenterPort = getProperty(AuthorityCenterPort, Rmi.randomPort());
        traceSql = getProperty(TraceSql, false);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        String stringKey = (String)key;
        return super.put(stringKey.toUpperCase(), value);
    }

    public String getProperty(String key) {
        return super.getProperty(key.toUpperCase());
    }

    public final boolean getProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public final int getProperty(String key, int defaultValue) {
        String value = getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
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
        return serverId;
    }

    public final String getAuthorityCenterHost() {
        return authorityCenterHost;
    }

    public final void setAuthorityCenterHost(String authorityCenterHost) {
        this.authorityCenterHost = authorityCenterHost;
    }

    public final int getAuthorityCenterPort() {
        return authorityCenterPort;
    }

    public final void setAuthorityCenterPort(int authorityCenterPort) {
        this.authorityCenterPort = authorityCenterPort;
    }
    
    public final boolean getTraceSql() {
        return traceSql;
    }

}
