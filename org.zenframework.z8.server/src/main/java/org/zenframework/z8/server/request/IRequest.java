package org.zenframework.z8.server.request;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;

public abstract class IRequest {
	abstract public Map<string, string> getParameters();

	abstract public List<file> getFiles();

	private int events = 0;
	private RequestTarget target;

	public String getParameter(string key) {
		Map<string, string> parameters = getParameters();

		if(parameters == null)
			return null;

		string parameter = parameters.get(key);

		return parameter != null ? parameter.get() : null;
	}

	public String id() {
		try {
			String id = getParameter(Json.request);
			return id != null ? URLDecoder.decode(id, encoding.UTF8.toString()) : null;
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public void disableEvents() {
		events++;
	}

	public void enableEvents() {
		events--;
	}

	public boolean events() {
		return events == 0;
	}

	public RequestTarget getTarget() {
		return target;
	}

	public void setTarget(RequestTarget target) {
		this.target = target;
	}

	public String displayName() {
		return target != null ? target.displayName() : null;
	}

	abstract public IResponse getResponse();

	abstract public ISession getSession();

	abstract public IMonitor getMonitor();

	abstract public void setMonitor(IMonitor monitor);
}
