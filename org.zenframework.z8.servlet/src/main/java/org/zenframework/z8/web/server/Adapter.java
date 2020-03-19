package org.zenframework.z8.web.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.exceptions.ServerUnavailableException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Message;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;
import org.zenframework.z8.web.servlet.Servlet;

public abstract class Adapter {
	private static final String UseContainerSession = "useContainerSession";
	static final String ApplicationJson = "application/json";
	static final String TextHtml = "text/html";

	private static final Collection<String> IgnoredExceptions = Arrays.asList("org.apache.catalina.connector.ClientAbortException");

	protected Servlet servlet;
	private boolean useContainerSession;

	protected Adapter(Servlet servlet) {
		this.servlet = servlet;
		useContainerSession = Boolean.parseBoolean(servlet.getInitParameter(UseContainerSession));
	}

	protected Servlet getServlet() {
		return servlet;
	}

	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		HttpSession httpSession = useContainerSession ? request.getSession() : null;
		try {
			ISession session = null;

			Map<String, String> parameters = new HashMap<String, String>();
			List<file> files = new ArrayList<file>();

			parseRequest(request, parameters, files);

			boolean isLogin = Json.login.equals(parameters.get(Json.request.get()));

			String sessionId = parameters.get(Json.session.get());
			String serverId = parameters.get(Json.server.get());

			if(isLogin) {
				String login = getParameter(Json.login.get(), parameters, httpSession);
				String password = getParameter(Json.password.get(), parameters, httpSession);

				if(login == null || login.isEmpty() || login.length() > IAuthorityCenter.MaxLoginLength || password != null && password.length() > IAuthorityCenter.MaxPasswordLength)
					throw new AccessDeniedException();

				session = login(login, password);
			} else
				session = authorize(sessionId, serverId, parameters.get(Json.request.get()));

			if(session == null)
				throw serverId == null ? new AccessDeniedException() : new ServerUnavailableException(serverId);

			service(session, parameters, files, request, response);
		} catch(AccessDeniedException e) {
			if (httpSession != null)
				httpSession.invalidate();
			processAccessDenied(response);
		} catch(NoSuchObjectException e) {
			processAccessDenied(response);
		} catch(ConnectException e) {
			processAccessDenied(response);
		} catch(Throwable e) {
			String className = e.getClass().getCanonicalName();
			if(!IgnoredExceptions.contains(className)) {
				Trace.logError(e);
				processError(response, e);
			}
		}
	}

	protected ISession login(String login, String password) throws IOException, ServletException {
		return ServerConfig.authorityCenter().login(login, password);
	}

	protected ISession authorize(String sessionId, String serverId, String request) throws IOException, ServletException {
		return sessionId != null ? ServerConfig.authorityCenter().server(sessionId, serverId) : null;
	}

	private void parseRequest(HttpServletRequest request, Map<String, String> parameters, List<file> files) throws IOException {
		if(ServletFileUpload.isMultipartContent(request)) {
			List<FileItem> fileItems = parseMultipartRequest(request);

			long fileSizeMaxMB = ServerConfig.webServerUploadMax();
			long fileSizeMax = fileSizeMaxMB > 0 ? fileSizeMaxMB * NumericUtils.Megabyte : Long.MAX_VALUE;

			for(FileItem fileItem : fileItems) {
				if(!fileItem.isFormField()) {
					if(fileItem.getSize() > fileSizeMax)
						throw new RuntimeException(Resources.format("Exception.fileSizeLimitExceeded", fileItem.getName(), fileSizeMaxMB));
					files.add(new file(fileItem));
				} else
					parameters.put(fileItem.getFieldName(), fileItem.getString(encoding.Default.toString()));
			}
		} else {
			Map<String, String[]> requestParameters = request.getParameterMap();

			for(String name : requestParameters.keySet()) {
				String[] values = requestParameters.get(name);
				parameters.put(name, values.length != 0 ? values[0] : null);
			}
		}

		parameters.put(Json.ip.get(), request.getRemoteAddr());
	}

	protected List<FileItem> parseMultipartRequest(HttpServletRequest request) {
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

		try {
			return upload.parseRequest(request);
		} catch(FileUploadException e) {
			throw new RuntimeException(e);
		}
	}

	public void start() {
	}

	public void stop() {
	}

	abstract public boolean canHandleRequest(HttpServletRequest request);

	protected void service(ISession session, Map<String, String> parameters, List<file> files, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		GNode node = new GNode(parameters, files);

		IApplicationServer server = session.getServerInfo().getServer();
		node = server.processRequest(session, node);

		if(response != null)
			writeResponse(response, node.getInputStream(), getContentType(parameters));
	}

	private String getContentType(Map<String, String> parameters) {
		boolean content = Json.content.get().equals(parameters.get(Json.action.get()));
		return content ?  TextHtml : ApplicationJson; 
	}

	protected void processError(HttpServletResponse response, Throwable e) throws IOException, ServletException {
		writeError(response, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	protected void processAccessDenied(HttpServletResponse response) throws IOException {
		writeError(response, Resources.get("Exception.accessDenied"), HttpServletResponse.SC_UNAUTHORIZED);
	}

	@SuppressWarnings("unchecked")
	private void writeError(HttpServletResponse response, String errorText, int status) throws IOException {
		response.setStatus(status);

		JsonWriter writer = new JsonWriter();

		if(errorText == null || errorText.isEmpty())
			errorText = "Internal server error.";

		writer.startResponse(null, false, status);
		writer.writeInfo(Arrays.asList(Message.error(new RuntimeException(errorText), null)), Collections.EMPTY_LIST, null);
		writer.startArray(Json.data);
		writer.finishArray();
		writer.finishResponse();

		writeResponse(response, writer.toString());
	}

	protected void writeResponse(HttpServletResponse response, String content) throws IOException {
		InputStream in = new ByteArrayInputStream(content.getBytes());
		writeResponse(response, in, ApplicationJson);
	}

	protected void writeResponse(HttpServletResponse response, InputStream in, String contentType) throws IOException {
		response.setContentType(contentType + ";charset=" + encoding.Default.toString());

		OutputStream out = response.getOutputStream();
		IOUtils.copyLarge(in, out);
	}

	private static String getParameter(String key, Map<String, String> parameters, HttpSession httpSession) {
		String value = parameters.get(key);

		if(httpSession != null) {
			if(value != null)
				httpSession.setAttribute(key, value);
			else
				value = (String)httpSession.getAttribute(key);
		}

		return value;
	}
}
