package org.zenframework.z8.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.ContentType;
import org.zenframework.z8.server.request.Message;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.Charset;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/*
 * Z8ErrorHandler always returns valid JSON for SSO requests
 */
public class Z8ErrorHandler extends ErrorHandler {

	public static class strings {
		public static final String AccessDenied = "Exception.accessDenied";
	}

	private static final String ENCODING = Charset.Default.toString();

	private static final String TEMPLATES_PATH = "templates";
	private static final String DEFAULT_TEMPLATE = "<html>\n"
			+ "<head>\n"
			+ "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/>\n"
			+ "<title>HTTP Error ${status}</title>\n"
			+ "</head>\n"
			+ "<body><h2>HTTP ERROR ${status}</h2>\n"
			+ "<table style=\"text-align: left\">\n"
			+ "<tr><th>URI:</th><td>${uri}</td></tr>\n"
			+ "<tr><th>STATUS:</th><td>${status}</td></tr>\n"
			+ "</table>\n"
			+ "</body>\n"
			+ "</html>\n";

	private Template template;

	@Override
	public void doError(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String path = URLDecoder.decode(baseRequest.getRequestURI(), "UTF-8");

		handleDefault(path, response);
	}

	protected void handleDefault(String uri, HttpServletResponse response) throws IOException {
		Writer writer = new StringWriter();

		try {
			getTemplate().process(getBindings(uri, response), writer);
		} catch (TemplateException exc) {
			throw new RuntimeException(exc);
		}

		writeResponse(response, ContentType.Html, writer.toString());
	}

	protected void handleSsoError(HttpServletResponse response) throws IOException {
		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, false, response.getStatus());
		writer.writeInfo(Arrays.asList(Message.error(new RuntimeException(Resources.get(strings.AccessDenied)), null)), Collections.emptyList(), null);
		writer.startArray(Json.data);
		writer.finishArray();
		writer.finishResponse();

		writeResponse(response, ContentType.Json, writer.toString());
	}

	protected void writeResponse(HttpServletResponse response, ContentType contentType, String content) throws IOException {
		byte[] bytes = content.getBytes(ENCODING);
		response.setContentType(contentType + ";charset=" + ENCODING);
		response.setContentLength(bytes.length);
		response.getOutputStream().write(bytes);
	}

	protected Template getTemplate() {
		if (template != null)
			return template;

		String name = getTemplateName();

		Configuration configuration = new Configuration();
		configuration.setDefaultEncoding(Charset.Default.toString());
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		try {
			template = new Template(name, new InputStreamReader(new FileInputStream(new File(ServerConfig.workingPath(), TEMPLATES_PATH + '/' + name)), ENCODING), configuration);
		} catch (IOException e) {
			try {
				template = new Template(name, new StringReader(DEFAULT_TEMPLATE), configuration);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}

		return template;
	}

	protected Map<String, Object> getBindings(String uri, HttpServletResponse response) {
		Map<String, Object> bindings = new HashMap<String, Object>();
		bindings.put("uri", uri);
		bindings.put("status", response.getStatus());
		return bindings;
	}

	protected String getTemplateName() {
		return "error_" + ServerConfig.language() + ".html";
	}
}
