package org.zenframework.z8.server.ie.rmi;

import java.io.IOException;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.exceptions.UnsupportedException;
import org.zenframework.z8.server.ie.BaseMessage;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.file;

public class ApplicationServerProxy implements IApplicationServer {
	private IApplicationServer server;
	
	public ApplicationServerProxy(IApplicationServer server) {
		this.server = server;
	}
	
	@Override
	public boolean has(BaseMessage message) throws RemoteException {
		return ServerConfig.interconnectionCenter().has(server, message);
	}

	@Override
	public boolean accept(BaseMessage data) throws RemoteException {
		return ServerConfig.interconnectionCenter().accept(server, data);
	}

	@Override
	public String id() throws RemoteException {
		throw new UnsupportedException();
	}

	@Override
	public void start() throws RemoteException {
		throw new UnsupportedException();
	}

	@Override
	public void stop() throws RemoteException {
		throw new UnsupportedException();
	}

	@Override
	public void probe() throws RemoteException {
		throw new UnsupportedException();
	}

	@Override
	public GNode processRequest(ISession session, GNode request) throws RemoteException {
		throw new UnsupportedException();
	}

	@Override
	public file download(file file) throws RemoteException, IOException {
		throw new UnsupportedException();
	}

	@Override
	public IUser login(String login) throws RemoteException {
		throw new UnsupportedException();
	}

	@Override
	public IUser login(String login, String password) throws RemoteException {
		throw new UnsupportedException();
	}

	@Override
	public String[] domains() throws RemoteException {
		throw new UnsupportedException();
	}
}
