package org.zenframework.z8.web.servlet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.zenframework.z8.auth.AuthorityCenter;
import org.zenframework.z8.interconnection.InterconnectionCenter;
import org.zenframework.z8.rmi.ObjectIO;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IServer;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.web.server.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

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

	private static final Collection<UrlPattern> urlPatterns = new LinkedList<UrlPattern>(
			Arrays.asList(
					new UrlPattern("/apidoc"),
					new UrlPattern("*.json"),
					new UrlPattern("/storage/*"),
					new UrlPattern("/files/*"),
					new UrlPattern("/reports/*")));

	private Properties mappings;
	private WebResourceHandler resourceHandler;


	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		mappings = Servlet.getMappings(ServerConfig.webServerMappings());
		urlPatterns.addAll(Servlet.getUrlPatterns(ServerConfig.webServerUrlPatterns()));

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

		adapters.add(new APIDocAdapter(this));
		adapters.add(new SystemAdapter(this));
		adapters.add(new ConverterAdapter(this));
		adapters.add(new LogoutAdapter(this));

		for(Adapter adapter : adapters)
			adapter.start();

		resourceHandler = getWebResourceHandler();
		resourceHandler.init(Folders.Base, ServerConfig.webServerWebapp(), ServerConfig.language());
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = URLDecoder.decode(request.getRequestURI(), "UTF-8");
/*
				LOG.debug("REQUEST: " + path);
*/
		response.setCharacterEncoding("UTF-8");
		response.setContentType(getContentType(path));

		if (Servlet.isSystemRequest(path)) {
			// Z8 request
			Adapter adapter = getAdapter(request);
			if (adapter != null) {
				request.setCharacterEncoding(encoding.Default.toString());
				adapter.service(request, response);
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else if (path.contains("..") || path.startsWith("/WEB-INF"))
			// Access denied
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal path " + path);
		else
			// Files or other resources
			resourceHandler.handle(path, response);
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

	protected WebResourceHandler getWebResourceHandler() {
		return new WebResourceHandler();
	}

	private String getContentType(String path) {
		return mappings.getProperty(FilenameUtils.getExtension(path), "text/html;charset=UTF-8");
	}

	private static Properties getMappings(String path) {
		Properties mappings = new Properties();
		Reader reader = null;

		try {
			Enumeration<URL> resources = Servlet.class.getClassLoader().getResources("webserver/mappings.properties");
			while (resources.hasMoreElements()) {
				try {
					reader = new InputStreamReader(resources.nextElement().openStream());
					mappings.load(reader);
				} catch (IOException e1) {
				} finally {
					IOUtils.closeQuietly(reader);
				}
			}
		} catch (IOException e) {
			Trace.logError("Couldn't load mappings from classpath webserver/mappings.properties" + path, e);
		}

		if (path != null) {
			try {
				reader = new FileReader(path);
				mappings.load(reader);
			} catch (IOException e) {
				Trace.logError("Couldn't load mappings from " + path, e);
			} finally {
				IOUtils.closeQuietly(reader);
			}
		}
		return mappings;
	}

	private static boolean isSystemRequest(String path) {
		for (UrlPattern pattern : urlPatterns)
			if (pattern.matches(path))
				return true;
		return false;
	}

	private static Collection<UrlPattern> getUrlPatterns(String str) {
		if (str == null || str.isEmpty())
			return Collections.emptyList();
		Collection<UrlPattern> patterns = new LinkedList<UrlPattern>();
		String[] parts = str.split("\\,");
		for (String part : parts)
			patterns.add(new UrlPattern(part.trim()));
		return patterns;
	}
	
}
