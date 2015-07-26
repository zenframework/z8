package org.zenframework.z8.web.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.auth.AuthorityCenterMain;
import org.zenframework.z8.server.config.AppServerConfig;
import org.zenframework.z8.server.config.AuthCenterConfig;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.config.SystemProperty;
import org.zenframework.z8.server.config.WebServerConfig;
import org.zenframework.z8.server.engine.ApplicationServerMain;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.security.Digest_utils;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.web.server.Adapter;
import org.zenframework.z8.web.server.JsonAdapter;
import org.zenframework.z8.web.server.ConverterAdapter;
import org.zenframework.z8.web.server.TrustedAuthAdapter;

public class Servlet extends HttpServlet {
    
    private static final long serialVersionUID = 6442937554115725675L;
    
    private static final Collection<String> IGNORE_EXCEPTIONS = Arrays.asList("org.apache.catalina.connector.ClientAbortException");

    static private String ApplicationServer = ApplicationServerMain.class.getCanonicalName();
    static private String AuthorityService = AuthorityCenterMain.class.getCanonicalName();

    static private String Start = "start";
    static private String Stop = "stop";

    private static IAuthorityCenter authorityCenter = null;

    private final List<Adapter> adapters = new ArrayList<Adapter>();

    private WebServerConfig webServerConfig = null;
    private AuthCenterConfig authorityCenterConfig = null;
    private AppServerConfig applicationServerConfig = null;

    static public IAuthorityCenter getAuthorityCenter() {
        return authorityCenter;
    }

    public WebServerConfig config() {
        return webServerConfig;
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        ServletContext context = servletConfig.getServletContext();

        System.setProperty(SystemProperty.ConfigFilePath, context.getRealPath("WEB-INF"));

        webServerConfig = new WebServerConfig();
        Digest_utils.initialize(webServerConfig);

        try {
            if (webServerConfig.doRunAllServers()) {
                if (!webServerConfig.dontRunAuthCenter()) {
                    authorityCenterConfig = new AuthCenterConfig();
                    startServer(AuthorityService, authorityCenterConfig);
                }
                if (!webServerConfig.dontRunAppServer()) {
                    applicationServerConfig = new AppServerConfig();
                    startServer(ApplicationServer, applicationServerConfig);
                }
            }

            authorityCenter = (IAuthorityCenter) Rmi.connect(webServerConfig.getAuthorityCenterHost(),
                    webServerConfig.getAuthorityCenterPort(), IAuthorityCenter.Name);
        } catch (Throwable e) {
            try {
                Trace.logError(e);
            } catch (Throwable ex) {}

            destroy();
            throw new ServletException(e);
        }

        // TODO Вынести в web.xml
        adapters.clear();
        adapters.add(new JsonAdapter(this));
        adapters.add(new TrustedAuthAdapter(this));
        adapters.add(new ConverterAdapter(this));
        
        for (Adapter adapter : adapters) {
            adapter.start();
        }

        super.init(servletConfig);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Adapter adapter = getAdapter(request);
        request.setCharacterEncoding(encoding.Default.toString());
        try {
            adapter.service(request, response);
        } catch(IOException e) {
            if (!IGNORE_EXCEPTIONS.contains(e.getClass().getCanonicalName())) {
                throw e;
            }
        }
    }

    public File getWebInfPath() {
        return new File(getServletContext().getRealPath("WEB-INF"));
    }

    private void startServer(String server, ServerConfig options) {
        call(server, Start, options);
    }

    private void stopServer(String server, ServerConfig options) {
        call(server, Stop, options);
    }

    private void call(String className, String methodName, ServerConfig options) {
        try {
            ClassLoader loader = getClass().getClassLoader();
            Class<? extends Object> cls = loader.loadClass(className);
            Method method = cls.getDeclaredMethod(methodName, ServerConfig.class);
            method.invoke(null, options);
        } catch (Exception e) {
            Trace.logError(e);
        }
    }

    @Override
    public void destroy() {
        if (webServerConfig.doRunAllServers()) {
            if (!webServerConfig.dontRunAppServer()) {
                stopServer(ApplicationServer, applicationServerConfig);
            }
            if (!webServerConfig.dontRunAuthCenter()) {
                stopServer(AuthorityService, authorityCenterConfig);
            }
        }

        webServerConfig = null;
        
        for (Adapter adapter : adapters) {
            adapter.stop();
        }

        super.destroy();
    }

    private Adapter getAdapter(HttpServletRequest request) {
        for (Adapter adapter : adapters) {
            if (adapter.canHandleRequest(request)) {
                return adapter;
            }
        }
        return null;
    }

    public String getServletPath() {
        return getServletContext().getRealPath("") + "\\WEB-INF";
    }
}
