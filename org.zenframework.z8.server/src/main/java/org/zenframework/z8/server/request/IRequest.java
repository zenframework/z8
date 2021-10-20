package org.zenframework.z8.server.request;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.engine.EventsLevel;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.request.actions.RequestAction;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;

public abstract class IRequest {
	abstract public Map<string, string> getParameters();

	abstract public Collection<file> getFiles();

	private List<EventsLevel> levels = new ArrayList<EventsLevel>();
	private RequestTarget target;
	private RequestAction action;

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

	public void setEventsLevel(EventsLevel level) {
		levels.add(level);
	}

	public void restoreEventsLevel() {
		if (!levels.isEmpty())
			levels.remove(levels.size() - 1);
	}

	public EventsLevel eventsLevel() {
		return levels.isEmpty() ? EventsLevel.ALL : levels.get(levels.size() - 1);
	}

	public boolean eventsEnabled(EventsLevel level) {
		return eventsLevel().ordinal() <= level.ordinal();
	}

	public RequestTarget getTarget() {
		return target;
	}

	public void setTarget(RequestTarget target) {
		this.target = target;
	}

	public RequestAction getAction() {
		return action;
	}

	public void setAction(RequestAction action) {
		this.action = action;
	}

	public String displayName() {
		return target != null ? target.displayName() : null;
	}

	abstract public IResponse getResponse();
	abstract public void setResponse(IResponse response);

	abstract public ISession getSession();
	abstract public void setSession(ISession session);

	abstract public IMonitor getMonitor();
	abstract public void setMonitor(IMonitor monitor);
}
