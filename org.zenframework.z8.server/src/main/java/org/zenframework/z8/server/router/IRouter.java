package org.zenframework.z8.server.router;

import java.util.Map;

public interface IRouter {

	public Map<String, Route> getHttpEndpoints(); 
}
