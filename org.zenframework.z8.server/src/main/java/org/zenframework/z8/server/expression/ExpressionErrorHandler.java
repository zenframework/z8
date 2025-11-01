package org.zenframework.z8.server.expression;

import org.antlr.v4.runtime.DefaultErrorStrategy;

public class ExpressionErrorHandler extends DefaultErrorStrategy {
/*
	@Override
	public void recover(Parser parser, RecognitionException e) {
		log("recover: " + e.getMessage());
		//super.recover(parser, e);
		throw new RuntimeException(e);
	}

	@Override
	public void reportError(Parser parser, RecognitionException e) {
		log("reportError: " + e.getMessage());
		super.reportError(parser, e);
	}

	private static void log(String message) {
		System.out.println(message);
	}
*/
}
