package org.zenframework.z8.web.server;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.zenframework.z8.server.apidocs.DocBuilder;
import org.zenframework.z8.server.apidocs.dto.Documentation;
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
		Documentation documentation = new DocBuilder().build(entities);

		Template temp = APIDocAdapter.getFreeMakerCfg().getTemplate(templateName);

		StringWriter stringWriter = new StringWriter();
		try {
			temp.process(documentation, stringWriter);
		} catch (TemplateException exc) {
			throw new RuntimeException(exc);
		}
		InputStream inputStream = new ByteArrayInputStream(stringWriter.toString().getBytes());
		writeResponse(response, inputStream, ContentType.Html);
	}
	
	private static Configuration getFreeMakerCfg() {
		Configuration freeMarkerCfg = new Configuration(Configuration.VERSION_2_3_30);
		freeMarkerCfg.setClassForTemplateLoading(APIDocAdapter.class, "/templates/");
		freeMarkerCfg.setDefaultEncoding("UTF-8");
		freeMarkerCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freeMarkerCfg.setLogTemplateExceptions(false);
		freeMarkerCfg.setWrapUncheckedExceptions(true);
		freeMarkerCfg.setFallbackOnNullLoopVariable(false);
		return freeMarkerCfg;
	}
}
