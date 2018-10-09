package org.zenframework.z8.web.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.auth.AuthorityCenter;
import org.zenframework.z8.interconnection.InterconnectionCenter;
import org.zenframework.z8.rmi.ObjectIO;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IServer;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.web.server.Adapter;
import org.zenframework.z8.web.server.ConverterAdapter;
import org.zenframework.z8.web.server.LogoutAdapter;
import org.zenframework.z8.web.server.SiteAdapter;
import org.zenframework.z8.web.server.SystemAdapter;

public class Servlet extends HttpServlet {

	static final private String StartApplicationServer = "startApplicationServer";
	static final private String StartAuthorityCenter = "startAuthorityCenter";
	static final private String StartInterconnectionCenter = "startInterconnectionCenter";

	static {
		ObjectIO.initialize(new RmiIO());
	}

	private static final long serialVersionUID = 6442937554115725675L;

	private final List<Adapter> adapters = new ArrayList<Adapter>();

	private IServer interconnectionCenter;
	private IServer authorityCenter;
	private IServer applicationServer;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		ServletContext context = getServletContext();

		String workingPath = context.getRealPath("WEB-INF");

		try {
			ServerConfig config = new ServerConfig(new File(workingPath, ServerConfig.DefaultConfigurationFileName).getPath());

			if(getInitParameter(servletConfig, StartInterconnectionCenter, false))
				interconnectionCenter = InterconnectionCenter.launch(config);
			if(getInitParameter(servletConfig, StartAuthorityCenter, true))
				authorityCenter = AuthorityCenter.launch(config);
			if(getInitParameter(servletConfig, StartApplicationServer, true))
				applicationServer = ApplicationServer.launch(config);
		} catch(Throwable e) {
			Trace.logError(e);
			destroy();
			throw new ServletException(e);
		}

		adapters.add(new SystemAdapter(this));
		adapters.add(new SiteAdapter(this));
		adapters.add(new ConverterAdapter(this));
		adapters.add(new LogoutAdapter(this));

		for(Adapter adapter : adapters)
			adapter.start();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Adapter adapter = getAdapter(request);
		if (adapter != null) {
			request.setCharacterEncoding(encoding.Default.toString());
			adapter.service(request, response);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	private void stopServer(IServer server) {
		try {
			server.stop();
		} catch(Throwable e) {
			Trace.logError(e);
		}
	}

	@Override
	public void destroy() {
		if(applicationServer != null)
			stopServer(applicationServer);
		if(authorityCenter != null)
			stopServer(authorityCenter);
		if(interconnectionCenter != null)
			stopServer(interconnectionCenter);

		for(Adapter adapter : adapters)
			adapter.stop();

		adapters.clear();

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
	
	private static boolean getInitParameter(ServletConfig servletConfig, String name, boolean defaultValue) {
		String value = servletConfig.getInitParameter(name);
		return value != null ? Boolean.parseBoolean(value) : defaultValue;
	}
	
}
