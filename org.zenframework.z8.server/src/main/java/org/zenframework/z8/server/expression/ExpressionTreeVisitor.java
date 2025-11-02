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

public class ExpressionTreeVisitor extends ExpressionBaseVisitor<Value> {

	private static final char LeftIndex = '[';
	private static final char RightIndex = ']';

	private static final char LeftBracket = '(';
	private static final char RightBracket = ')';

	private static final char Member = '.';

	private static final char Question = '?';
	private static final char Colon = ':';

	private static final String Elvis = "?:";

	private final Calculator calculator;

	public ExpressionTreeVisitor(Calculator calculator) {
		this.calculator = calculator;
	}

	@Override
	public Value visitRootExpression(RootExpressionContext ctx) {
		return visit(ctx.getChild(0));
	}

	@Override
	public Value visitMemberReferenceExpression(MemberReferenceExpressionContext ctx) {
		Value object = visit(ctx.object);

		if (ctx.property != null) {
			String property = ctx.property.getText();

			if (object.isEvaluated())
				return new Value().setValue(calculator.getProperty(object.get(), property));

			return new Value().setExpression(object.toString() + Member + property);

		} else if (ctx.method != null) {
			MethodCallContext methodCall = ctx.methodCall();
			String function = methodCall.function.getText();
			Value[] arguments = evaluateArguments(methodCall);

			if (object.isEvaluated() && Value.isEvaluated(arguments))
				return new Value().setValue(calculator.callMethod(object.get(), function, Value.get(arguments)));

			return new Value().setExpression(object.toString() + Member + function + LeftBracket + Value.toString(arguments) + RightBracket);
		}

		throw new IllegalStateException("[null] member");
	}

	@Override
	public Value visitBinaryOperatorExpression(BinaryOperatorExpressionContext ctx) {
		Value left = visit(ctx.left);
		Value right = visit(ctx.right);
		Operator op = Operator.parse(ctx.op);

		if (left.isEvaluated() && right.isEvaluated())
			return new Value().setValue(calculator.binary(left.get(), right.get(), op));

		return new Value().setExpression(left.toString() + ' ' + op + ' ' + right.toString());
	}

	@Override
	public Value visitUnaryOperatorExpression(UnaryOperatorExpressionContext ctx) {
		Value expression = visit(ctx.expression());
		Operator op = Operator.parse(ctx.prefix);

		if (expression.isEvaluated())
			return new Value().setValue(calculator.unary(expression.get(), op));

		return new Value().setExpression((op != null ? op.toString() : "") + expression.toString());
	}

	@Override
	public Value visitMethodCallExpression(MethodCallExpressionContext ctx) {
		MethodCallContext methodCall = ctx.methodCall();
		String function = methodCall.function.getText();
		Value[] arguments = evaluateArguments(methodCall);

		if (Value.isEvaluated(arguments))
			return new Value().setValue(calculator.callFunction(function, Value.get(arguments)));

		return new Value().setExpression(function + LeftBracket + Value.toString(arguments) + RightBracket);
	}

	@Override
	public Value visitSquareBracketExpression(SquareBracketExpressionContext ctx) {
		Value array = visit(ctx.array);
		Value index = visit(ctx.index);

		if (array.isEvaluated() && index.isEvaluated())
			return new Value().setValue(calculator.getItem(array.get(), index.get()));

		return new Value().setExpression(array.toString() + LeftIndex + index.toString() + RightIndex);
	}

	@Override
	public Value visitTernaryExpression(TernaryExpressionContext ctx) {
		Value condition = visit(ctx.condition);

		if (condition.isEvaluated())
			return Calculator.isTrue(condition.get()) ? visit(ctx.trueExp) : visit(ctx.falseExp);

		Value trueExp = visit(ctx.trueExp);
		Value falseExp = visit(ctx.falseExp);

		return new Value().setExpression(condition.toString() + ' ' + Question + trueExp.toString() + ' ' + Colon + ' ' + falseExp.toString());
	}

	@Override
	public Value visitElvisExpression(ElvisExpressionContext ctx) {
		Value value = visit(ctx.value);

		if (value.isEvaluated() && value.get() != null)
			return value;

		Value alternative = visit(ctx.alternative);

		if (!value.isEvaluated())
			return new Value().setExpression(value.toString() + ' ' + Elvis + ' ' + alternative.toString());

		return alternative;
	}

	@Override
	public Value visitPriority(PriorityContext ctx) {
		Value expression = visit(ctx.expression());
		return expression.isEvaluated() ? expression : new Value().setExpression(LeftBracket + expression.toString() + RightBracket);
	}

	@Override
	public Value visitLiteral(LiteralContext ctx) {
		if (ctx.integer != null)
			return new Value().setValue(new integer(ctx.integer.getText()));
		if (ctx.decimal != null)
			return new Value().setValue(new decimal(ctx.decimal.getText()));
		if (ctx.string != null)
			return new Value().setValue(new string(unquote(ctx.string.getText())));
		if (ctx.bool != null)
			return new Value().setValue(new bool(ctx.bool.getText()));
		return null;
	}

	@Override
	public Value visitIdentifier(IdentifierContext ctx) {
		return calculator.getVariableValue(ctx.getText());
	}

	private Value[] evaluateArguments(MethodCallContext methodCall) {
		ExpressionListContext argumentsList = methodCall.arguments;
		List<ExpressionContext> arguments = argumentsList != null ? argumentsList.expression() : Collections.emptyList();
		Value[] values = new Value[arguments.size()];
		int index = 0;

		for (ExpressionContext argument : arguments)
			values[index++] = visit(argument);

		return values;
	}

	private static String unquote(String value) {
		return value != null ? value.substring(1, value.length() - 1) : null;
	}
}
