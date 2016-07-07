package org.zenframework.z8.server.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;

public class Request extends IRequest {

	private Map<string, string> parameters = new RLinkedHashMap<string, string>();
	private List<file> files = new ArrayList<file>();

	private ISession session;
	private IResponse response;
	private IMonitor monitor;

	public Request(ISession session) {
		this.session = session;
		this.monitor = new Monitor();
	}

	public Request(Map<String, String> parameters, List<file> files, ISession session) {
		this.files = files;
		this.session = session;
		this.monitor = new Monitor();
		this.response = new Response();
		
		for(Map.Entry<String, String> entry : parameters.entrySet())
			this.parameters.put(new string(entry.getKey()), new string(entry.getValue()));
	}

	@Override
	public IResponse getResponse() {
		return response;
	}

	@Override
	public Map<string, string> getParameters() {
		return parameters;
	}

	@Override
	public List<file> getFiles() {
		return files;
	}

	@Override
	public ISession getSession() {
		return session;
	}

	public void setSession(ISession session) {
		this.session = session;
	}

	@Override
	public IMonitor getMonitor() {
		return monitor;
	}

	@Override
	public void setMonitor(IMonitor monitor) {
		this.monitor = monitor;
	}

	private String parametersAsString() {
		String result = "";

		for (Map.Entry<string, string> entry : parameters.entrySet()) {
			string key = entry.getKey();
			if (!Json.data.equals(key) && !Json.requestId.equals(key)) {
				String value = entry.getValue().get();
				result += (result.isEmpty() ? "" : ", ") + key + "=" + (value != null ? value : "");
			}
		}

		String requestId = getParameter(Json.requestId);
		
		if(requestId != null) {
			String[] ids = requestId.split("\\.");
			requestId = ids[ids.length - 1];
		} else
			requestId = Json.retry + "(" + getParameter(Json.retry) + ")";
		
		
		return requestId + "(" + hashCode() + ") = {" + result + "}";
	}

	@Override
	public String toString() {
		return parametersAsString() + ", user=" + session.user().name();
	}

}
