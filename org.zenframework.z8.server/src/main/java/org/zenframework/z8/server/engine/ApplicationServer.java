package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.ie.rmi.TransportJob;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.request.RequestProcessor;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class ApplicationServer extends RmiServer implements IApplicationServer {

	static private final ThreadLocal<IRequest> currentRequest = new ThreadLocal<IRequest>();

	static public String id = guid.create().toString();

	static private ApplicationServer instance;

	static public IApplicationServer launch(ServerConfig config) throws RemoteException {
		if(instance == null) {
			instance = new ApplicationServer();
			instance.start();
		}
		return instance;	
	}

	static public IRequest getRequest() {
		IRequest request = currentRequest.get();
		if(request == null)
			request = new Request(new Session());
		return request;
	}

	static public ISession getSession() {
		return getRequest().getSession();
	}

	static public IMonitor getMonitor() {
		return getRequest().getMonitor();
	}

	public static IUser getUser() {
		return getSession().user();
	}

	public static void setRequest(IRequest request) {
		if(request != null)
			currentRequest.set(request);
		else
			currentRequest.remove();
	}

	public static void disableEvents() {
		getRequest().disableEvents();
	}

	public static void enableEvents() {
		getRequest().enableEvents();
	}

	public static boolean events() {
		return getRequest().events();
	}

	private ApplicationServer() throws RemoteException {
		super(ServerConfig.applicationServerPort());
	}

	@Override
	public String id() throws RemoteException {
		return id;
	}

	@Override
	public void start() throws RemoteException {
		super.start();

		checkSchemaVersion();

		enableTimeoutChecking(1 * datespan.TicksPerMinute);

		Scheduler.start();

		Trace.logEvent("JVM startup options: " + ManagementFactory.getRuntimeMXBean().getInputArguments().toString() + "\n\t" + RequestDispatcher.getMemoryUsage());
	}

	@Override
	public void stop() throws RemoteException {
		Scheduler.stop();
	
		unregister();
		
		super.stop();
	}

	@Override
	public String[] domains() {
		return Domains.newInstance().getNames().toArray(new String[0]);
	}

	@Override
	public IUser login(String login) {
		return User.load(login);
	}

	@Override
	public IUser login(String login, String password) {
		return User.load(login, password);
	}

	@Override
	public file download(file file) throws IOException {
		try {
			return Files.get(file);
		} finally {
			ConnectionManager.release();
		}
	}

	@Override
	public GNode processRequest(ISession session, GNode node) {
		IRequest request = new Request(node.getAttributes(), node.getFiles(), session);
		IResponse response = request.getResponse();

		setRequest(request);

		new RequestProcessor().processRequest(request, response);

		setRequest(null);

		return new GNode(response.getContent());
	}

	@Override
	public boolean accept(Object object) {
		return TransportJob.accept(object);
	}

	@Override
	public boolean hasFile(file file) throws RemoteException {
		return Files.newInstance().hasRecord(file.id);
	}
	
	@Override
	protected void timeoutCheck() {
		register();
	}
	
	private void register() {
		try {
			ServerConfig.authorityCenter().register(this);
		} catch(Throwable e) {
		}
	}
	
	private void unregister() {
		try {
			ServerConfig.authorityCenter().unregister(this);
		} catch(Throwable e) {
		}
	}

	private void checkSchemaVersion() {
		String version = Runtime.version();
		System.out.println("Runtime schema version: " + version);
	}
}
