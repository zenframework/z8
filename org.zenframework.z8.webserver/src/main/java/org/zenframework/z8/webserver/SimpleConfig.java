package org.zenframework.z8.webserver;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class SimpleConfig implements ServletConfig, FilterConfig {

	private final String name;
	private final ServletContext context;
	private final Map<String, String> initParameters;

	public SimpleConfig(String name, ServletContext context, Map<String, String> initParameters) {
		this.name = name;
		this.context = context;
		this.initParameters = initParameters;
	}

	@Override
	public String getServletName() {
		return name;
	}

	@Override
	public String getFilterName() {
		return name;
	}

	@Override
	public String getInitParameter(String var1) {
		return initParameters.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		Iterator<String> names = initParameters.keySet().iterator();
		return new Enumeration<String>() {
			@Override
			public boolean hasMoreElements() {
				return names.hasNext();
			}

			@Override
			public String nextElement() {
				return names.next();
			}
		};
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

}
