package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

import org.zenframework.z8.server.security.IUser;

public interface IAuthorityCenter extends IServer {

    String Name = "z8-authority-center";

    int MaxLoginLength = 32;
    int MaxPasswordLength = 32;

    ISession login(String userName, String password) throws RemoteException;

    ISession getTrustedSession(String userName) throws RemoteException;

    ISession getServer(String sessionId) throws RemoteException;

    ISession getServer(String sessionId, String serverId) throws RemoteException;

    void register(IServer server) throws RemoteException;

    void unregister(IServer server) throws RemoteException;

    void save(IUser user) throws RemoteException;

}
