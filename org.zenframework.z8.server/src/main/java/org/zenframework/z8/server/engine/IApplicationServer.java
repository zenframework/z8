package org.zenframework.z8.server.engine;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.xml.GNode;

import java.io.IOException;
import java.rmi.RemoteException;

public interface IApplicationServer extends IServer {

    public static final String Name = "z8-application-server";

    public boolean hasRegisteredOnAuthorityCenter() throws RemoteException;

    public GNode processRequest(ISession session, GNode request) throws RemoteException;

    public FileInfo download(String filePath) throws RemoteException;

    public FileInfo download(FileInfo fileInfo) throws IOException;

}
