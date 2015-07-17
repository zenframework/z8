package org.zenframework.z8.server.config;

public class WebServerConfig extends ServerConfig {
    private static final long serialVersionUID = 762707213714657319L;

    public static final String property_runservers = "runservers";
    public static final String property_no_run_authservice = "no_run_authservice";
    public static final String property_no_run_appserver = "no_run_appserver";

    private boolean doRunAllServers;
    private boolean donotRunAuthCenter;
    private boolean donotRunAppServer;

    public WebServerConfig() {
        super();
    }

    @Override
    protected void init() {
        super.init();

        doRunAllServers = getProperty(property_runservers, false);
        donotRunAuthCenter = getProperty(property_no_run_authservice, false);
        donotRunAppServer = getProperty(property_no_run_appserver, false);
    }

    public final boolean doRunAllServers() {
        return doRunAllServers;
    }

    public final boolean dontRunAuthCenter() {
        return donotRunAuthCenter;
    }

    public final boolean dontRunAppServer() {
        return donotRunAppServer;
    }
}
