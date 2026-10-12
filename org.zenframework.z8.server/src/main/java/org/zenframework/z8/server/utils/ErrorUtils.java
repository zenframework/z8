package org.zenframework.z8.server.utils;

import org.zenframework.z8.server.types.file;

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
		message = message.replace(exception.getClass().getCanonicalName() + ": ", "");
		return message == null || message.isEmpty() ? "Internal server error." : message;
	}

	static public String getStackTrace(Throwable throwable) {
		StringBuilder result = new StringBuilder(1024).append(String.valueOf(throwable.getMessage()));

		for (StackTraceElement element : throwable.getStackTrace())
			result.append(file.EOL).append('\t').append(element.toString());

		return result.toString();
	}

	static public String toString(StackTraceElement[] stackTrace) {
		if (stackTrace == null || stackTrace.length == 0)
			return "";

		StringBuilder str = new StringBuilder(stackTrace.length * 100);

		for (int i = 1; i < stackTrace.length; i++) {
			if (i > 1)
				str.append('\n');
			str.append("\t at ").append(stackTrace[i].toString());
		}

		return str.toString();
	}
}
