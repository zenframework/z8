// Generated from Text.g by ANTLR 4.13.2

package org.zenframework.z8.server.expression.generated;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TextParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TextVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TextParser#text}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitText(TextParser.TextContext ctx);
	/**
	 * Visit a parse tree produced by the {@code plainTextPart}
	 * labeled alternative in {@link TextParser#textPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPlainTextPart(TextParser.PlainTextPartContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expressionTextPart}
	 * labeled alternative in {@link TextParser#textPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionTextPart(TextParser.ExpressionTextPartContext ctx);
}