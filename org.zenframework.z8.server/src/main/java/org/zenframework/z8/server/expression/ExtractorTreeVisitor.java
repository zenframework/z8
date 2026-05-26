package org.zenframework.z8.server.expression;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.zenframework.z8.server.expression.generated.ExpressionBaseVisitor;
import org.zenframework.z8.server.expression.generated.ExpressionLexer;
import org.zenframework.z8.server.expression.generated.ExpressionParser;
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

	public ExtractorTreeVisitor(Calculator calculator, Expression.Extractor extractor) {
		this.calculator = calculator;
		this.extractor = extractor;
	}

	@Override
	public Object visitExpressionTextPart(ExpressionTextPartContext ctx) {
		try {
			CharStream charStream = CharStreams.fromString(ctx.TEXT().getText());
			ExpressionLexer lexer = new ExpressionLexer(charStream);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ExpressionParser parser = new ExpressionParser(tokens);

			expressionVisitor.visit(parser.rootExpression());
		} catch (Throwable e) {
			calculator.addError(e.getMessage());
		}

		return null;
	}
}
