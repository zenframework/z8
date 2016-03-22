package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

import org.zenframework.z8.server.ie.Message;

public interface ITransportServer extends IServer {

    String Name = "z8-transport-server";

	void sendMessage(String sender, Message message) throws RemoteException;

}
