package org.zenframework.z8.server.engine;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.ie.ExportMessages;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.logs.Trace;

public class TransportServer extends RmiServer implements ITransportServer {

	private static final long serialVersionUID = 6031141331643514419L;

	private static final Log LOG = LogFactory.getLog(TransportServer.class);

	private static TransportServer INSTANCE;

	private final Collection<String> registered = Collections.synchronizedCollection(new LinkedList<String>());

	private TransportServer(ServerConfig config) throws RemoteException {
		super(ITransportServer.class);
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
	public void start() throws RemoteException {
		super.start();
		Trace.logEvent("TS: transport server started at '" + getUrl() + "'");
	}

	@Override
	public void checkRegistration(String selfAddress, String transportCentralRegistry) throws RemoteException {
		if (!registered.contains(selfAddress)) {
			new Registrator(selfAddress, transportCentralRegistry).start();
		}
	}

	@Override
	public void sendMessage(Message message) throws RemoteException {
		try {
			new ExportMessages.CLASS<ExportMessages>().get().addMessage(message, getUrl());
		} catch (Throwable e) {
			throw new RemoteException("Can't import message '" + message.getId() + "' from '" + message.getSender() + "'", e);
		}
	}

	private static class Registrator extends Thread {

		final String address;
		final String transportCentralRegistry;

		Registrator(String address, String transportCentralRegistry) {
			super("TransportRegistrator-" + address);
			this.address = address;
			this.transportCentralRegistry = transportCentralRegistry;
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				URI transportRegistryUri = new URI(transportCentralRegistry);
				while (true) {
					try {
						Rmi.get(ITransportRegistry.class, transportRegistryUri).registerTransportServer(address,
								Rmi.getConfig().getRmiRegistryPort());
						break;
					} catch (Exception e) {
						LOG.debug("Can't register transport server for '" + address + "'. Retrying...", e);
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							LOG.error("Can't register transport server for '" + address + "'. Thread interrupted", e1);
							break;
						}
					}
				}
			} catch (URISyntaxException e) {
				LOG.error("Can't register address '" + address + "'", e);
			}
		}

	}

}
