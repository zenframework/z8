package org.zenframework.z8.server.request;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;

public class Request extends IRequest {

	private Map<string, string> parameters = new RLinkedHashMap<string, string>();
	private List<file> files = new RCollection<file>();

	private ISession session;
	private IResponse response;
	private IMonitor monitor;

	public Request(ISession session) {
		this.session = session;
		this.monitor = new Monitor();
	}

	public Request(Map<String, String> parameters, Collection<file> files, ISession session) {
		this.session = session;
		this.monitor = new Monitor();
		this.response = new Response();

		for(Map.Entry<String, String> entry : parameters.entrySet())
			this.parameters.put(new string(entry.getKey()), new string(entry.getValue()));

		this.files.addAll(files);
	}

	@Override
	public IResponse getResponse() {
		return response;
	}

	@Override
	public void setResponse(IResponse response) {
		this.response = response;
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

	@Override
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

		for(Map.Entry<string, string> entry : parameters.entrySet()) {
			string key = entry.getKey();
			if(!Json.data.equals(key) && !Json.request.equals(key)) {
				String value = left(entry.getValue().get(), 1000);
				result += (result.isEmpty() ? "" : ", ") + key + "=" + (value != null ? value : "");
			}
		}

		String request = getParameter(Json.request);

		if(request != null) {
			String[] ids = request.split("\\.");
			request = ids[ids.length - 1];
		} else
			request = Json.retry + "(" + getParameter(Json.retry) + ")";

		return request + "(" + hashCode() + ") = {" + result + "}";
	}

	private static String left(String value, int max) {
		return value == null || value.length() <= max ? value : value.substring(0, max) + "...";
	}

	@Override
	public String toString() {
		return parametersAsString() + ", user=" + session.user().login();
	}

}
