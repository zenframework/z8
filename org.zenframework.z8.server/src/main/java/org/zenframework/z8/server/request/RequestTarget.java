package org.zenframework.z8.server.request;

import java.util.Map;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.string;

public abstract class RequestTarget implements IRequestTarget {
	private IRequest request;
	private String id;

	public RequestTarget() {
	}

	public RequestTarget(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public String displayName() {
		return null;
	}

	public IRequest request() {
		if(request == null)
			request = ApplicationServer.getRequest();
		return request;
	}

	public Map<string, string> getParameters() {
		return request().getParameters();
	}

	public String getParameter(string key) {
		return request().getParameter(key);
	}

	@Override
	public void processRequest(IResponse response) throws Throwable {
		JsonWriter writer = new JsonWriter();

		writer.startResponse(request().id(), true);

		response.setWriter(writer);

		writeResponse(writer);

		IMonitor monitor = request().getMonitor();

		if(monitor != this)
			monitor.writeResponse(writer);

		writer.finishResponse();

		response.setContent(writer.toString());
	}
}
