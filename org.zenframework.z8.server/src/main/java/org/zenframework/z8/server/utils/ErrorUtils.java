package org.zenframework.z8.server.utils;

public class ErrorUtils {

	static public String getMessage(Throwable exception) {
        String message = "";

        do {
        	if(exception.getMessage() != null)
        		message += (message.isEmpty() ? "" : "\n\t") + exception.getMessage();
        	
        	Throwable cause = exception.getCause();

        	if(cause == null || cause == exception)
        		break;
        	
        	exception = cause;
        } while(true);
		
		return message.length() == 0 ? "Internal server error." : message.toString();
	}

	static public String getStackTrace(Throwable throwable) {
		String result = "";

		for(StackTraceElement element : throwable.getStackTrace())
			result += (result.isEmpty() ? "" : "\t") + element.toString() + "\r\n";

		return result;
	}
}
