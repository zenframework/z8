package org.zenframework.z8.server.engine;

import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.ie.TransportException;

public interface ITransportServer extends IServer {

    String Name = "z8-transport-server";

	void sendMessage(String sender, Message message) throws TransportException;

}
