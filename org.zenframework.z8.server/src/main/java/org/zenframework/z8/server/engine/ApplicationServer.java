package org.zenframework.z8.server.engine;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.file.FileConverter;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.FilesFactory;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.file.InputOnlyFileItem;
import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.ie.TransportEngine;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.request.RequestProcessor;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.IOUtils;

public class ApplicationServer extends RmiServer implements IApplicationServer {

	private static final long serialVersionUID = 7035837292407422257L;

	private static final ThreadLocal<IRequest> currentRequest = new ThreadLocal<IRequest>();

	private static ServerConfig Config = null;
	private static String Id = null;

	private IAuthorityCenter authorityCenter = null;

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

		authorityCenter = Rmi.get(IAuthorityCenter.class, Config.getAuthorityCenterHost(), Config.getAuthorityCenterPort());
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
			if (fileInfo.path.get().startsWith("table/"))
				return downloadFromTable(fileInfo);
			else
				return downloadFromStorage(fileInfo);
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
		if (Config == null) {
			Config = config;
			Id = config.getServerId();
			new ApplicationServer().start();
			;
			Scheduler.start();
			FileConverter.startOfficeManager();
		}
	}

	public static String Id() {
		return Id;
	}

	public static ServerConfig config() {
		return Config;
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

	private FileInfo downloadFromStorage(FileInfo fileInfo) throws IOException {
		FileInfo result = Files.getFile(fileInfo);
		ConnectionManager.release();
		return result;
	}

	private static FileInfo downloadFromTable(FileInfo fileInfo) throws IOException {
		File path = new File(Folders.Base, fileInfo.path.get());
		if (FileInfo.isDefaultWrite()) {
			InputStream inputStream = !path.exists() ? getTableFieldInputStream(fileInfo) : new FileInputStream(path);
			if (inputStream == null)
				return null;
			fileInfo.file = FilesFactory.createFileItem(fileInfo.name.get());
			IOUtils.copy(inputStream, fileInfo.getOutputStream());
			return fileInfo;
		} else {
			if (!path.exists()) {
				InputStream inputStream = getTableFieldInputStream(fileInfo);
				if (inputStream == null)
					return null;
				IOUtils.copy(inputStream, path);
			}
			fileInfo.file = new InputOnlyFileItem(path, fileInfo.name.get());
			return fileInfo;
		}

	}

	private static InputStream getTableFieldInputStream(FileInfo fileInfo) throws IOException {
		String path[] = fileInfo.path.get().split("/");
		if (path.length != 4)
			throw new IOException("Incorrect path '" + fileInfo.path + "'");
		Query query = (Query) Loader.getInstance(path[1]);
		if (query.readRecord(new guid(path[2]))) {
			return new ByteArrayInputStream(query.getFieldByName(path[3]).get().toString().getBytes());
		} else {
			throw new IOException("Incorrect path '" + fileInfo.path + "'");
		}
	}

}
