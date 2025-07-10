package org.zenframework.z8.webserver.resource;

import java.util.Map;

public class SimpleProcessor implements IProcessor{

	@Override
	public String evaluate(String content, Map<String, Object> bindins) {
		for (Map.Entry<String, Object> binding : bindins.entrySet())
			content = content.replace("#{" + binding.getKey() + '}', toString(binding.getValue()));

		return content;
	}

	protected String toString(Object o) {
		return o != null ? o.toString() : null;
	}
}
