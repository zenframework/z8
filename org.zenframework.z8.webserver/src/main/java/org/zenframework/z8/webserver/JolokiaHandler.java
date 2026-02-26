package org.zenframework.z8.webserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.jolokia.http.AgentServlet;

public class JolokiaHandler extends AbstractHandler {

	private final HttpServlet servlet;

	public JolokiaHandler(ServletContext context) {
		servlet = new AgentServlet();

		try {
			servlet.init(new SimpleConfig("Jolokia Servlet", context, getJolokiaParams()));
		} catch (ServletException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		servlet.service(request, response);
	}

	private static Map<String, String> getJolokiaParams() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("debug", "false");
		params.put("historyMaxEntries", "10");
		params.put("debugMaxEntries", "100");
		params.put("maxDepth", "15");
		params.put("maxCollectionSize", "0");
		params.put("maxObjects", "0");
		params.put("detectorOptions", "{}");
		params.put("canonicalNaming", "true");
		params.put("includeStackTrace", "true");
		params.put("serializeException", "false");
		params.put("discoveryEnabled", "false");
		return params;
	}
}
