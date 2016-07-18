package org.zenframework.z8.web.server;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.utils.NumericUtils;
import org.zenframework.z8.web.servlet.Servlet;

public abstract class Adapter {
	private static final Collection<String> IgnoredExceptions = Arrays.asList("org.apache.catalina.connector.ClientAbortException");

	protected Servlet servlet;

	protected Adapter(Servlet servlet) {
		this.servlet = servlet;
	}

	protected Servlet getServlet() {
		return servlet;
	}

	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			ISession session = null;

			Map<String, String> parameters = new HashMap<String, String>();
			List<file> files = new ArrayList<file>();

			parseRequest(request, parameters, files);

			String login = parameters.get(Json.login);
			String password = parameters.get(Json.password);
			String sessionId = parameters.get(Json.sessionId);
			String serverId = parameters.get(Json.serverId);

			if(login != null && password != null) {

				if(login.isEmpty() || login.length() > IAuthorityCenter.MaxLoginLength || password.length() > IAuthorityCenter.MaxPasswordLength)
					throw new AccessDeniedException();

				session = ServerConfig.authorityCenter().login(login, password);
			} else if(sessionId != null) {
				session = ServerConfig.authorityCenter().server(sessionId, serverId);
			}

			if(session == null)
				throw serverId == null ? new AccessDeniedException() : new ServerUnavailableException(serverId);

			service(session, parameters, files, request, response);
		} catch(AccessDeniedException e) {
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

	private void parseRequest(HttpServletRequest request, Map<String, String> parameters, List<file> files) throws IOException {
		if(ServletFileUpload.isMultipartContent(request)) {
			List<FileItem> fileItems = parseMultipartRequest(request);

			long fileSizeMaxMB = ServerConfig.webServerFileSizeMax();
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
			@SuppressWarnings("unchecked")
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

	protected void processError(HttpServletResponse response, Throwable e) throws IOException, ServletException {
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		writeResponse(response, (e.getMessage() != null ? e.getMessage() : "Internal server error").getBytes(encoding.Default.toString()));
	}

	protected void writeResponse(HttpServletResponse response, byte[] content) throws IOException {
		response.setContentType("text/html;charset=" + encoding.Default.toString());

		OutputStream out = response.getOutputStream();
		out.write(content);
		out.flush();
		out.close();
	}

	protected void processAccessDenied(HttpServletResponse response) throws IOException {
	}

	protected void service(ISession session, Map<String, String> parameters, List<file> files, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		GNode node = new GNode(parameters, files);

		IApplicationServer server = session.getServerInfo().getServer();
		node = server.processRequest(session, node);

		if(response != null)
			writeResponse(response, node.getContent());
	}
}
