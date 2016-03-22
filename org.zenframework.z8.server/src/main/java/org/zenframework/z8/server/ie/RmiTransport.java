package org.zenframework.z8.server.ie;

import java.rmi.RemoteException;

import org.zenframework.z8.server.engine.ITransportServer;
import org.zenframework.z8.server.engine.Rmi;

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
	public void send(Message message) throws TransportException {
		RmiAddress address = new RmiAddress(message.getAddress());
		try {
			ITransportServer server = (ITransportServer) Rmi.connect(address.host, address.port, ITransportServer.Name);
			server.sendMessage(context.getProperty(TransportContext.SelfAddressProperty), message);
		} catch (RemoteException e) {
			throw new TransportException("Can't send message to '" + message.getAddress());
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

	public static class RmiAddress {

		public final String host;
		public final int port;
		public final String id;

		public RmiAddress(String address) throws TransportException {
			try {
				int hostAndPort = address.indexOf(':');
				int portAndId = address.indexOf('/');
				host = address.substring(0, hostAndPort);
				port = Integer.parseInt(address.substring(hostAndPort + 1, portAndId));
				id = address.substring(portAndId + 1);
			} catch (Throwable e) {
				throw new TransportException("Can't parse RMI address '" + address + "'", e);
			}
		}

	}

}
