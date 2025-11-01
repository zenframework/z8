package org.zenframework.z8.server.expression;

import java.util.Collections;
import java.util.List;

import org.zenframework.z8.server.expression.generated.ExpressionBaseVisitor;
import org.zenframework.z8.server.expression.generated.ExpressionParser.BinaryOperatorExpressionContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.ElvisExpressionContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.ExpressionContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.ExpressionListContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.IdentifierContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.LiteralContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.MemberReferenceExpressionContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.MethodCallContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.MethodCallExpressionContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.PriorityContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.RootExpressionContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.SquareBracketExpressionContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.TernaryExpressionContext;
import org.zenframework.z8.server.expression.generated.ExpressionParser.UnaryOperatorExpressionContext;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class ExpressionTreeVisitor extends ExpressionBaseVisitor<Object> {

	private final Expression expression;

	public ExpressionTreeVisitor(Expression expression) {
		this.expression = expression;
	}

	@Override
	public Object visitRootExpression(RootExpressionContext ctx) {
		return visit(ctx.getChild(0));
	}

	@Override
	public Object visitMemberReferenceExpression(MemberReferenceExpressionContext ctx) {
		Object value = null;

		if (ctx.property != null) {
			value = Operator.getProperty(visit(ctx.object), ctx.property.getText());
		} else if (ctx.method != null) {
			MethodCallContext methodCall = ctx.methodCall();
			value = Operator.callMethod(visit(ctx.object), methodCall.function.getText(), evaluateArguments(methodCall));
		}

		return expression.getValue(value);
	}

	@Override
	public Object visitBinaryOperatorExpression(BinaryOperatorExpressionContext ctx) {
		return expression.getValue(Operator.binary(visit(ctx.left), visit(ctx.right), Operator.parse(ctx.op)));
	}

	@Override
	public Object visitUnaryOperatorExpression(UnaryOperatorExpressionContext ctx) {
		return expression.getValue(Operator.unary(visit(ctx.expression()), Operator.parse(ctx.prefix.getText())));
	}

	@Override
	public Object visitMethodCallExpression(MethodCallExpressionContext ctx) {
		MethodCallContext methodCall = ctx.methodCall();
		return expression.getValue(Functions.call(methodCall.function.getText(), evaluateArguments(methodCall)));
	}

	@Override
	public Object visitSquareBracketExpression(SquareBracketExpressionContext ctx) {
		return expression.getValue(Operator.index(visit(ctx.array), visit(ctx.index)));
	}

	@Override
	public Object visitTernaryExpression(TernaryExpressionContext ctx) {
		return expression.getValue(Operator.isTrue(expression.getValue(visit(ctx.condition)))
				? visit(ctx.trueExp) : visit(ctx.falseExp));
	}

	@Override
	public Object visitElvisExpression(ElvisExpressionContext ctx) {
		Object value = expression.getValue(visit(ctx.value));
		return value != null ? value : expression.getValue(visit(ctx.alternative));
	}

	@Override
	public Object visitPriority(PriorityContext ctx) {
		return visit(ctx.expression());
	}

	@Override
	public Object visitLiteral(LiteralContext ctx) {
		if (ctx.integer != null)
			return new integer(ctx.integer.getText());
		if (ctx.decimal != null)
			return new decimal(ctx.decimal.getText());
		if (ctx.string != null)
			return new string(unquote(ctx.string.getText()));
		if (ctx.bool != null)
			return new bool(ctx.bool.getText());
		return null;
	}

	@Override
	public Object visitIdentifier(IdentifierContext ctx) {
		return expression.getVariableValue(ctx.getText(), true);
	}

	private Object[] evaluateArguments(MethodCallContext methodCall) {
		ExpressionListContext argumentsList = methodCall.arguments;
		List<ExpressionContext> arguments = argumentsList != null ? argumentsList.expression() : Collections.emptyList();
		Object[] values = new Object[arguments.size()];
		int index = 0;

		for (ExpressionContext argument : arguments)
			values[index++] = visit(argument);

		return values;
	}

	private static String unquote(String value) {
		return value != null ? value.substring(1, value.length() - 1) : null;
	}
}
