// Generated from Expression.g by ANTLR 4.13.2

package org.zenframework.z8.server.expression.generated;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#rootExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRootExpression(ExpressionParser.RootExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TernaryExpression}
	 * labeled alternative in {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTernaryExpression(ExpressionParser.TernaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ElvisExpression}
	 * labeled alternative in {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElvisExpression(ExpressionParser.ElvisExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MemberReferenceExpression}
	 * labeled alternative in {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberReferenceExpression(ExpressionParser.MemberReferenceExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BinaryOperatorExpression}
	 * labeled alternative in {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryOperatorExpression(ExpressionParser.BinaryOperatorExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code UnaryOperatorExpression}
	 * labeled alternative in {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryOperatorExpression(ExpressionParser.UnaryOperatorExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PrimaryExpression}
	 * labeled alternative in {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpression(ExpressionParser.PrimaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MethodCallExpression}
	 * labeled alternative in {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCallExpression(ExpressionParser.MethodCallExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SquareBracketExpression}
	 * labeled alternative in {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSquareBracketExpression(ExpressionParser.SquareBracketExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#expressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionList(ExpressionParser.ExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#methodCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCall(ExpressionParser.MethodCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(ExpressionParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#priority}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPriority(ExpressionParser.PriorityContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(ExpressionParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(ExpressionParser.IdentifierContext ctx);
}