package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

public interface ITransportRegistry extends IServer {

	void registerTransportServer(String address, int localRegistryPort) throws RemoteException;

	String getTransportServerAddress(String address) throws RemoteException;

}
