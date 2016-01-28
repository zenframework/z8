package org.zenframework.z8.server.engine;

import java.io.Serializable;

public class ServerInfo implements Serializable {
    private static final long serialVersionUID = 5011706173964296365L;

    private IServer server;
    private String id;
    private String address;

    public ServerInfo(IServer server, String id, String netAddress) {
        this.server = server;
        this.id = id;
        this.address = netAddress;
    }

    public IServer getServer() {
        return server;
    }

    public IApplicationServer getAppServer() {
        return (IApplicationServer)server;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }
}
