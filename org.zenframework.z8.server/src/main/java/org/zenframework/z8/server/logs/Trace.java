package org.zenframework.z8.server.logs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.exceptions.ThreadInterruptedException;
import org.zenframework.z8.server.utils.ErrorUtils;

public class Trace {
	private static final Log log = LogFactory.getLog("Z8");

	static public void logEvent(Object message) {
		log.info("\n\t" + message);
	}

	static public void logEvent(Throwable exception) {
		log.info("\n\t" + ErrorUtils.getMessage(exception));
	}

	static public void logError(Throwable exception) {
		if(exception instanceof AccessDeniedException || exception instanceof ThreadInterruptedException)
			return;
		log.error(getCause(exception));
	}

	static public void logError(String message, Throwable exception) {
		if(exception instanceof AccessDeniedException || exception instanceof ThreadInterruptedException)
			return;
		log.error("\n\t" + message, getCause(exception));
	}
	
	static private Throwable getCause(Throwable exception) {
		if(exception instanceof RuntimeException) {
			Throwable cause = exception.getCause();
			if(cause == null || exception == cause)
				return exception;
			return getCause(cause); 
		}
		return exception;
	}
}
