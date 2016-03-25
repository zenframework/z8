package org.zenframework.z8.web.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
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
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.engine.TransportServer;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
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
	static private final String TransportServerClass = TransportServer.class.getCanonicalName();

	static private final String Start = "start";
	static private final String Stop = "stop";

	private final List<Adapter> adapters = new ArrayList<Adapter>();
	private ServerConfig config;
	private IAuthorityCenter authorityCenter;

	public ServerConfig getConfig() {
		return config;
	}

	public IAuthorityCenter getAuthorityCenter() {
		return authorityCenter;
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {

		ServletContext context = servletConfig.getServletContext();

		config = new ServerConfig(context.getRealPath("WEB-INF" + File.separator
				+ ServerConfig.ConfigurationFileName));
		Properties.setServerConfig(config);

		try {
			Rmi.init(config);
			if (config.webServerStartAuthorityCenter())
				startServer(AuthorityCenterClass, config);

			if (config.webServerStartApplicationServer())
				startServer(ApplicationServerClass, config);

			if (config.webServerStartTransportServer())
				startServer(TransportServerClass, config);
		} catch (Throwable e) {
			try {
				Trace.logError(e);
			} catch (Throwable ex) {}

			destroy();
			throw new ServletException(e);
		}

		try {
			authorityCenter = Rmi.get(IAuthorityCenter.class, config.getAuthorityCenterHost(),
					config.getAuthorityCenterPort());
			context.setAttribute(IAuthorityCenter.class.getSimpleName(), authorityCenter);
		} catch (Throwable e) {
			throw new AccessDeniedException();
		}

		adapters.clear();
		adapters.add(new JsonAdapter(this));
		adapters.add(new TrustedAuthAdapter(this));
		adapters.add(new ConverterAdapter(this));

		for (Adapter adapter : adapters)
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
		} catch (Exception e) {
			Trace.logError(e);
		}
	}

	@Override
	public void destroy() {
		if (config.webServerStartApplicationServer())
			stopServer(ApplicationServerClass, config);

		if (config.webServerStartAuthorityCenter())
			stopServer(AuthorityCenterClass, config);

		if (config.webServerStartTransportServer())
			stopServer(TransportServerClass, config);

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
