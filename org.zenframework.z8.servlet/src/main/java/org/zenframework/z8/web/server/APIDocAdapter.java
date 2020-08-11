package org.zenframework.z8.web.server;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.zenframework.z8.server.apidocs.APIDocumentationBuilder;
import org.zenframework.z8.server.apidocs.dto.EntityDocumentation;
import org.zenframework.z8.server.config.FreeMarkerConfiguration;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.request.ContentType;
import org.zenframework.z8.server.runtime.IRuntime;
import org.zenframework.z8.web.servlet.Servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class APIDocAdapter extends Adapter {

	static private final String templateName = "api_documentation.html";
	static private final String AdapterPath = "/apidoc";

	public APIDocAdapter(Servlet servlet) {
		super(servlet);
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().endsWith(AdapterPath);
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		IRuntime runtime = Runtime.instance();
		Map<String, List<EntityDocumentation>> data = new APIDocumentationBuilder().build(runtime.requests());

		Configuration freeMarkerCfg = FreeMarkerConfiguration.getFreeMarkerCfg();
		Template temp = freeMarkerCfg.getTemplate(templateName);

		StringWriter stringWriter = new StringWriter();
		try {
			temp.process(data, stringWriter);
		} catch (TemplateException exc) {
			throw new RuntimeException(exc);
		}
		InputStream inputStream = new ByteArrayInputStream(stringWriter.toString().getBytes());
		writeResponse(response, inputStream, ContentType.Html);
	}
}
