package org.zenframework.z8.server.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorUtils {

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
		StringWriter out = new StringWriter();
		throwable.printStackTrace(new PrintWriter(out));
		return out.toString();
	}

}
