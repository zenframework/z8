package org.zenframework.z8.server.config;

import java.util.HashSet;
import java.util.Set;

import org.zenframework.z8.server.engine.Rmi;

public class AppServerConfig extends ServerConfig {
    private static final long serialVersionUID = 762707213714657319L;

    public static final String property_srvapp_port = "srvapp_port";
    public static final String property_support_services = "support_services";

    private int applicationServerPort;
    private Set<String> supportedServices;

    public AppServerConfig() {
        super();
    }

    @Override
    protected void init() {
        super.init();

        applicationServerPort = getProperty(property_srvapp_port, Rmi.defaultPort);

        supportedServices = new HashSet<String>();
        String support_services = getProperty(property_support_services, "");
        String[] supported_services = support_services.split("[,;:|]");
        for(String service : supported_services) {
            if(!service.isEmpty())
                supportedServices.add(service);
        }
    }

    public final int getApplicationServerPort() {
        return applicationServerPort;
    }

    public final void setApplicationServerPort(int applicationServerPort) {
        this.applicationServerPort = applicationServerPort;
    }

    public final String[] services() {
        return supportedServices.toArray(new String[supportedServices.size()]);
    }
}
