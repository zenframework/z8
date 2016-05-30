package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;
import java.util.List;

import org.zenframework.z8.server.ie.TransportRoute;

public interface ITransportCenter extends IServer {

	void registerTransportService(String receiver, int localRegistryPort) throws RemoteException;

	List<TransportRoute> getTransportRoutes(String receiver) throws RemoteException;

}
