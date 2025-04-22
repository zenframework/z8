package org.zenframework.z8.web.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.exceptions.RedirectException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.ContentType;
import org.zenframework.z8.server.request.Message;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.security.LoginParameters;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

public abstract class Adapter {
	protected static final Collection<String> IgnoredExceptions = Arrays.asList("org.apache.catalina.connector.ClientAbortException");

	protected static final long FileSizeMaxMB = ServerConfig.webServerUploadMax();
	protected static final long FileSizeMax = FileSizeMaxMB > 0 ? FileSizeMaxMB * NumericUtils.Megabyte : Long.MAX_VALUE;

	protected static final int UploadMemThresholdMB = ServerConfig.webServerUploadMemThreshold();
	protected static final int UploadMemThreshold = UploadMemThresholdMB > 0 ? UploadMemThresholdMB * NumericUtils.Megabyte : Integer.MAX_VALUE;

	protected static final boolean UseContainerSession = ServerConfig.webServerUseContainerSession();

	public void start() {}

	public void stop() {}

	abstract public boolean canHandleRequest(HttpServletRequest request);

	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			Map<String, String> parameters = new HashMap<String, String>();
			List<file> files = new ArrayList<file>();

			parseRequest(request, parameters, files);

			service(request, response, parameters, files);
		} catch(AccessDeniedException e) {
			request.getSession().invalidate();
			processAccessDenied(response);
		} catch(NoSuchObjectException e) {
			processAccessDenied(response);
		} catch(ConnectException e) {
			processAccessDenied(response);
		} catch (RedirectException e) {
			processRedirect(response, e.url);
		} catch(Throwable e) {
			String className = e.getClass().getCanonicalName();
			if(!IgnoredExceptions.contains(className)) {
				Trace.logError(e);
				processError(response, e);
			}
		}
	}

	protected void service(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters, List<file> files) throws IOException {
		ISession session = authorize(request, parameters);
		checkSession(request, parameters, session);
		service(request, response, parameters, files, session);
	}

	protected ISession authorize(HttpServletRequest request, Map<String, String> parameters) throws IOException {
		String serverId = parameters.get(Json.server.get());
		String sessionId = parameters.get(Json.session.get());

		if (sessionId == null && UseContainerSession)
			sessionId = (String)request.getSession().getAttribute(Json.session.get());

		return sessionId != null ? ServerConfig.authorityCenter().authorize(sessionId, serverId) : null;
	}

	protected void checkSession(HttpServletRequest request, Map<String, String> parameters, ISession session) {
		if(session == null)
			throw new AccessDeniedException();

		if (UseContainerSession && Boolean.parseBoolean(parameters.get(Json.saveSession.get())))
			request.getSession().setAttribute(Json.session.get(), session.id());
	}

	protected void service(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters, List<file> files, ISession session) throws IOException {
		GNode node = new GNode(parameters, files);

		IApplicationServer server = session.getServerInfo().getServer();
		node = server.processRequest(session, node);

		if(response != null)
			writeResponse(response, node.getInputStream(), node.getContentType());
	}

	protected String getRequestHost(HttpServletRequest request) throws MalformedURLException {
		URL rURL = new URL(request.getRequestURL().toString());
		StringBuilder host = new StringBuilder().append(rURL.getProtocol()).append("://").append(rURL.getHost());
		if(rURL.getPort() != -1)
			host.append(":").append(rURL.getPort());
		host.append("/");
		return host.toString();
	}

	protected void parseRequest(HttpServletRequest request, Map<String, String> parameters, List<file> files) throws IOException {
		if(ServletFileUpload.isMultipartContent(request)) {
			List<FileItem> fileItems = parseMultipartRequest(request);

			for(FileItem fileItem : fileItems) {
				if(!fileItem.isFormField()) {
					if(fileItem.getSize() > FileSizeMax)
						throw new RuntimeException(Resources.format("Exception.fileSizeLimitExceeded", fileItem.getName(), FileSizeMaxMB));
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
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory(UploadMemThreshold, null));

		try {
			return upload.parseRequest(request);
		} catch(FileUploadException e) {
			throw new RuntimeException(e);
		}
	}

	protected String extractSchemaName(HttpServletRequest request) {
		if(!ServerConfig.isMultitenant())
			return null;

		String serverName = request.getServerName();
		int index = serverName.indexOf('.');
		if(index == -1 || index == serverName.lastIndexOf('.') && !serverName.endsWith("localhost"))
			throw new AccessDeniedException();

		return serverName.substring(0, index);
	}

	protected void processError(HttpServletResponse response, Throwable e) throws IOException {
		writeError(response, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	protected void processAccessDenied(HttpServletResponse response) throws IOException {
		writeError(response, Resources.get("Exception.accessDenied"), HttpServletResponse.SC_UNAUTHORIZED);
	}

	protected void processRedirect(HttpServletResponse response, String url) throws IOException {
		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);

		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, false, 302);
		writer.writeProperty(Json.redirect, url);
		writer.finishResponse();

		writeResponse(response, writer.toString());
	}

	private void writeError(HttpServletResponse response, String errorText, int status) throws IOException {
		response.setStatus(status);

		JsonWriter writer = new JsonWriter();

		if(errorText == null || errorText.isEmpty())
			errorText = "Internal server error.";

		writer.startResponse(null, false, status);
		writer.writeInfo(Arrays.asList(Message.error(new RuntimeException(errorText), null)), Collections.emptyList(), null);
		writer.startArray(Json.data);
		writer.finishArray();
		writer.finishResponse();

		writeResponse(response, writer.toString());
	}

	protected void writeResponse(HttpServletResponse response, String content) throws IOException {
		writeResponse(response, new ByteArrayInputStream(content.getBytes()), ContentType.Json);
	}

	protected void writeResponse(HttpServletResponse response, InputStream in, ContentType contentType) throws IOException {
		response.setContentType(contentType + ";charset=" + encoding.Default.toString());

		try {
			response.setContentLength(in.available());
			IOUtils.copyLarge(in, response.getOutputStream());
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	protected LoginParameters getLoginParameters(String login, HttpServletRequest request, boolean trusted) {
		return new LoginParameters(login).setAddress(getClientIp(request)).setSchema(extractSchemaName(request)).setTrusted(trusted);
	}

	protected static String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		return ip != null ? ip : request.getRemoteAddr();
	}
}
