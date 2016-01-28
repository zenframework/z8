package org.zenframework.z8.server.engine;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.utils.ErrorUtils;

public abstract class RmiServer extends UnicastRemoteObject implements IServer {
    private static final long serialVersionUID = -1200219220297838398L;

    private int port;
    private String name;
    private InetAddress address;
    private String hostOS;

    protected RmiServer(int port, String name) throws RemoteException {
        super(port == 0 ? Registry.REGISTRY_PORT : port);

        this.port = port == 0 ? Registry.REGISTRY_PORT : port;
        this.name = name;
        hostOS = System.getProperty("os.name");

        try {
            address = InetAddress.getLocalHost();
        }
        catch(UnknownHostException e) {
            address = null;
            Trace.logError(e);
        }
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    @Override
    public String id() throws RemoteException {
        return null;
    }

    @Override
    public String netAddress() throws RemoteException {
        if(address == null) {
            return "unknown:" + port;
        }

        return address.getCanonicalHostName() + "[" + address.getHostAddress() + "]:" + port;
    }

    public String hostOS() throws RemoteException {
        return hostOS;
    }

    @Override
    public void start() throws RemoteException {
        String url = Rmi.url(Rmi.localhost, port, name);

        try {
            // Trying to start the RMI registry on the localhost.
            // It can already be running (in this case
            // LocateRegistry.createRegistry would fail with ExportException).
            LocateRegistry.createRegistry(port);
        }
        catch(ExportException e) {
            // Failed to start. Do nothing with it here.
            // If registry is already running it's ok.
        }

        try {
            Naming.bind(url, this);
        }
        catch(Exception e1) {
            throw new RemoteException(ErrorUtils.getMessage(e1), e1);
        }
    }

    @Override
    public void stop() throws RemoteException {
        String url = Rmi.url(Rmi.localhost, port, name);
        try {
            Naming.unbind(url);
            UnicastRemoteObject.unexportObject(this, true);
        }
        catch(Exception e) {
            throw new RemoteException(ErrorUtils.getMessage(e), e);
        }
    }
}
