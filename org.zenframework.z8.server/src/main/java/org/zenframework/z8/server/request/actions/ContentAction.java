package org.zenframework.z8.server.request.actions;

import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.types.binary;

public class ContentAction extends RequestAction {
	public ContentAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void processRequest(IResponse response) throws Throwable {
		binary binary = getQuery().getContent();
		if(binary != null)
			response.setInputStream(binary.get());
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		throw new UnsupportedOperationException();
	}

}
