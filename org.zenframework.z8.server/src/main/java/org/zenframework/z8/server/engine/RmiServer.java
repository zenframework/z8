package org.zenframework.z8.server.engine;

import java.lang.reflect.Proxy;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

import org.zenframework.z8.server.logs.Trace;

public abstract class RmiServer implements IServer, Remote {
	private TimeoutChecker timeoutChecker;
	private Proxy proxy;

	protected RmiServer(int port) throws RemoteException {
		export(port);
	}

	@Override
	public void probe() throws RemoteException {
	}

	@Override
	public void start() throws RemoteException {
		Rmi.register(this);
	}

	@Override
	public void stop() throws RemoteException {
		if(timeoutChecker != null)
			timeoutChecker.destroy();

		unexport();
		Rmi.unregister(this);
	}

	public Proxy proxy() {
		return proxy;
	}

	protected void enableTimeoutChecking(long timeout) {
		timeoutChecker = new TimeoutChecker(timeout, this, getClass().getSimpleName() + " Timeout Thread");
	}

	protected void timeoutCheck() {
	}

	private void export(int port) throws RemoteException {
		if(port != 0) {
			while(!safeExport(port))
				port++;
		}
	}

	private boolean safeExport(int port) throws RemoteException {
		try {
			proxy = (Proxy)UnicastRemoteObject.exportObject(this, port);
			return true;
		} catch(ExportException e) {
			return false;
		} catch(Throwable e) {
			Trace.logError(e);
			throw new RemoteException("", e);
		}
	}

	private void unexport() {
		try {
			UnicastRemoteObject.unexportObject(this, true);
		} catch(NoSuchObjectException e) {
			Trace.logError(e);
		}
	}
}

class TimeoutChecker extends Thread {
	private RmiServer server;
	private long timeout;

	public TimeoutChecker(long timeout, RmiServer server, String name) {
		super(name);

		this.server = server;
		this.timeout = timeout;

		start();
	}

	@Override
	public void run() {
		while(true) {
			try {
				server.timeoutCheck();

				if(Thread.interrupted())
					return;

				Thread.sleep(timeout);
			} catch(InterruptedException e) {
				return;
			}
		}
	}

	@Override
	public void destroy() {
		interrupt();
	}

}
