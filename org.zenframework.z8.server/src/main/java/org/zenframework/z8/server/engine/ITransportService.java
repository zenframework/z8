package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

import org.zenframework.z8.server.ie.Message;

public interface ITransportService extends IServer {

	void checkRegistration(String selfAddress, String transportCentralRegistry) throws RemoteException;

	void sendMessage(Message message) throws RemoteException;

}
