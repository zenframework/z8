package org.zenframework.z8.server.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private final Map<String, TextParser.TextContext> textTrees = new HashMap<String, TextParser.TextContext>();
	private final Map<String, ExpressionParser.RootExpressionContext> expressionTrees = new HashMap<String, ExpressionParser.RootExpressionContext>();

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
		ExpressionTreeVisitor visitor = new ExpressionTreeVisitor(calculator.setSilent(silent));

		return visitor.visit(parseExpression(expression));
	}

	public Object evaluateText(String text) {
		return evaluateText(text, false);
	}

	public Object evaluateText(String text, boolean silent) {
		TextParser.TextContext tree = parseText(text);
		List<TextParser.TextPartContext> parts = tree.textPart();

		if (parts.size() == 1 && parts.get(0) instanceof TextParser.ExpressionTextPartContext) {
			String exprContent = ((TextParser.ExpressionTextPartContext) parts.get(0)).TEXT().getText();
			Value value = evaluateExpression(exprContent, silent);
			return value.isEvaluated() ? value.get() : value.toString();
		}

		calculator.setSilent(silent);
		TextTreeVisitor visitor = new TextTreeVisitor(this);
		return visitor.visit(tree).toString();
	}

	public void extractObjects(String text, Extractor extractor) {
		calculator.setSilent(true);
		ExtractorTreeVisitor visitor = new ExtractorTreeVisitor(this, extractor);

		visitor.visit(parseText(text));
	}

	ExpressionParser.RootExpressionContext parseExpression(String expression) {
		ExpressionParser.RootExpressionContext tree = expressionTrees.get(expression);
		if (tree != null)
			return tree;

		CharStream charStream = CharStreams.fromString(expression);
		ExpressionLexer lexer = new ExpressionLexer(charStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ExpressionParser parser = new ExpressionParser(tokens);
		tree = parser.rootExpression();
		expressionTrees.put(expression, tree);
		return tree;
	}

	private TextParser.TextContext parseText(String text) {
		TextParser.TextContext tree = textTrees.get(text);
		if (tree != null)
			return tree;

		CharStream charStream = CharStreams.fromString(text);
		TextLexer lexer = new TextLexer(charStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		TextParser parser = new TextParser(tokens);
		tree = parser.text();
		textTrees.put(text, tree);
		return tree;
	}
}
