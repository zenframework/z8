package org.zenframework.z8.server.expression;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.json.parser.JsonObject;
import org.zenframework.z8.server.expression.generated.ExpressionLexer;
import org.zenframework.z8.server.expression.generated.ExpressionParser;
import org.zenframework.z8.server.expression.generated.TextLexer;
import org.zenframework.z8.server.expression.generated.TextParser;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Expression extends OBJECT {
	public static class CLASS<T extends OBJECT> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Expression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Expression(container);
		}
	}

	public static interface Extractor {
		void onObject(OBJECT object);
	}

	public static interface Getter {
		Object getValue(Object object);
	}

	public Expression() {
		super(null);
	}

	public Expression(IObject container) {
		super(container);
	}

	private final Map<String, Variable> variables = new HashMap<String, Variable>();
	private final List<String> errors = new LinkedList<String>();

	private Getter getter = null;

	public Expression setVariable(Variable value) {
		variables.put(value.getName(), value);
		return this;
	}

	public Expression setVariable(String name, Object value) {
		return setVariable(new Variable(name, value));
	}

	public Variable getVariable(String name, boolean safely) {
		Variable variable = variables.get(name);

		if (safely && variable == null)
			addError(new MessageFormat(Resources.getByKey("$Expression.variableIsNotSet$")).format(new Object[] { name }));

		return variable;
	}

	public Object getVariableValue(String name, boolean safely) {
		Variable variable = getVariable(name, safely);
		return getValue(variable != null ? variable.getValue() : null);
	}

	public String getVariablesInfo() {
		return variables.toString();
	}

	public List<String> getErrors() {
		return errors;
	}

	public Expression addError(String error) {
		errors.add(error);
		return this;
	}

	public Expression setGetter(Getter getter) {
		this.getter = getter;
		return this;
	}

	public Object getValue(Object value) {
		return getter != null ? getter.getValue(value) : value;
	}

	public Object evaluateExpression(String expression) {
		CharStream charStream = CharStreams.fromString(expression);
		ExpressionLexer lexer = new ExpressionLexer(charStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ExpressionParser parser = new ExpressionParser(tokens);
//		parser.setErrorHandler(new ExpressionErrorHandler());
		ExpressionTreeVisitor visitor = new ExpressionTreeVisitor(this);

		return visitor.visit(parser.rootExpression());
	}

	public String evaluateText(String text) {
		CharStream charStream = CharStreams.fromString(text);
		TextLexer lexer = new TextLexer(charStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		TextParser parser = new TextParser(tokens);
//		parser.setErrorHandler(new ExpressionErrorHandler());
		TextTreeVisitor visitor = new TextTreeVisitor(this);

		return visitor.visit(parser.text()).toString();
	}

	public void extractObjects(String text, Extractor extractor) {
		CharStream charStream = CharStreams.fromString(text);
		TextLexer lexer = new TextLexer(charStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		TextParser parser = new TextParser(tokens);
//		parser.setErrorHandler(new ExpressionErrorHandler());
		ExtractorTreeVisitor visitor = new ExtractorTreeVisitor(this, extractor);

		visitor.visit(parser.text());
	}

	@SuppressWarnings("unchecked")
	public Expression.CLASS<Expression> z8_setVariable(string name, primary value) {
		return (Expression.CLASS<Expression>) setVariable(name.get(), value).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Expression.CLASS<Expression> z8_setVariable(string name, JsonObject.CLASS<? extends JsonObject> value) {
		return (Expression.CLASS<Expression>) setVariable(name.get(), value.get().get()).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Expression.CLASS<Expression> z8_setVariable(string name, JsonArray.CLASS<? extends JsonArray> value) {
		return (Expression.CLASS<Expression>) setVariable(name.get(), value.get().get()).getCLASS();
	}

	public string z8_getVariablesInfo() {
		return new string(getVariablesInfo());
	}

	public RCollection<string> z8_getErrors() {
		RCollection<string> errors = new RCollection<string>(this.errors.size(), false);
		for (String error : this.errors)
			errors.add(new string(error));
		return errors;
	}

	public bool z8_evaluateBoolean(string expression) {
		Object result = evaluateExpression(expression.get());

		if (result instanceof bool)
			return (bool) result;

		throw new IllegalStateException("Boolean result expected: " + result);
	}

	public string z8_evaluateText(string expression) {
		return expression != null ? new string(evaluateText(expression.get())) : null;
	}
}
