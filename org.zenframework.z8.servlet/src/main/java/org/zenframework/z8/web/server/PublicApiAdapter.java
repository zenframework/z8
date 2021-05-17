package org.zenframework.z8.web.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.exceptions.ServerUnavailableException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonException;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.router.Router;
import org.zenframework.z8.server.router.UrlMatch;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.utils.NumericUtils;
import org.zenframework.z8.web.servlet.Servlet;
import org.zenframework.z8.web.utils.ServletUtil;

public class PublicApiAdapter extends Adapter {
	static private final String AdapterPath = "/public-api/";
	private static final Collection<String> IgnoredExceptions = Arrays
			.asList("org.apache.catalina.connector.ClientAbortException");
	
	private static final Collection<String> DATA_RECORD_ID_ACTIONS = Arrays.asList("update", "destroy");

	Router router;

	public PublicApiAdapter(Servlet servlet) {
		super(servlet);

		this.router = new Router(this.AdapterPath);
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().contains(this.AdapterPath);
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		UrlMatch match = this.router.match(request);

		if (match != null) {
			Map<String, String> parameters = new HashMap<String, String>();
			List<file> files = new ArrayList<file>();

			match.parameterSet().forEach(parameter -> {
				if (parameter.getKey() == "action" && parameter.getValue() == "login") {
					parameters.put("request", "login");
					
					return;
				}
				
				parameters.put(parameter.getKey(), parameter.getValue());
			});
			
			ISession session = this.tryLogin(request, response, parameters, files);

			if (session != null) {
				this.service(session, parameters, files, request, response);
			}
		}
	}

	/**
	 * @param request
	 * @param response
	 * @param parameters
	 * @param files
	 * @return
	 */
	public ISession tryLogin(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters,
			List<file> files) throws IOException, ServletException {
		HttpSession httpSession = useContainerSession ? request.getSession() : null;

		try {
			ISession session = null;

			this.parseRequest(request, response, parameters, files);

			boolean isLogin = Json.login.equals(parameters.get(Json.request.get()));

			String sessionId = getParameter(Json.session.get(), parameters, httpSession);
			String serverId = parameters.get(Json.server.get());

			if (isLogin && sessionId == null) {
				String login = parameters.get(Json.login.get());
				String password = parameters.get(Json.password.get());

				if (login == null || login.isEmpty() || login.length() > IAuthorityCenter.MaxLoginLength
						|| password != null && password.length() > IAuthorityCenter.MaxPasswordLength) {
					throw new AccessDeniedException();
				}

				session = this.login(login, password, ServletUtil.getSchema(request));
				if (httpSession != null) {
					httpSession.setAttribute(Json.session.get(), session.id());
				}
			} else {
				session = this.authorize(sessionId, serverId, parameters.get(Json.request.get()));
			}

			if (session == null) {
				throw serverId == null ? new AccessDeniedException() : new ServerUnavailableException(serverId);
			}

			return session;

		} catch (AccessDeniedException e) {
			if (httpSession != null) {
				httpSession.invalidate();
			}

			processAccessDenied(response);
		} catch (NoSuchObjectException e) {
			processAccessDenied(response);
		} catch (ConnectException e) {
			processAccessDenied(response);
		} catch (Throwable e) {
			String className = e.getClass().getCanonicalName();

			if (!IgnoredExceptions.contains(className)) {
				Trace.logError(e);
				processError(response, e);
			}
		}

		return null;
	}

	private void parseRequest(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters,
			List<file> files) throws IOException, JsonException, ServletException {
		if (ServletFileUpload.isMultipartContent(request)) {
			List<FileItem> fileItems = parseMultipartRequest(request);

			long fileSizeMaxMB = ServerConfig.webServerUploadMax();
			long fileSizeMax = fileSizeMaxMB > 0 ? fileSizeMaxMB * NumericUtils.Megabyte : Long.MAX_VALUE;

			for (FileItem fileItem : fileItems) {
				if (!fileItem.isFormField()) {
					if (fileItem.getSize() > fileSizeMax) {
						throw new RuntimeException(
								Resources.format("Exception.fileSizeLimitExceeded", fileItem.getName(), fileSizeMaxMB));
					}

					files.add(new file(fileItem));
				} else {
					Map<String, String[]> requestParameters = request.getParameterMap();
					parameters.put(fileItem.getFieldName(), fileItem.getString(encoding.Default.toString()));
				}
			}
		} else {
			String requestData = request.getReader().lines().collect(Collectors.joining());

			try {
				JsonObject jsonRequestData = new JsonObject(requestData);
				Iterator<String> keys = jsonRequestData.keys();

				while (keys.hasNext()) {
					String key = keys.next();
					String value = jsonRequestData.getString(key);


					
					if (request.getMethod() == "GET" && parameters.containsKey("requestId")) {
						String filter = this.buildBaseFilter("recordId", parameters.get("recordId"), "eq", "logical");
						
						parameters.put("filter", filter);
					}
					
					if (DATA_RECORD_ID_ACTIONS.contains(request.getMethod()) && parameters.containsKey("requestId") && key == "data") {
						JsonObject data = new JsonObject(value);
						data.put("recortId", parameters.get("recordId"));
					}
					
					parameters.put(key, value);
				}
			} catch (JsonException $e) {
				processError(response, $e);
			}
		}
		
		parameters.put(Json.ip.get(), request.getRemoteAddr());
	}

	private static String getParameter(String key, Map<String, String> parameters, HttpSession httpSession) {
		String value = parameters.get(key);

		if (httpSession != null && value == null) {
			value = (String) httpSession.getAttribute(key);
		}

		return value;
	}
	
	private String buildBaseFilter(String propertyName, String value, String operator, String logical) {
		ArrayList<JsonObject> filter = new ArrayList<JsonObject>();
		ArrayList<JsonObject> expression = new ArrayList<JsonObject>();
		
		HashMap<String, String> expressionsData = new HashMap<String, String>();
		expressionsData.put("property", propertyName);
		expressionsData.put("value", value);
		expressionsData.put("operator", operator);
		HashMap<String, String> filterData = new HashMap<String, String>();
		filterData.put("expressions", expression.toString());
		filterData.put("logical", logical);
		
		expression.add(new JsonObject(expressionsData));
		filter.add(new JsonObject(filterData));
	
		return filter.toString();
	}

	@Override
	protected void service(ISession session, Map<String, String> parameters, List<file> files,
			HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		super.service(session, parameters, files, request, response);
	}

	@Override
	protected void processError(HttpServletResponse response, Throwable e) throws IOException, ServletException {
		super.processError(response, e);
	}

	@Override
	protected void processAccessDenied(HttpServletResponse response) throws IOException {
		super.processAccessDenied(response);
	}
}
