package org.zenframework.z8.server.utils;

public class ErrorUtils {

	static public Throwable unwrapRuntimeException(Throwable exception) {
		while(exception instanceof RuntimeException) {
			Throwable cause = exception.getCause();
			if(cause instanceof RuntimeException)
				exception = cause;
			else
				return cause == null ? exception : cause;
		}
		return exception;
	}

	static public Throwable getCause(Throwable exception) {
		Throwable cause = exception;

		while(true) {
			cause = exception.getCause();
			if(cause == null || cause == exception)
				return exception;
			exception = cause;
		}
	}

	static public String getMessage(Throwable exception) {
		String message = getCause(exception).toString();
		return message == null || message.isEmpty() ? "Internal server error." : message;
	}

	static public String getStackTrace(Throwable throwable) {
		String result = "";

		for(StackTraceElement element : throwable.getStackTrace())
			result += (result.isEmpty() ? "" : "\t") + element.toString() + "\r\n";

		return result;
	}
}
