package org.zenframework.z8.webserver.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.webserver.resource.IProcessor;
import org.zenframework.z8.webserver.resource.SimpleProcessor;
import org.zenframework.z8.webserver.resource.WebResource;

public class DefaultServlet extends HttpServlet {

	private static final long serialVersionUID = -8135803465620653218L;

	protected static final String WELCOME_FILE = "index.html";

	public static final String[] ServletPaths = { "/*" };
	protected static final String[] Patterns = { "/signin(/[^/]*)?/?", "/signup(/[^/]*)?/?", "/invite(/[^/]*)?/?", "/recover(/[^/]*)?/?" };

	private final Set<String> evaluatedWildcards = new HashSet<String>();
	private final Map<String, Object> bindings = new HashMap<String, Object>();

	private File webapp;
	private IProcessor processor;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		webapp = ServerConfig.webServerWebapp();

		evaluatedWildcards.add("*.html");

		bindings.put(ServerConfig.Language, ServerConfig.language());
		bindings.put(ServerConfig.WebServerSsoAuthenticator, ServerConfig.webServerSsoAuthenticator());
		bindings.put(ServerConfig.WebClientHashPassword, ServerConfig.webClientHashPassword());
		processor = new SimpleProcessor();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.service(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = URLDecoder.decode(request.getRequestURI(), "UTF-8");

		// Access denied
		if(path.contains("..") || path.startsWith("/WEB-INF")) {
			writeErrorFileNotFound(response, path);
			return;
		}

		//bindings.put(Licence.FileName, Licence.getJson(RequestParser.getSchema(request)).toString()); // renew the licence for each request

		WebResource resource = getResource(path);
		String checkedPath = checkPattern(path);

		if(resource == null && checkedPath == null) {
			writeErrorFileNotFound(response, path);
			return;
		} else if(resource == null)
			resource = findResource(checkedPath);

		if(resource != null && resource.isFolder())
			resource = getResource(resource.getPath() + (resource.getPath().endsWith("/") ? "" : "/") + WELCOME_FILE);

		IOUtils.copy(resource.getInputStream(), response.getOutputStream());
	}

	protected WebResource findResource(String path) throws IOException {
		WebResource resource = new WebResource(path, webapp);

		if(resource.open())
			return resource;

		File parent = new File(path).getParentFile();
		return parent != null ? findResource(parent.getPath().replace(File.separatorChar, '/')) : null;
	}

	protected String checkPattern(String path) {
		for(String pattern : Patterns) {
			String checkedPath = path.replaceFirst(pattern, "");
			if(!path.equals(checkedPath))
				return checkedPath;
		}

		return null;
	}

	protected WebResource getResource(String path) throws IOException {
		if(path == null)
			return null;

		WebResource resource = new WebResource(path, webapp);

		if(!resource.open())
			return null;

		if(isEvaluated(resource.getName()))
			resource.evaluate(processor, bindings);

		return resource;
	}

	protected IProcessor getResourceProcessor() {
		return new SimpleProcessor();
	}

	protected boolean isEvaluated(String name) {
		for(String wildcard : evaluatedWildcards)
			if(FilenameUtils.wildcardMatch(name, wildcard))
				return true;
		return false;
	}

	protected static void writeErrorFileNotFound(HttpServletResponse response, String path) throws IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND, "File " + path + " not found");
	}
}
