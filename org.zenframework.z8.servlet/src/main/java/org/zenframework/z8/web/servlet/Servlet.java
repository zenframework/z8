package org.zenframework.z8.web.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.auth.AuthorityCenterMain;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.config.SystemProperty;
import org.zenframework.z8.server.engine.ApplicationServerMain;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.engine.TransportServerMain;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.web.server.Adapter;
import org.zenframework.z8.web.server.ConverterAdapter;
import org.zenframework.z8.web.server.JsonAdapter;
import org.zenframework.z8.web.server.TrustedAuthAdapter;

public class Servlet extends HttpServlet {

	private static final long serialVersionUID = 6442937554115725675L;

	static private String ApplicationServer = ApplicationServerMain.class.getCanonicalName();
	static private String AuthorityCenter = AuthorityCenterMain.class.getCanonicalName();
	static private String TransportServer = TransportServerMain.class.getCanonicalName();

	static private String Start = "start";
	static private String Stop = "stop";

	static private IAuthorityCenter authorityCenter = null;
	static private ServerConfig config = null;
	static private Object lock = new Object();

	private final List<Adapter> adapters = new ArrayList<Adapter>();

	static public ServerConfig config() {
		if(config == null)
			config = new ServerConfig();
		return config;
	}

	static public IAuthorityCenter getAuthorityCenter() throws RemoteException {
		if(authorityCenter != null)
			return authorityCenter;
					
		synchronized(lock) {
			if(authorityCenter != null)
				return authorityCenter;

			ServerConfig config = config();
			return authorityCenter = (IAuthorityCenter) Rmi.connect(config.getAuthorityCenterHost(), config.getAuthorityCenterPort(), IAuthorityCenter.Name);
		}
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {

		ServletContext context = servletConfig.getServletContext();

		System.setProperty(SystemProperty.ConfigFilePath, context.getRealPath("WEB-INF"));

		ServerConfig config = config();
		
		try {
			if(config.webServerStartAuthorityCenter())
				startServer(AuthorityCenter, config);

			if(config.webServerStartApplicationServer())
				startServer(ApplicationServer, config);
		} catch(Throwable e) {
			try {
				Trace.logError(e);
			} catch(Throwable ex) {
			}

			destroy();
			throw new ServletException(e);
		}

		adapters.clear();
		adapters.add(new JsonAdapter(this));
		adapters.add(new TrustedAuthAdapter(this));
		adapters.add(new ConverterAdapter(this));

		for(Adapter adapter : adapters)
			adapter.start();

		super.init(servletConfig);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Adapter adapter = getAdapter(request);
		request.setCharacterEncoding(encoding.Default.toString());
		adapter.service(request, response);
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
		} catch(Exception e) {
			Trace.logError(e);
		}
	}

	@Override
	public void destroy() {
		if(config.webServerStartApplicationServer())
			stopServer(ApplicationServer, config);

		if(config.webServerStartAuthorityCenter())
			stopServer(AuthorityCenter, config);

		if(config.webServerStartTransportServer())
			stopServer(TransportServer, config);

		config = null;

		for(Adapter adapter : adapters)
			adapter.stop();

		super.destroy();
	}

	private Adapter getAdapter(HttpServletRequest request) {
		for(Adapter adapter : adapters) {
			if(adapter.canHandleRequest(request))
				return adapter;
		}
		return null;
	}

	public String getServletPath() {
		return getServletContext().getRealPath("WEB-INF");
	}
}
