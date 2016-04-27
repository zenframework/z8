package org.zenframework.z8.server.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.Json;

public class Request extends IRequest {

	private Map<String, String> parameters = new HashMap<String, String>();
	private List<FileInfo> files = new ArrayList<FileInfo>();

	private ISession session;
	private IResponse response;
	private IMonitor monitor;

	public Request(ISession session) {
		this.session = session;
		this.monitor = new Monitor();
	}

	public Request(Map<String, String> parameters, List<FileInfo> files, ISession session) {
		this.parameters = parameters;
		this.files = files;
		this.session = session;
		this.monitor = new Monitor();
		this.response = new Response();
	}

	@Override
	public IResponse getResponse() {
		return response;
	}

	@Override
	public Map<String, String> getParameters() {
		return parameters;
	}

	@Override
	public List<FileInfo> getFiles() {
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

		for (String key : parameters.keySet()) {
			if (!Json.data.equals(key) && !Json.requestId.equals(key)) {
				String value = parameters.get(key);
				result += (result.isEmpty() ? "" : ", ") + key + "=" + (value != null ? value : "");
			}
		}

		String[] requestId = parameters.get(Json.requestId).split("\\.");
		return requestId[requestId.length - 1] + "={" + result + "}";
	}

	@Override
	public String toString() {
		return parametersAsString() + ", user=" + session.user().name();
	}

}
