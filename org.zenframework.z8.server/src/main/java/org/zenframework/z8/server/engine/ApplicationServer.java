package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.FileInfoNotFoundException;
import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.ie.TransportEngine;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.request.RequestProcessor;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;

public class ApplicationServer extends RmiServer implements IApplicationServer {

	private static final long serialVersionUID = 7035837292407422257L;

	private static final ThreadLocal<IRequest> currentRequest = new ThreadLocal<IRequest>();

	private static ApplicationServer INSTANCE = null;

	private static String Id = null;

	private transient IAuthorityCenter authorityCenter = null;

	private ApplicationServer() throws RemoteException {
		super(IApplicationServer.class);
	}

	@Override
	public String id() {
		return Id;
	}

	@Override
	public void start() throws RemoteException {
		super.start();

		checkSchemaVersion();

		authorityCenter = Rmi.get(IAuthorityCenter.class, Z8Context.getConfig().getAuthorityCenterHost(), Z8Context
				.getConfig().getAuthorityCenterPort());
		authorityCenter.register(this);

		RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
		System.out.println("INFO: JVM startup options: " + mxBean.getInputArguments().toString());
	}

	@Override
	public void stop() {
		Scheduler.stop();
		TransportEngine.getInstance().stop();
		try {
			authorityCenter.unregister(this);
		} catch (RemoteException e) {
			Trace.logError(e);
		}
		try {
			super.stop();
		} catch (RemoteException e) {
			Trace.logError(e);
		}
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
	public FileInfo download(FileInfo fileInfo) throws IOException, FileInfoNotFoundException {
		try {
			return Files.instance().getFile(fileInfo);
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

	public static void start(ServerConfig config) throws RemoteException {
		if (INSTANCE == null) {
			Id = config.getServerId();
			INSTANCE = new ApplicationServer();
			INSTANCE.start();
			Scheduler.start();
		}
	}

	public static String Id() {
		return Id;
	}

	public static IRequest getRequest() {
		IRequest request = currentRequest.get();
		if (request == null)
			request = new Request(new Session());
		return request;
	}

	public static ISession getSession() {
		return getRequest().getSession();
	}

	public static IMonitor getMonitor() {
		return getRequest().getMonitor();
	}

	public static Database database() {
		return new Database(Z8Context.getConfig());
	}

	public static IUser getUser() {
		return getSession().user();
	}

	public static void setRequest(IRequest request) {
		if (request != null)
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

	private void checkSchemaVersion() {
		String version = Runtime.version();
		System.out.println("Runtime schema version: " + version);
	}

}
