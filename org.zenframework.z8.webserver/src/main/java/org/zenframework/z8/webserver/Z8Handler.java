package org.zenframework.z8.webserver;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.component.LifeCycle;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.web.servlet.Servlet;
import org.zenframework.z8.webserver.servlet.DefaultServlet;

public class Z8Handler extends AbstractHandler {

	@SuppressWarnings("deprecation")
	private class LifeCycleListener extends AbstractLifeCycleListener {
		@Override
		public void lifeCycleStopped(LifeCycle event) {
			for(HttpServlet servlet : servlets)
				servlet.destroy();
			for(Filter filter : filters)
				filter.destroy();
		}
	}

	private final ContextHandler context;
	private final Properties mappings;
	private final Map<UrlPattern, HttpServlet> servletPatterns = new LinkedHashMap<UrlPattern, HttpServlet>();
	private final Set<HttpServlet> servlets = new HashSet<HttpServlet>();
	private final Map<UrlPattern, List<Filter>> filterPatterns = new LinkedHashMap<UrlPattern, List<Filter>>();
	private final Set<Filter> filters = new HashSet<Filter>();

	private final HttpServlet servlet = new Servlet();
	private final HttpServlet defaultServlet = new DefaultServlet();

	public Z8Handler(ContextHandler context) {
		this.context = context;
		mappings = getMappings(ServerConfig.webServerMappings());

		initServlets();
		initFilters();
		addLifeCycleListener(new LifeCycleListener());
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		String path = URLDecoder.decode(baseRequest.getRequestURI(), "UTF-8");
		baseRequest.setServletPath(path);
		/*
		 * LOG.debug("REQUEST: " + path);
		 */
		String contentType = getContentType(path);
		if(contentType != null)
			response.setContentType(contentType);

		handle(request, response, path);
		baseRequest.setHandled(true);
	}

	public void handle(HttpServletRequest request, HttpServletResponse response, String path) throws IOException, ServletException {
		final List<Filter> filters = new LinkedList<Filter>();
		for(Map.Entry<UrlPattern, List<Filter>> entry : filterPatterns.entrySet())
			if(entry.getKey().matches(path))
				filters.addAll(entry.getValue());

		new FilterChain() {
			@Override
			public void doFilter(ServletRequest var1, ServletResponse var2) throws IOException, ServletException {
				if (!filters.isEmpty())
					filters.remove(0).doFilter(request, response, this);
			}
		}.doFilter(request, response);

		for(Map.Entry<UrlPattern, HttpServlet> entry : servletPatterns.entrySet()) {
			if(entry.getKey().matches(path)) {
				entry.getValue().service(request, response);
				return;
			}
		}

		throw new RuntimeException("Can't handle request " + path);
	}

	protected void initServlets() {
		addServlet(servlet, "Z8 Servlet", Servlet.ServletPaths);
		addServlet(defaultServlet, "Default Servlet", DefaultServlet.ServletPaths);
	}

	public Servlet getZ8Servlet() {
		return (Servlet) servlet;
	}

	private Map<String, String> getCors() {
		String prefix = ServerConfig.WebServerCors + '.';

		Map<String, String> cors = new HashMap<String, String>();
		for(String key : ServerConfig.getKeys()) {
			if(key.startsWith(prefix))
				cors.put(key.substring(prefix.length()), ServerConfig.get(key));
		}
		return cors;
	}

	protected void initFilters() {
		// CORS man:
		// https://www.eclipse.org/jetty/javadoc/jetty-9/org/eclipse/jetty/servlets/CrossOriginFilter.html
		// Add 'web.server.cors.allowedOrigins', etc. to server properties
		Map<String, String> corsConfig = getCors();
		if(!corsConfig.isEmpty())
			addFilter(CrossOriginFilter.class, "CORS", corsConfig, new String[] { "/*" });
	}

	protected void addServlet(HttpServlet servlet, String name, String[] patterns) {
		addServlet(servlet, name, Collections.emptyMap(), patterns);
	}

	protected void addServlet(HttpServlet servlet, String name, Map<String, String> initParameters, String[] patterns) {
		try {
			servlet.init(new SimpleConfig(name, context.getServletContext(), initParameters));
			for(UrlPattern pattern : getUrlPatterns(patterns))
				servletPatterns.put(pattern, servlet);
			servlets.add(servlet);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void addFilter(Class<? extends Filter> filterClass, String name, String[] patterns) {
		addFilter(filterClass, name, Collections.emptyMap(), patterns);
	}

	protected void addFilter(Class<? extends Filter> filterClass, String name, Map<String, String> initParameters, String[] patterns) {
		try {
			Filter filter = filterClass.newInstance();
			filter.init(new SimpleConfig(name, context.getServletContext(), initParameters));
			for(UrlPattern pattern : getUrlPatterns(patterns)) {
				List<Filter> filters = filterPatterns.get(pattern);
				if(filters == null)
					filterPatterns.put(pattern, filters = new LinkedList<Filter>());
				filters.add(filter);
			}
			filters.add(filter);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String getContentType(String path) {
		return mappings.getProperty(FilenameUtils.getExtension(path), "text/html;charset=UTF-8");
	}

	protected static Properties getMappings(String path) {
		Properties mappings = new Properties();
		Reader reader = null;

		try {
			Enumeration<URL> resources = WebServer.class.getClassLoader().getResources("webserver/mappings.properties");
			while(resources.hasMoreElements()) {
				try {
					reader = new InputStreamReader(resources.nextElement().openStream());
					mappings.load(reader);
				} catch(IOException e1) {
				} finally {
					IOUtils.closeQuietly(reader);
				}
			}
		} catch(IOException e) {
			Trace.logError("Couldn't load mappings from classpath webserver/mappings.properties" + path, e);
		}

		if(path != null) {
			try {
				reader = new FileReader(path);
				mappings.load(reader);
			} catch(IOException e) {
				Trace.logError("Couldn't load mappings from " + path, e);
			} finally {
				IOUtils.closeQuietly(reader);
			}
		}
		return mappings;
	}

	protected static Collection<UrlPattern> getUrlPatterns(String[] strs) {
		if(strs == null || strs.length == 0)
			return Collections.emptyList();
		Collection<UrlPattern> patterns = new LinkedList<UrlPattern>();
		for(String str : strs)
			patterns.add(new UrlPattern(str.trim()));
		return patterns;
	}

}
