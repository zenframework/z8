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
import org.zenframework.z8.server.request.ContentType;
import org.zenframework.z8.server.request.Message;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.LoginParameters;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;
import org.zenframework.z8.web.servlet.Servlet;
import org.zenframework.z8.web.utils.ServletUtil;

public abstract class Adapter {
	private static final String UseContainerSession = "useContainerSession";

	private static final Collection<String> IgnoredExceptions = Arrays.asList("org.apache.catalina.connector.ClientAbortException");

	protected Servlet servlet;
	protected boolean useContainerSession;

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
			boolean isRegistration = Json.register.equals(parameters.get(Json.request.get()));
			boolean isVerification = Json.verify.equals(parameters.get(Json.request.get()));
			boolean isRemindInit = Json.remindInit.equals(parameters.get(Json.request.get()));
			boolean isRemind = Json.remind.equals(parameters.get(Json.request.get()));
			boolean isChangePassword = Json.changePassword.equals(parameters.get(Json.request.get()));

			String sessionId = getParameter(Json.session.get(), parameters, httpSession);
			String serverId = parameters.get(Json.server.get());

			if(isChangePassword && sessionId == null)
				changePassword(parameters, request, response);
			else if(isRemindInit && sessionId == null)
				remindInit(parameters, request, response);
			else if(isRemind && sessionId == null)
				remind(parameters, request, response);
			else if(isVerification && sessionId == null)
				verify(parameters, request, response);
			else if(isRegistration && sessionId == null)
				register(parameters, request, response);
			else {
				if(isLogin && sessionId == null)
					session = login(parameters, request, session, httpSession);
				else
					session = authorize(sessionId, serverId, parameters.get(Json.request.get()));
	
				if(session == null)
					throw serverId == null ? new AccessDeniedException() : new ServerUnavailableException(serverId);
	
				service(session, parameters, files, request, response);
			}
		} catch(AccessDeniedException e) {
			if(httpSession != null)
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
	
	private String getRequestHost(HttpServletRequest request) throws MalformedURLException {
		URL rURL = new URL(request.getRequestURL().toString());
		StringBuilder host = new StringBuilder().append(rURL.getProtocol()).append("://").append(rURL.getHost());
		if (rURL.getPort() != -1)
			host.append(":").append(rURL.getPort());
		host.append("/");
		return host.toString();
	}
	
	/**
	 * Initiates password remind. 
	 * If user is not blocked, sets up verification code for password remind.
	 * @param parameters
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	@SuppressWarnings("unchecked")
	private void remindInit(Map<String, String> parameters, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String login = parameters.get(Json.login.get());
		
		ServerConfig.authorityCenter().remindInit(login, ServletUtil.getSchema(request), getRequestHost(request));
		
		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, true);
		writer.writeInfo(Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
		writer.startArray(Json.data);
		writer.finishArray();
		writer.finishResponse();
		
		writeResponse(response, writer.toString());
	}
	
	@SuppressWarnings("unchecked")
	private void remind(Map<String, String> parameters, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String verificationCode = parameters.get(Json.verificationCode.get());
		
		IUser user = ServerConfig.authorityCenter().remind(verificationCode, ServletUtil.getSchema(request), getRequestHost(request));
		boolean accessed = verificationCode.equals(user.verification());
		
		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, true);
		writer.writeInfo(Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
		writer.startArray(Json.data);
		writer.startObject();
		if (accessed)
			writer.writeProperty(Json.verification, user.verification());
		writer.writeProperty(Json.login, user.login());
		writer.writeProperty(Json.success, accessed);
		writer.finishObject();
		writer.finishArray();
		writer.finishResponse();
		
		writeResponse(response, writer.toString());
	}
	
	@SuppressWarnings("unchecked")
	private void changePassword(Map<String, String> parameters, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String verificationCode = parameters.get(Json.verificationCode.get());
		String password = parameters.get(Json.password.get());
		
		ServerConfig.authorityCenter().changePassword(verificationCode, password, ServletUtil.getSchema(request), getRequestHost(request));
		
		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, true);
		writer.writeInfo(Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
		writer.startArray(Json.data);
		writer.finishArray();
		writer.finishResponse();
		
		writeResponse(response, writer.toString());
	}
	
	@SuppressWarnings("unchecked")
	private void verify(Map<String, String> parameters, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String verificationCode = parameters.get(Json.verificationCode.get());
		
		IUser user = ServerConfig.authorityCenter().verify(verificationCode, ServletUtil.getSchema(request), getRequestHost(request));
		boolean unbanned = !user.banned();
		
		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, true);
		writer.writeInfo(Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
		writer.startArray(Json.data);
		writer.startObject();
		writer.writeProperty(Json.login, user.login());
		writer.writeProperty(Json.success, unbanned);
		writer.finishObject();
		writer.finishArray();
		writer.finishResponse();
		
		writeResponse(response, writer.toString());
	}

	@SuppressWarnings("unchecked")
	private void register(Map<String, String> parameters, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String login = parameters.get(Json.email.get());
		String email = parameters.get(Json.email.get());
		String firstName = parameters.get(Json.firstName.get());
		String lastName = parameters.get(Json.lastName.get());
		String password = parameters.get(Json.password.get());
		
		LoginParameters loginParameters = new LoginParameters(login).setEmail(email).setFirstName(firstName).setLastName(lastName).setSchema(ServletUtil.getSchema(request));
		
		ServerConfig.authorityCenter().register(loginParameters, password, getRequestHost(request));
		
		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, true);
		writer.writeInfo(Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
		writer.startArray(Json.data);
		writer.finishArray();
		writer.finishResponse();
		
		writeResponse(response, writer.toString());
	}
	
	private ISession login(Map<String, String> parameters, HttpServletRequest request, ISession session, HttpSession httpSession) throws IOException, ServletException {
		String login = parameters.get(Json.login.get());
		String password = parameters.get(Json.password.get());

		if(login == null || login.isEmpty() || login.length() > IAuthorityCenter.MaxLoginLength || password != null && password.length() > IAuthorityCenter.MaxPasswordLength)
			throw new AccessDeniedException();

		session = login(getLoginParameters(login, request), password);
		if(httpSession != null)
			httpSession.setAttribute(Json.session.get(), session.id());
		return session;
	}

	protected ISession login(LoginParameters loginParameters, String password) throws IOException, ServletException {
		return ServerConfig.authorityCenter().login(loginParameters, password);
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
			writeResponse(response, node.getInputStream(), node.getContentType());
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

	protected static LoginParameters getLoginParameters(String login, HttpServletRequest request) {
		LoginParameters loginParameters = new LoginParameters(login);
		loginParameters.setAddress(request.getRemoteAddr());
		loginParameters.setSchema(ServletUtil.getSchema(request));
		return loginParameters;
	}

	private static String getParameter(String key, Map<String, String> parameters, HttpSession httpSession) {
		String value = parameters.get(key);

		if(httpSession != null && value == null)
			value = (String)httpSession.getAttribute(key);

		return value;
	}
}
