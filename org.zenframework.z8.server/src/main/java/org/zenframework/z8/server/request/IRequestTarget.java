package org.zenframework.z8.server.request;

import org.zenframework.z8.server.json.JsonWriter;

public interface IRequestTarget {
	public String id();
	public String displayName();

	public void processRequest(IResponse response) throws Throwable;
	public void writeResponse(JsonWriter writer) throws Throwable;
}
