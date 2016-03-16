package org.zenframework.z8.server.engine;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.file.FileInfo;
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
import org.zenframework.z8.server.types.guid;

public class ApplicationServer extends RmiServer implements IApplicationServer {
    private static final long serialVersionUID = 7035837292407422257L;

    static public String Id = guid.create().toString();

    private static ApplicationServer instance = null;

    private IAuthorityCenter authorityCenter = null;

    private ServerConfig config;

    static private ThreadLocal<IRequest> currentRequest = new ThreadLocal<IRequest>();

    static public ApplicationServer get() {
        return instance;
    }

    protected ApplicationServer(ServerConfig config) throws RemoteException {
        super(config.getApplicationServerPort(), IApplicationServer.Name);

        Id = config.getServerId();

        this.config = config;

        instance = this;
        instance.start();
    }

    static public IAuthorityCenter getAuthorityCenter() {
        return instance.authorityCenter;
    }

    static public ServerConfig config() {
        return instance != null ? instance.config : new ServerConfig();
    }

    static public IRequest getRequest() {
        IRequest request = currentRequest.get();

        if (request == null)
            request = new Request(new Session());

        return request;
    }

    static public ISession getSession() {
        return getRequest().getSession();
    }

    static public IMonitor getMonitor() {
        return getRequest().getMonitor();
    }

    static public Database database() {
        return new Database(config());
    }

    static public IUser getUser() {
        return getSession().user();
    }

    static public void setRequest(IRequest request) {
        if (request != null)
            currentRequest.set(request);
        else
            currentRequest.remove();
    }

    static public void disableEvents() {
        getRequest().disableEvents();
    }

    static public void enableEvents() {
        getRequest().enableEvents();
    }

    static public boolean events() {
        return getRequest().events();
    }

    @Override
    public String id() throws RemoteException {
        return Id;
    }

    static public File workingPath() {
        return new File(instance.config.getWorkingPath());
    }

    static public String naming() {
        try {
            return instance.netAddress();
        } catch (RemoteException e) {}
        return "not defined";
    }

    @Override
    public void start() throws RemoteException {

        super.start();

        checkSchemaVersion();
        
        authorityCenter = (IAuthorityCenter) Rmi.connect(config.getAuthorityCenterHost(), config.getAuthorityCenterPort(),
                IAuthorityCenter.Name);
        authorityCenter.register(this);

        RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
        System.out.println("INFO: JVM startup options: " + mxBean.getInputArguments().toString());

        Scheduler.start();
    }

    private void checkSchemaVersion() {
        String version = Runtime.version();
        System.out.println("Runtime schema version: " + version);
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
    
/*    @Override
    public FileInfo download(String filePath) throws RemoteException {
        File f = new File(Folders.Base, filePath);
        
        FileItem fileItem = FilesFactory.createFileItem(f.getName());

        try {
            FileInfo info = new FileInfo(fileItem, f.toString());
            IOUtils.copy(new FileInputStream(f), info.getOutputStream());
        	return info;
        } catch (IOException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }
*/
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

        GNode result = new GNode();

        IRequest request = new Request(node.getAttributes(), node.getFiles(), session);
        IResponse response = request.getResponse();

        setRequest(request);

        new RequestProcessor().processRequest(request, response);

        setRequest(null);

        result.set(response.getContent());

        return result;
    }
}
