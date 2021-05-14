package org.zenframework.z8.server.router;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.OBJECT.CLASS;

public class RouterList {
	private Map<String, Route> routes;
	
	public RouterList() {
		this.routes = new HashMap<String, Route>();
		
		for (CLASS<? extends OBJECT> request : Runtime.instance().requests()) {
			if (request.hasAttribute(Json.apiSlug.get()) && !request.getAttribute(Json.apiSlug.get()).isEmpty()) {
				String slug = request.getAttribute(Json.apiSlug.get());
				Map<String, String> options = new HashMap<String, String>();
				options.put("class", request.getJavaClass().getName());
				
				if (slug == "user") {
					this.routes.put(String.format("login_%s", slug), new Route(String.format("login_%s", slug), "/" + slug + "/login", "login", new String[] {"POST"}, options));
				}
				
				this.routes.put(String.format("read_all_%s", slug), new Route(String.format("read_all_%s", slug), "/" + slug, "read", new String[] {"GET"}, options));
				this.routes.put(String.format("read_%s", slug), new Route(String.format("read_%s", slug), "/" + slug + "/{recordId}", "read", new String[] {"GET"}, options));
				this.routes.put(String.format("create_%s", slug), new Route(String.format("create_%s", slug), "/" + slug + "", "create", new String[] {"POST"}, options));
				this.routes.put(String.format("copy_%s", slug), new Route(String.format("copy_%s", slug), "/" + slug + "/{recordId}/copy", "copy", new String[] {"POST"}, options));
				this.routes.put(String.format("update_%s", slug), new Route(String.format("update_%s", slug), "/" + slug + "/{recordId}", "update", new String[] {"PUT"}, options));
				this.routes.put(String.format("destroy_%s", slug), new Route(String.format("destroy_%s", slug), "/" + slug + "/{recordId}", "destroy", new String[] {"DELETE"}, options));
				this.routes.put(String.format("report_%s", slug), new Route(String.format("report_%s", slug), "/" + slug + "/{recordId}/report", "report", new String[] {"GET"}, options));
				this.routes.put(String.format("preview_%s", slug), new Route(String.format("preview_%s", slug), "/" + slug + "/{recordId}/preview", "preview", new String[] {"GET"}, options));
				this.routes.put(String.format("action_%s", slug), new Route(String.format("action_%s", slug), "/" + slug + "/{recordId}/action", "action", new String[] {"POST"}, options));
				this.routes.put(String.format("attach_%s", slug), new Route(String.format("attach_%s", slug), "/" + slug + "/{recordId}/attach", "attach", new String[] {"POST"}, options));
				this.routes.put(String.format("detach_%s", slug), new Route(String.format("detach_%s", slug), "/" + slug + "/{recordId}/detach", "detach", new String[] {"POST"}, options));
				this.routes.put(String.format("content_%s", slug), new Route(String.format("content_%s", slug), "/" + slug + "/{recordId}/content", "content", new String[] {"POST"}, options));
			}
		}
	}
	
	/**
	 * @param name
	 * @return IRoute
	 * @throws IllegalArgumentException
	 */
	public IRoute getRouteByName(String name) {
		if (this.routes.containsKey(name)) {
			return this.routes.get(name);
		}
		
		throw new IllegalArgumentException("Invalid route name");
	}
	
	/**
	 * @return
	 */
	public Map<String, Route> getRoutes() {
		return this.routes;
	}
}
