package org.zenframework.z8.server.expression;

import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.zenframework.z8.server.expression.generated.ExpressionLexer;
import org.zenframework.z8.server.expression.generated.ExpressionParser;
import org.zenframework.z8.server.expression.generated.TextLexer;
import org.zenframework.z8.server.expression.generated.TextParser;
import org.zenframework.z8.server.runtime.OBJECT;

public class Expression {
	public static interface Extractor {
		void onObject(OBJECT object);
	}

	public static interface Getter {
		Object getValue(Object object);
	}

	private final Calculator calculator = new Calculator();

	public Calculator getCalculator() {
		return calculator;
	}

	public Expression setGetter(Getter getter) {
		calculator.setGetter(getter);
		return this;
	}

	public Expression setContext(Context context) {
		calculator.setContext(context);
		return this;
	}

	public List<String> getErrors() {
		return calculator.getErrors();
	}

	public Expression clear() {
		calculator.clear();
		return this;
	}

	public Value evaluateExpression(String expression) {
		return evaluateExpression(expression, false);
	}

	public Value evaluateExpression(String expression, boolean silent) {
		CharStream charStream = CharStreams.fromString(expression);
		ExpressionLexer lexer = new ExpressionLexer(charStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ExpressionParser parser = new ExpressionParser(tokens);
//		parser.setErrorHandler(new ExpressionErrorHandler());
		ExpressionTreeVisitor visitor = new ExpressionTreeVisitor(calculator.setSilent(silent));

		return visitor.visit(parser.rootExpression());
	}

	public String evaluateText(String text) {
		return evaluateText(text, false);
	}

	public String evaluateText(String text, boolean silent) {
		CharStream charStream = CharStreams.fromString(text);
		TextLexer lexer = new TextLexer(charStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		TextParser parser = new TextParser(tokens);
//		parser.setErrorHandler(new ExpressionErrorHandler());
		TextTreeVisitor visitor = new TextTreeVisitor(calculator.setSilent(silent));

		return visitor.visit(parser.text()).toString();
	}

	public void extractObjects(String text, Extractor extractor) {
		CharStream charStream = CharStreams.fromString(text);
		TextLexer lexer = new TextLexer(charStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		TextParser parser = new TextParser(tokens);
//		parser.setErrorHandler(new ExpressionErrorHandler());
		ExtractorTreeVisitor visitor = new ExtractorTreeVisitor(calculator.setSilent(true), extractor);

		visitor.visit(parser.text());
	}
}
