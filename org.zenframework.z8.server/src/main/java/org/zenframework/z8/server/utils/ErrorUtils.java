package org.zenframework.z8.server.utils;

public class ErrorUtils {

	static public String getMessage(Throwable exception) {
        Throwable cause = exception; 
        while(true) {
        	cause = exception.getCause();
        	if(cause == null || cause == exception)
        		break;
        	exception = cause;
        }
        
        String message = exception.toString();
		return message == null || message.isEmpty() ? "Internal server error." : message;
	}

	static public String getStackTrace(Throwable throwable) {
		String result = "";

		for(StackTraceElement element : throwable.getStackTrace())
			result += (result.isEmpty() ? "" : "\t") + element.toString() + "\r\n";

		return result;
	}
}
