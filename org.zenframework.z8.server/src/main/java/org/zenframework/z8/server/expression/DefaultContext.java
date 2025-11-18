package org.zenframework.z8.server.expression;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class DefaultContext extends Context {

	private Map<String, Variable> variables = new HashMap<String, Variable>();

	public DefaultContext(Context parent) {
		super(parent);
	}

	public DefaultContext(Context parent, Map<String, Variable> variables) {
		this(parent);
		this.variables.putAll(variables);
	}

	public DefaultContext setVariable(Variable value) {
		variables.put(value.getName(), value);
		return this;
	}

	public DefaultContext setVariable(String name, Object value) {
		return setVariable(new Variable(name, value));
	}

	public DefaultContext freeze() {
		variables = Collections.unmodifiableMap(variables);
		return this;
	}

	public DefaultContext copy() {
		return new DefaultContext(getParent(), variables);
	}

	@Override
	protected Variable getDefinedVariable(String name) {
		return variables.get(name);
	}

	public static DefaultContext create() {
		return new DefaultContext(null)
				.setVariable("bool", bool.class)
				.setVariable("date", date.class)
				.setVariable("datespan", datespan.class)
				.setVariable("decimal", decimal.class)
				.setVariable("guid", guid.class)
				.setVariable("int", integer.class)
				.setVariable("string", string.class);
	}
}
