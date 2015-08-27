package org.zenframework.z8.server.engine;

import java.io.Serializable;

public class ServerInfo implements Serializable {
    private static final long serialVersionUID = 5011706173964296365L;

    private IServer server;
    private String[] services;
    private String id;
    private String address;

    public ServerInfo(IServer server, String[] services, String id, String netAddress) {
        this.server = server;
        this.services = services;
        this.id = id;
        this.address = netAddress;
    }

    public IServer getServer() {
        return server;
    }

    public IApplicationServer getAppServer() {
        return (IApplicationServer)server;
    }

    public boolean supportService(String serviceName) {
        for(String service : services)
            if(service.equalsIgnoreCase(serviceName))
                return true;
        return false;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }
}
