package org.zenframework.z8.server.expression;

import org.zenframework.z8.server.expression.generated.ExpressionBaseVisitor;
import org.zenframework.z8.server.expression.generated.ExpressionParser.IdentifierContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.MemberReferenceExpressionContext;
import org.zenframework.z8.server.expression.generated.TextBaseVisitor;
import org.zenframework.z8.server.expression.generated.TextParser.ExpressionTextPartContext;
import org.zenframework.z8.server.runtime.OBJECT;

public class ExtractorTreeVisitor extends TextBaseVisitor<Object> {
	private class ExpressionTreeVisitor extends ExpressionBaseVisitor<Value> {
		@Override
		public Value visitMemberReferenceExpression(MemberReferenceExpressionContext ctx) {
			Value object = ctx.property != null ? visit(ctx.object) : null;
			String property = ctx.property.getText();
			Object value = object != null ? calculator.getProperty(object.get(), property) : null;

			if (value instanceof OBJECT)
				extractor.onObject((OBJECT) value);

			return new Value().setValue(value);
		}

		@Override
		public Value visitIdentifier(IdentifierContext ctx) {
			return calculator.getVariableValue(ctx.getText());
		}
	}

	private final ExpressionTreeVisitor expressionVisitor = new ExpressionTreeVisitor();

	private final Calculator calculator;
	private final Expression.Extractor extractor;
	private final Expression expression;

	public ExtractorTreeVisitor(Expression expression, Expression.Extractor extractor) {
		this.expression = expression;
		this.calculator = expression.getCalculator();
		this.extractor = extractor;
	}

	@Override
	public Object visitExpressionTextPart(ExpressionTextPartContext ctx) {
		try {
			expressionVisitor.visit(expression.parseExpression(ctx.TEXT().getText()));
		} catch (Throwable e) {
			calculator.addError(e.getMessage());
		}

		return null;
	}
}
