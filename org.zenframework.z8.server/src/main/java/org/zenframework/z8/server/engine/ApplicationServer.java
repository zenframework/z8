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
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.ie.MessageAcceptor;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.request.RequestProcessor;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.LoginParameters;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class ApplicationServer extends RmiServer implements IApplicationServer {
	static private final ThreadLocal<IRequest> currentRequest = new ThreadLocal<IRequest>();

	static public final String id = guid.create().toString();

	static private ApplicationServer instance;

	static public IApplicationServer launch(ServerConfig config) throws RemoteException {
		if(instance == null) {
			instance = new ApplicationServer();
			instance.start();
		}
		return instance;	
	}

	static public IRequest getRequest() {
		return currentRequest.get();
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

	public static IDatabase getDatabase() {
		IRequest request = getRequest();
		return request == null && !ServerConfig.isMultitenant() ? Database.get(null) : request.getSession().user().database();
	}

	public static String getSchema() {
		return getDatabase().schema();
	}
	
	public static void setRequest(IRequest request) {
		if(request != null)
			currentRequest.set(request);
		else
			currentRequest.remove();
	}

	public static void setEventsLevel(EventsLevel level) {
		getRequest().setEventsLevel(level);
	}

	public static void restoreEventsLevel() {
		getRequest().restoreEventsLevel();
	}
	
	public static EventsLevel eventsLevel() {
		return getRequest().eventsLevel();
	}

	public static boolean eventsEnabled(EventsLevel level) {
		return getRequest().eventsEnabled(level);
	}

	public static boolean systemEventsEnabled() {
		return eventsEnabled(EventsLevel.SYSTEM);
	}

	public static boolean userEventsEnabled() {
		return eventsEnabled(EventsLevel.USER);
	}

	protected ApplicationServer() throws RemoteException {
		super(ServerConfig.applicationServerPort());
	}

	@Override
	public String id() throws RemoteException {
		return id;
	}

	@Override
	public void start() throws RemoteException {
		super.start();

		enableTimeoutChecking(1 * datespan.TicksPerMinute);

		Trace.logEvent("Application Server JVM startup options: " + ManagementFactory.getRuntimeMXBean().getInputArguments().toString() + "\n\t" + RequestDispatcher.getMemoryUsage());
	}

	@Override
	public void stop() throws RemoteException {
		Scheduler.destroy();

		unregister();

		super.stop();

		Trace.logEvent("Application Server stopped");
	}

	@Override
	public String[] domains() {
		if(ServerConfig.isMultitenant())
			throw new RuntimeException("Multidomain is incompatible with multitenacy ");

		try {
			setRequest(new Request(new Session(getSchema())));
			return Domains.newInstance().getAddresses().toArray(new String[0]);
		} finally {
			releaseConnections();
			setRequest(null);
		}
	}

	@Override
	public IUser user(LoginParameters loginParameters, String password) {
		setRequest(new Request(new Session(loginParameters != null ? loginParameters.getSchema() : null)));

		try {
			return User.load(loginParameters, password);
		} finally {
			Scheduler.start(getDatabase());

			releaseConnections();
			setRequest(null);
		}
	}

	@Override
	public IUser create(LoginParameters loginParameters) {
		setRequest(new Request(new Session(loginParameters != null ? loginParameters.getSchema() : null)));

		try {
			return User.create(loginParameters);
		} finally {
			Scheduler.start(getDatabase());

			releaseConnections();
			setRequest(null);
		}
	}
	
	@Override
	public IUser registerUser(LoginParameters loginParameters, String password, String requestHost) throws RemoteException {
		setRequest(new Request(new Session(loginParameters != null ? loginParameters.getSchema() : null)));

		try {
			return User.register(loginParameters, password, requestHost);
		} finally {
			Scheduler.start(getDatabase());

			releaseConnections();
			setRequest(null);
		}
	}
	
	@Override
	public IUser verifyUser(String verification, String schema, String requestHost) throws RemoteException {
		setRequest(new Request(new Session(schema)));

		try {
			return User.verify(verification, requestHost);
		} finally {
			Scheduler.start(getDatabase());

			releaseConnections();
			setRequest(null);
		}
	}
	
	@Override
	public IUser remindInit(String login, String schema, String requestHost) throws RemoteException {
		setRequest(new Request(new Session(schema)));

		try {
			return User.remindInit(login, requestHost);
		} finally {
			Scheduler.start(getDatabase());

			releaseConnections();
			setRequest(null);
		}
	}

	@Override
	public IUser remind(String verification, String schema, String requestHost) throws RemoteException {
		setRequest(new Request(new Session(schema)));

		try {
			return User.remind(verification, requestHost);
		} finally {
			Scheduler.start(getDatabase());

			releaseConnections();
			setRequest(null);
		}
	}
	
	@Override
	public IUser changeUserPassword(String verification, String password, String schema, String requestHost) throws RemoteException {
		setRequest(new Request(new Session(schema)));

		try {
			return User.changePassword(verification, password, requestHost);
		} finally {
			Scheduler.start(getDatabase());

			releaseConnections();
			setRequest(null);
		}
	}
	
	@Override
	public file download(ISession session, GNode node, file file) throws IOException {
		setRequest(new Request(node.getAttributes(), node.getFiles(), session));
		try {
			return Files.get(file);
		} finally {
			releaseConnections();
			setRequest(null);
		}
	}

	@Override
	public GNode processRequest(ISession session, GNode node) {
		IRequest request = new Request(node.getAttributes(), node.getFiles(), session);
		IResponse response = request.getResponse();

		setRequest(request);

		new RequestProcessor().processRequest(request, response);

		setRequest(null);

		return new GNode(response.getInputStream(), response.getContentType());
	}

	@Override
	public boolean has(Message message) throws RemoteException {
		return MessageAcceptor.has(message);
	}

	@Override
	public boolean accept(Message message) {
		return MessageAcceptor.accept(message);
	}

	@Override
	protected void timeoutCheck() {
		register();
		releaseConnections();
	}

	private void register() {
		try {
			ServerConfig.authorityCenter().register(this);
		} catch(Throwable e) {
			Trace.logError(e);
		}
	}

	private void unregister() {
		try {
			ServerConfig.authorityCenter().unregister(this);
		} catch(Throwable e) {
		}
	}

	private void releaseConnections() {
		try {
			ConnectionManager.release();
		} catch(Throwable e) {
			Trace.logError(e);
		}
	}
}
