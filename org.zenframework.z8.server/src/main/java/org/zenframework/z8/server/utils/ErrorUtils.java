package org.zenframework.z8.server.utils;

public class ErrorUtils {

	static public String getMessage(Throwable e) {
		StringBuilder message = new StringBuilder();
		for (; e != null && e.getCause() != e; e = e.getCause()) {
			String s = e.getMessage();
			if (s != null && !s.isEmpty())
				message.append(s).append(" <-- ");
		}
		if (message.length() > 4)
			message.setLength(message.length() - 5);
		return message.length() == 0 ? "Internal server error." : message.toString();
	}

	static public String getStackTrace(Throwable throwable) {
		StringBuilder stackTrace = new StringBuilder();

		for (StackTraceElement element : throwable.getStackTrace())
			stackTrace.append("\t" + element.toString() + "\r\n");

		return stackTrace.toString();
	}

}
