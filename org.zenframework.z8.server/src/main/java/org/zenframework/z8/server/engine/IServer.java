package org.zenframework.z8.server.engine;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote, Serializable {

    public void start() throws RemoteException;

    public void stop() throws RemoteException;

    public String id() throws RemoteException;

    public String getName() throws RemoteException;

    public String netAddress() throws RemoteException;
}
