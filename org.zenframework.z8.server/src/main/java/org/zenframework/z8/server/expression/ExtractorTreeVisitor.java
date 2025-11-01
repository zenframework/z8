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
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.OBJECT;

public class ExtractorTreeVisitor extends TextBaseVisitor<Object> {
	private class ExpressionTreeVisitor extends ExpressionBaseVisitor<Object> {
		@Override
		@SuppressWarnings("rawtypes")
		public Object visitMemberReferenceExpression(MemberReferenceExpressionContext ctx) {
			Object object = ctx.property != null ? visit(ctx.object) : null;
			Object property = object != null ? Operator.getProperty(object, ctx.property.getText()) : null;

			if (property instanceof CLASS)
				property = ((CLASS) property).get();

			if (property instanceof OBJECT)
				extractor.onObject((OBJECT) property);

			return property;
		}

		@Override
		public Object visitIdentifier(IdentifierContext ctx) {
			return expression.getVariableValue(ctx.getText(), false);
		}
	}

	private final ExpressionTreeVisitor expressionVisitor = new ExpressionTreeVisitor();

	private final Expression expression;
	private final Expression.Extractor extractor;

	public ExtractorTreeVisitor(Expression expression, Expression.Extractor extractor) {
		this.expression = expression;
		this.extractor = extractor;
	}

	@Override
	public Object visitExpressionTextPart(ExpressionTextPartContext ctx) {
		try {
			CharStream charStream = CharStreams.fromString(ctx.TEXT().getText());
			ExpressionLexer lexer = new ExpressionLexer(charStream);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ExpressionParser parser = new ExpressionParser(tokens);

			expressionVisitor.visit(parser.expression());
		} catch (Throwable e) {
			expression.addError(e.getMessage());
		}

		return null;
	}
}
