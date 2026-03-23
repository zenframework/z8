package org.zenframework.z8.server.base.eval;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.json.parser.JsonObject;
import org.zenframework.z8.server.expression.DefaultContext;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Eval extends OBJECT {
	public static class CLASS<T extends OBJECT> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Eval.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Eval(container);
		}
	}

	private final DefaultContext context = DefaultContext.create();
	private final org.zenframework.z8.server.expression.Expression expression = new org.zenframework.z8.server.expression.Expression().setContext(context);

	public Eval(IObject container) {
		super(container);
	}

	public void z8_setVariable(string name, primary value) {
		context.setVariable(name.get(), value);
	}

	public void z8_setVariable(string name, JsonObject.CLASS<? extends JsonObject> value) {
		context.setVariable(name.get(), value.get().get());
	}

	public void z8_setVariable(string name, JsonArray.CLASS<? extends JsonArray> value) {
		context.setVariable(name.get(), value.get().get());
	}

	public string z8_getContextInfo() {
		return new string(context.toString());
	}

	public RCollection<string> z8_getErrors() {
		RCollection<string> errors = new RCollection<string>(expression.getErrors().size(), false);
		for (String error : expression.getErrors())
			errors.add(new string(error));
		return errors;
	}

	public bool z8_evaluateBoolean(string expression) {
		Object result = this.expression.evaluateExpression(expression.get());

		if (result instanceof bool)
			return (bool) result;

		throw new IllegalStateException("Boolean result expected: " + result);
	}

	public string z8_evaluateText(string expression) {
		Object result = this.expression.evaluateExpression(expression.get());
		return new string(result == null ? "null" : result.toString());
	}
}
