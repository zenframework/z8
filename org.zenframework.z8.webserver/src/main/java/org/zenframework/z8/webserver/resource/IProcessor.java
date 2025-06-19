package org.zenframework.z8.webserver.resource;

import java.util.Map;

public interface IProcessor {

	String evaluate(String content, Map<String, Object> bindins);

}
