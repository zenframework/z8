package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

import javax.xml.bind.JAXBException;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.ie.ExportMessages;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.ie.TransportException;

public class TransportServer extends RmiServer implements ITransportServer {

	private static final long serialVersionUID = 6031141331643514419L;

	private static TransportServer INSTANCE;

	private TransportServer(ServerConfig config) throws RemoteException {
		super(config.getAuthorityCenterPort(), ITransportServer.Name);
	}

	public static void start(ServerConfig config) throws RemoteException {
		if (INSTANCE == null) {
			INSTANCE = new TransportServer(config);
			INSTANCE.start();
		}
	}

	public static TransportServer get() {
		return INSTANCE;
	}

	@Override
	public void sendMessage(String sender, Message message) throws TransportException {
		message.setSender(sender);
		try {
			new ExportMessages.CLASS<ExportMessages>().get().addMessage(message, "rmi");
		} catch (JAXBException e) {
			throw new TransportException("Can't import message '" + message.getId() + "' from '" + sender + "'", e);
		}
	}

}
