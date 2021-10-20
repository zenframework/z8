package org.zenframework.z8.web.server;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.zenframework.z8.server.apidocs.DocBuilder;
import org.zenframework.z8.server.apidocs.dto.Documentation;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.ContentType;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.web.servlet.Servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

public class APIDocAdapter extends Adapter {

	private static final String templateName = "api_documentation.html";
	private static final String AdapterPath = "/apidoc";

	private Documentation documentation;
	private Template temp;

	public APIDocAdapter(Servlet servlet) {
		super(servlet);
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().endsWith(AdapterPath);
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		initTemplate();
		// TODO Cache API documentation in file
		StringWriter stringWriter = new StringWriter();
		try {
			temp.process(documentation, stringWriter);
		} catch (TemplateException exc) {
			throw new RuntimeException(exc);
		}
		InputStream inputStream = new ByteArrayInputStream(stringWriter.toString().getBytes());
		writeResponse(response, inputStream, ContentType.Html);
	}

	private void initTemplate() {
		if (temp != null)
			return;

		Collection<OBJECT.CLASS<? extends OBJECT>> apiClasses = new ArrayList<>();
		for (OBJECT.CLASS<? extends OBJECT> request : Runtime.instance().requests())
			if (request.hasAttribute(Json.apiDescription.get()) && !request.getAttribute(Json.apiDescription.get()).isEmpty())
				apiClasses.add(request);

		ApplicationServer.setRequest(new Request(new Session(ServerConfig.databaseSchema())));
		try {
			documentation = new DocBuilder().build(apiClasses);
		} catch (Exception e) {
			e.printStackTrace();
			Trace.logError(e);
			throw new RuntimeException(e);
		} finally {
			ApplicationServer.setRequest(null);	
		}
		try {
			temp = APIDocAdapter.getFreeMakerCfg().getTemplate(templateName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Configuration getFreeMakerCfg() {
		// TODO Upgrade freemarker to 2.3.30
		Configuration freeMarkerCfg = new Configuration();
		freeMarkerCfg.setClassForTemplateLoading(APIDocAdapter.class, "/templates/");
		freeMarkerCfg.setDefaultEncoding("UTF-8");
		freeMarkerCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		//freeMarkerCfg.setLogTemplateExceptions(false);
		//freeMarkerCfg.setWrapUncheckedExceptions(true);
		//freeMarkerCfg.setFallbackOnNullLoopVariable(false);
		return freeMarkerCfg;
	}

}
