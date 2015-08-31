package org.zenframework.z8.server.engine;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.zenframework.z8.server.utils.ErrorUtils;

public class Rmi {
    final static public String localhost = "localhost";
    final static public int defaultPort = Registry.REGISTRY_PORT;

    static public String url(String host, int port, String name) throws RemoteException {
        return "rmi://" + host + ":" + port + "/" + name;
    }

    static public IServer connect(String host, int port, String name) throws RemoteException {
        String url = url(host, port, name);

        try {
            return (IServer)Naming.lookup(url);
        }
        catch(Throwable e) {
            throw new RemoteException(ErrorUtils.getMessage(e), e);
        }
    }
}
