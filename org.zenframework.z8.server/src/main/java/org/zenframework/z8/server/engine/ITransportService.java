package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.ie.Message;

public interface ITransportService extends IServer {

	void checkRegistration(String selfAddress) throws RemoteException;

	void sendMessage(Message message) throws RemoteException;

	FileInfo readFile(FileInfo fileInfo) throws RemoteException;

}
