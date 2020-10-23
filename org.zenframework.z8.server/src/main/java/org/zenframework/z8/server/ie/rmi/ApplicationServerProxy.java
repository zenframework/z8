package org.zenframework.z8.server.ie.rmi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;

import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.file;

public class ApplicationServerProxy implements IApplicationServer {
	private IApplicationServer server;

	public ApplicationServerProxy(IApplicationServer server) {
		this.server = server;
	}

	@Override
	public boolean has(Message message) throws RemoteException {
		return ServerConfig.interconnectionCenter().has(server, message);
	}

	@Override
	public boolean accept(Message data) throws RemoteException {
		return ServerConfig.interconnectionCenter().accept(server, data);
	}

	@Override
	public String id() throws RemoteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void start() throws RemoteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stop() throws RemoteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void probe() throws RemoteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public GNode processRequest(ISession session, GNode request) throws RemoteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public file download(ISession session, GNode request, file file) throws RemoteException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IUser user(String login, String password, boolean createIfNotExists) throws RemoteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IUser userLoad(String login, boolean createIfNotExist) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] domains() throws RemoteException {
		throw new UnsupportedOperationException();
	}
}
