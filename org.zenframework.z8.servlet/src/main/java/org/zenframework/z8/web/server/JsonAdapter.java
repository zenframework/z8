package org.zenframework.z8.web.server;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.web.servlet.Servlet;

public class JsonAdapter extends Adapter {

	static private final String AdapterPath = "/request.json";

	public JsonAdapter(Servlet servlet) {
		super(servlet);
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().endsWith(AdapterPath);
	}

	private void writeError(HttpServletResponse response, String errorText, int status) throws IOException {
		JsonWriter writer = new JsonWriter();

		if(errorText == null || errorText.isEmpty())
			errorText = "Internal server error.";

		writer.startResponse(null, false, status);
		writer.writeInfo(errorText);
		writer.startArray(Json.data);
		writer.finishArray();
		writer.finishResponse();

		writeResponse(response, writer.toString().getBytes(encoding.Default.toString()));
	}

	@Override
	protected void processAccessDenied(HttpServletResponse response) throws IOException {
		super.processAccessDenied(response);
		writeError(response, Resources.get("Exception.accessDenied"), HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Override
	protected void processError(HttpServletResponse response, Throwable ex) throws IOException {
		writeError(response, ex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}
}
