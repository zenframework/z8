package org.zenframework.z8.web.server;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.zenframework.z8.server.apidocs.APIDocumentationBuilder;
import org.zenframework.z8.server.apidocs.dto.EntityDocumentation;
import org.zenframework.z8.server.config.FreeMarkerConfiguration;
import org.zenframework.z8.server.request.ContentType;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.web.servlet.Servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;


public class APIDocAdapter extends Adapter {

	private static final String APIClassesPath = "META-INF/api_classes.list";
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
		Collection<OBJECT.CLASS<? extends OBJECT>> entities = new ArrayList<>();
		URL resource = getClass().getClassLoader().getResource(APIClassesPath);
		if (resource == null) {
			return;
		}
		String rawText = IOUtils.readText(resource);
		if (rawText.isEmpty()) {
			return;
		}
		Arrays.stream(rawText.split("\\n"))
				.forEach(className -> entities.add((OBJECT.CLASS<? extends OBJECT>) Loader.loadClass(className)));
		Map<String, List<EntityDocumentation>> data = new APIDocumentationBuilder().build(entities);

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
