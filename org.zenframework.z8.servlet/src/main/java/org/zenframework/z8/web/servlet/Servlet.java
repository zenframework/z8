package org.zenframework.z8.web.servlet;

import java.io.File;
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

import org.zenframework.z8.auth.AuthorityCenter;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.ITransportCenter;
import org.zenframework.z8.server.engine.ITransportService;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.engine.TransportCenter;
import org.zenframework.z8.server.engine.TransportService;
import org.zenframework.z8.server.engine.Z8Context;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.web.server.Adapter;
import org.zenframework.z8.web.server.ConverterAdapter;
import org.zenframework.z8.web.server.JsonAdapter;
import org.zenframework.z8.web.server.TrustedAuthAdapter;

public class Servlet extends HttpServlet {

	private static final long serialVersionUID = 6442937554115725675L;

	static private final String ApplicationServerClass = ApplicationServer.class.getCanonicalName();
	static private final String AuthorityCenterClass = AuthorityCenter.class.getCanonicalName();
	static private final String TransportServiceClass = TransportService.class.getCanonicalName();
	static private final String TransportRegistryClass = TransportCenter.class.getCanonicalName();

	static private final String ApplicationServerName = Rmi.getName(IApplicationServer.class);
	static private final String AuthorityCenterName = Rmi.getName(IAuthorityCenter.class);
	static private final String TransportServiceName = Rmi.getName(ITransportService.class);
	static private final String TransportRegistryName = Rmi.getName(ITransportCenter.class);

	static private final String StartMethod = "start";

	private final List<Adapter> adapters = new ArrayList<Adapter>();
	private ServerConfig config;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		ServletContext context = getServletContext();

		config = new ServerConfig(context.getRealPath("WEB-INF" + File.separator
				+ ServerConfig.ConfigurationFileName));
		Properties.setServerConfig(config);

		try {
			Z8Context.init(config);
			if (config.webServerStartAuthorityCenter())
				startServer(AuthorityCenterClass, config);
			if (config.webServerStartApplicationServer())
				startServer(ApplicationServerClass, config);
			if (config.webServerStartTransportService())
				startServer(TransportServiceClass, config);
			if (config.webServerStartTransportCenter())
				startServer(TransportRegistryClass, config);
		} catch (Throwable e) {
			Trace.logError(e);
			destroy();
			throw new ServletException(e);
		}

		adapters.clear();
		adapters.add(new JsonAdapter(this));
		adapters.add(new TrustedAuthAdapter(this));
		adapters.add(new ConverterAdapter(this));

		for (Adapter adapter : adapters)
			adapter.start();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Adapter adapter = getAdapter(request);
		request.setCharacterEncoding(encoding.Default.toString());
		adapter.service(request, response);
	}

	private void startServer(String className, ServerConfig options) {
		try {
			ClassLoader loader = getClass().getClassLoader();
			Class<? extends Object> cls = loader.loadClass(className);
			Method method = cls.getDeclaredMethod(StartMethod, ServerConfig.class);
			method.invoke(null, options);
		} catch (Exception e) {
			Trace.logError("Can't start server", e);
		}
	}

	private void stopServer(String server, ServerConfig options) {
		try {
			Rmi.get(server).stop();
		} catch (RemoteException e) {
			Trace.logError("Can't stop server", e);
		}
	}

	@Override
	public void destroy() {
		if (config.webServerStartApplicationServer())
			stopServer(ApplicationServerName, config);
		if (config.webServerStartAuthorityCenter())
			stopServer(AuthorityCenterName, config);
		if (config.webServerStartTransportService())
			stopServer(TransportServiceName, config);
		if (config.webServerStartTransportCenter())
			stopServer(TransportRegistryName, config);

		config = null;

		for (Adapter adapter : adapters)
			adapter.stop();

		super.destroy();
	}

	private Adapter getAdapter(HttpServletRequest request) {
		for (Adapter adapter : adapters) {
			if (adapter.canHandleRequest(request))
				return adapter;
		}
		return null;
	}

	public String getServletPath() {
		return getServletContext().getRealPath("WEB-INF");
	}

}
