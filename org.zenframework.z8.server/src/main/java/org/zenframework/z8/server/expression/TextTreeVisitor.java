package org.zenframework.z8.server.expression;

import org.zenframework.z8.server.expression.function.Format;
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
	private final Expression expression;

	public TextTreeVisitor(Expression expression) {
		this.expression = expression;
		this.calculator = expression.getCalculator();
		this.expressionVisitor = new ExpressionTreeVisitor(calculator);
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
			Value expression = expressionVisitor.visit(this.expression.parseExpression(ctx.TEXT().getText()));

			str.append(expression.isEvaluated() ? Format.format(expression.get()) : ExpStart + expression.toString() + ExpFinish);
		} catch (Throwable e) {
			calculator.addError(e.getMessage());
			str.append(ctx.getText());
		}

		return str;
	}
}
