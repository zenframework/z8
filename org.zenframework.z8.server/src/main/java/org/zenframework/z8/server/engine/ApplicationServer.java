package org.zenframework.z8.server.engine;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.file.FileConverter;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Properties;
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
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;

public class ApplicationServer extends RmiServer implements IApplicationServer {

	private static final long serialVersionUID = 7035837292407422257L;

	private static final ThreadLocal<IRequest> currentRequest = new ThreadLocal<IRequest>();

	private static ApplicationServer INSTANCE = null;

	private final String id;
	private final ServerConfig config;

	private IAuthorityCenter authorityCenter = null;

	private ApplicationServer(ServerConfig config) throws RemoteException {
		super(IApplicationServer.class);
		this.id = config.getServerId();
		this.config = config;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public void start() throws RemoteException {
		super.start();

		checkSchemaVersion();

		authorityCenter = Rmi.get(IAuthorityCenter.class, config.getAuthorityCenterHost(), config.getAuthorityCenterPort());
		authorityCenter.register(this);

		RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
		System.out.println("INFO: JVM startup options: " + mxBean.getInputArguments().toString());
	}

	@Override
	public void stop() throws RemoteException {
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
	public FileInfo download(FileInfo fileInfo) throws RemoteException {
		try {
			FileInfo result = Files.getFile(fileInfo);
			ConnectionManager.release();
			return result;
		} catch (IOException e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}

	@Override
	public GNode processRequest(ISession session, GNode node) throws RemoteException {
		IRequest request = new Request(node.getAttributes(), node.getFiles(), session);
		IResponse response = request.getResponse();

		setRequest(request);

		new RequestProcessor().processRequest(request, response);

		setRequest(null);

		return new GNode(response.getContent());
	}

	public static void start(ServerConfig config) throws RemoteException {
		if (INSTANCE == null) {
			INSTANCE = new ApplicationServer(config);
			INSTANCE.start();
			Scheduler.start();
			String officeHome = config.getOfficeHome();
			if (officeHome.isEmpty()) {
				officeHome = Properties.getProperty(ServerRuntime.LibreOfficeDirectoryProperty);
			}
			FileConverter.startOfficeManager(officeHome);
		}
	}

	public static ApplicationServer get() {
		return INSTANCE;
	}

	public static IAuthorityCenter getAuthorityCenter() {
		return INSTANCE.authorityCenter;
	}

	public static ServerConfig config() {
		return INSTANCE.config;
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
		return new Database(config());
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

	public static File workingPath() {
		return config().getWorkingPath();
	}

	private void checkSchemaVersion() {
		String version = Runtime.version();
		System.out.println("Runtime schema version: " + version);
	}

}
