package org.zenframework.z8.server.expression;

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

	public static final Context StaticContext = new DefaultContext(null)
			.setVariable("bool", bool.class)
			.setVariable("date", date.class)
			.setVariable("datespan", datespan.class)
			.setVariable("decimal", decimal.class)
			.setVariable("guid", guid.class)
			.setVariable("int", integer.class)
			.setVariable("string", string.class);

	private final Map<String, Variable> variables = new HashMap<String, Variable>();

	public DefaultContext(Context parent) {
		super(parent);
	}

	public DefaultContext setVariable(Variable value) {
		variables.put(value.getName(), value);
		return this;
	}

	public DefaultContext setVariable(String name, Object value) {
		return setVariable(new Variable(name, value));
	}

	@Override
	protected Variable getDefinedVariable(String name) {
		return variables.get(name);
	}
}
