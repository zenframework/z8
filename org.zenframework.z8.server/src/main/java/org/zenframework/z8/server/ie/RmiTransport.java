package org.zenframework.z8.server.ie;

import java.rmi.RemoteException;

import org.zenframework.z8.server.engine.ITransportServer;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.engine.RmiAddress;

public class RmiTransport extends AbstractTransport {

	public RmiTransport(TransportContext context) {
		super(context);
	}

	public static final String PROTOCOL = "rmi";

	@Override
	public void connect() throws TransportException {}

	@Override
	public void close() {}

	@Override
	public void send(Message message, String transportAddress) throws TransportException {
		RmiAddress address = new RmiAddress(transportAddress);
		try {
			ITransportServer server = (ITransportServer) Rmi.get(ITransportServer.class, address.host, address.port);
			server.sendMessage(message);
		} catch (RemoteException e) {
			throw new TransportException("Can't send message to '" + message.getAddress(), e);
		}
	}

	@Override
	public Message receive() throws TransportException {
		return null;
	}

	@Override
	public void commit() throws TransportException {}

	@Override
	public void rollback() throws TransportException {}

	@Override
	public String getProtocol() {
		return PROTOCOL;
	}

}
