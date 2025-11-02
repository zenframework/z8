package org.zenframework.z8.server.expression;

import java.util.HashMap;
import java.util.Map;

public class DefaultContext implements Context {

	private final Map<String, Variable> variables = new HashMap<String, Variable>();

	@Override
	public Variable getVariable(String name) {
		return variables.get(name);
	}

	public DefaultContext setVariable(Variable value) {
		variables.put(value.getName(), value);
		return this;
	}

	public DefaultContext setVariable(String name, Object value) {
		return setVariable(new Variable(name, value));
	}

	public String getVariablesInfo() {
		return variables.toString();
	}
}
