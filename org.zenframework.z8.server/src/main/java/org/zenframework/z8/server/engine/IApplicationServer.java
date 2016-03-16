package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.security.IUser;

public interface IApplicationServer extends IServer {

    public static final String Name = "z8-application-server";

    public GNode processRequest(ISession session, GNode request) throws RemoteException;

    public FileInfo download(FileInfo fileInfo) throws RemoteException;
    
    public IUser login(String login) throws RemoteException;
    public IUser login(String login, String password) throws RemoteException;

}
