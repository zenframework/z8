package org.zenframework.z8.server.expression;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.zenframework.z8.server.expression.function.Format;
import org.zenframework.z8.server.expression.generated.ExpressionLexer;
import org.zenframework.z8.server.expression.generated.ExpressionParser;
import org.zenframework.z8.server.expression.generated.TextBaseVisitor;
import org.zenframework.z8.server.expression.generated.TextParser.ExpressionTextPartContext;
import org.zenframework.z8.server.expression.generated.TextParser.PlainTextPartContext;
import org.zenframework.z8.server.expression.generated.TextParser.TextContext;

public class TextTreeVisitor extends TextBaseVisitor<StringBuilder> {

	private static final String ExpStart = "${";
	private static final String ExpFinish = "}";

	private final StringBuilder str = new StringBuilder();
	private final ExpressionTreeVisitor expressionVisitor;
	private final Calculator calculator;

	public TextTreeVisitor(Calculator calculator) {
		this.expressionVisitor = new ExpressionTreeVisitor(calculator);
		this.calculator = calculator;
	}

	@Override
	public StringBuilder visitText(TextContext ctx) {
		super.visitText(ctx);
		return str;
	}

	@Override
	public StringBuilder visitPlainTextPart(PlainTextPartContext ctx) {
		return str.append(ctx.getText());
	}

	@Override
	public StringBuilder visitExpressionTextPart(ExpressionTextPartContext ctx) {
		try {
			CharStream charStream = CharStreams.fromString(ctx.TEXT().getText());
			ExpressionLexer lexer = new ExpressionLexer(charStream);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ExpressionParser parser = new ExpressionParser(tokens);

			Value expression = expressionVisitor.visit(parser.rootExpression());

			str.append(expression.isEvaluated() ? Format.format(expression.get()) : ExpStart + expression.toString() + ExpFinish);
		} catch (Throwable e) {
			calculator.addError(e.getMessage());
			str.append(ctx.getText());
		}

		return str;
	}
}
