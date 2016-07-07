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
import org.zenframework.z8.server.engine.Z8Context;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.web.server.Adapter;
import org.zenframework.z8.web.server.ConverterAdapter;
import org.zenframework.z8.web.server.JsonAdapter;
import org.zenframework.z8.web.server.TrustedAuthAdapter;

public class Servlet extends HttpServlet {

	static {
		ObjectIO.initialize(new RmiIO());
	}

	private static final long serialVersionUID = 6442937554115725675L;

	static private final String ParamWorkingPath = "working-path";

	private final List<Adapter> adapters = new ArrayList<Adapter>();
	private ServerConfig config;

	private IServer interconnectionCenter;
	private IServer authorityCenter;
	private IServer applicationServer;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		ServletContext context = getServletContext();

		String workingPath = servletConfig.getInitParameter(ParamWorkingPath);
		if(workingPath == null)
			workingPath = context.getRealPath("WEB-INF");

		config = new ServerConfig(workingPath + File.separator + ServerConfig.ConfigurationFileName);

		try {
			Z8Context.init(config);
			if(config.webServerLaunchInterconnectionCenter())
				interconnectionCenter = InterconnectionCenter.launch(config);
			if(config.webServerLaunchAuthorityCenter())
				authorityCenter = AuthorityCenter.launch(config);
			if(config.webServerLaunchApplicationServer())
				applicationServer = ApplicationServer.launch(config);
		} catch(Throwable e) {
			Trace.logError(e);
			destroy();
			throw new ServletException(e);
		}

		adapters.add(new JsonAdapter(this));
		adapters.add(new TrustedAuthAdapter(this));
		adapters.add(new ConverterAdapter(this));

		for(Adapter adapter : adapters)
			adapter.start();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Adapter adapter = getAdapter(request);
		request.setCharacterEncoding(encoding.Default.toString());
		adapter.service(request, response);
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

		config = null;

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
}
