package org.zenframework.z8.server.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.rmi.RemoteException;

import org.apache.commons.fileupload.FileItem;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.FilesFactory;
import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.config.AppServerConfig;
import org.zenframework.z8.server.ie.TransportEngine;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.request.RequestProcessor;
import org.zenframework.z8.server.security.Digest_utils;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.IOUtils;

public class ApplicationServer extends RmiServer implements IApplicationServer {
    private static final long serialVersionUID = 7035837292407422257L;

    static public String Id = guid.create().toString();

    private static ApplicationServer instance = null;

    private IAuthorityCenter authorityCenter = null;

    private AppServerConfig config;

    static private ThreadLocal<IRequest> currentRequest = new ThreadLocal<IRequest>();

    static public ApplicationServer get() {
        return instance;
    }

    protected ApplicationServer(AppServerConfig config) throws RemoteException {
        super(config.getApplicationServerPort(), IApplicationServer.Name);

        Id = config.getServerId();

        this.config = config;

        Digest_utils.initialize(config);

        instance = this;
        instance.start();
    }

    static public IAuthorityCenter getAuthorityCenter() {
        return instance.authorityCenter;
    }

    static public AppServerConfig config() {
        return instance != null ? instance.config : new AppServerConfig();
    }

    static public Database defaultDatabase() {
        return new Database(config());
    }

    static public IRequest getRequest() {
        IRequest request = currentRequest.get();

        if (request == null) {
            request = new Request(new Session(defaultDatabase()));
        }
        return request;
    }

    static public ISession getSession() {
        return getRequest().getSession();
    }

    static public IMonitor getMonitor() {
        return getRequest().getMonitor();
    }

    static public Database database() {
        return getRequest().getSession().database();
    }

    static public Database setDatabase(Database database) {
        Database oldDatabase = database();
        database = database != null ? database : defaultDatabase();

        IRequest request = currentRequest.get();

        if (request != null) {
            request.getSession().setDatabase(database);
        }

        return oldDatabase;
    }

    static public boolean isDefaultDatabase() {
        return defaultDatabase().schema().equalsIgnoreCase(database().schema());
    }

    static public IUser getUser() {
        return getSession().user();
    }

    static public void setRequest(IRequest request) {
        if (request != null) {
            currentRequest.set(request);
        } else {
            currentRequest.remove();
        }
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

        authorityCenter = (IAuthorityCenter) Rmi.connect(config.getAuthorityCenterHost(), config.getAuthorityCenterPort(),
                IAuthorityCenter.Name);
        authorityCenter.register(this);

        // В новой версии birt-runtime 4.4.0 не нужно, и даже вредно!
        //System.setProperty(SystemProperty.BIRT_HOME, workingPath());

        RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
        System.out.println("INFO: JVM startup options: " + mxBean.getInputArguments().toString());

        Scheduler.start();
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
    public FileInfo download(String filePath) throws RemoteException {
        File f = new File(file.BaseFolder, filePath);
        FileItem fileItem = FilesFactory.createFileItem(f.getName());
        try {
            FileInfo info = new FileInfo(fileItem, f.toString());
            InputStream in = new FileInputStream(f);
            OutputStream out = info.getOutputStream();
            try {
                IOUtils.copy(in, out);
                return info;
            } finally {
                in.close();
                out.close();
            }
        } catch (IOException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public FileInfo download(FileInfo fileInfo) throws IOException {
        return Files.getFile(fileInfo);
    }

    @Override
    public GNode processRequest(ISession session, GNode node) throws RemoteException {

        GNode result = new GNode();

        IRequest request = new Request(node.getAttributes(), node.getFiles(), session);
        IResponse response = request.getResponse();

        setRequest(request);
        setDatabase(session.database());

        new RequestProcessor().processRequest(request, response);

        setRequest(null);

        result.set(response.getContent());

        return result;
    }
}
