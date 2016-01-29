package org.zenframework.z8.server.engine;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.utils.ErrorUtils;

public class Rmi {
    final static public String localhost = "localhost";
    final static public int defaultPort = Registry.REGISTRY_PORT;

    public static Map<String, IServer> servers = new HashMap<String, IServer>();
    
    static public String url(String host, int port, String name) throws RemoteException {
        return "rmi://" + host + ":" + port + "/" + name;
    }

    static public int randomPort() {
        long time = System.nanoTime() / 100;
        int port = (int)(time % 100000);
        
        if(port >= 65535)
            port = (int)(port * 65536.0f / 100000);
        
        return port;
    }
    
    static public void register(IServer server) throws RemoteException {
        servers.put(server.getName(), server);
    }
    
    static public void unregister(IServer server) throws RemoteException {
        servers.remove(server.getName());
    }

    static public IServer connect(String host, int port, String name) throws RemoteException {
        IServer server = servers.get(name);
        
        if(server != null)
            return server;

        String url = url(host, port, name);

        try {
            return (IServer)Naming.lookup(url);
        }
        catch(Throwable e) {
            throw new RemoteException(ErrorUtils.getMessage(e), e);
        }
    }
}
