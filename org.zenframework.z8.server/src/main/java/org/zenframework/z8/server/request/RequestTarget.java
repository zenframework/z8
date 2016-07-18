package org.zenframework.z8.server.request;

import java.util.Map;

import org.zenframework.z8.server.base.model.NamedObject;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.string;

public abstract class RequestTarget extends NamedObject implements IRequestTarget {
	private IRequest request;

	public RequestTarget(String id) {
		super(id);
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

		writer.startResponse(id(), true);

		response.setWriter(writer);

		writeResponse(writer);

		IMonitor monitor = request().getMonitor();

		if(monitor != this)
			monitor.writeResponse(writer);

		writer.finishResponse();

		response.setContent(writer.toString());
	}
}
