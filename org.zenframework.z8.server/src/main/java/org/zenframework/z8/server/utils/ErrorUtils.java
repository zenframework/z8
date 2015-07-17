package org.zenframework.z8.server.utils;

public class ErrorUtils {
    static public String getMessage(Throwable throwable) {
        String message = throwable.getMessage();

        if(message == null || message.isEmpty()) {
            message = "Internal server error.";
        }
        
        return message;
    }

    static public String getStackTrace(Throwable throwable) {
        StringBuilder stackTrace = new StringBuilder();

        for(StackTraceElement element : throwable.getStackTrace())
        {
            stackTrace.append("\t" + element.toString() + "\r\n");
        }
        
        return stackTrace.toString();
    }
}
