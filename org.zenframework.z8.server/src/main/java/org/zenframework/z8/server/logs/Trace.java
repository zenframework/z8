package org.zenframework.z8.server.logs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.exceptions.ThreadInterruptedException;

public class Trace {
	private static final Log log = LogFactory.getLog("Z8");

	static public void debug(Object message) {
		log.debug(message);
	}

	static public void debug(Object message, Throwable exception) {
		log.debug(message, exception);
	}

	static public void logEvent(Object message) {
		log.info(message);
	}

	static public void logError(String message, Throwable exception) {
		if(exception instanceof AccessDeniedException || exception instanceof ThreadInterruptedException)
			return;
		log.error(message, exception);
	}

	static public void logError(Throwable exception) {
		logError(exception.getMessage(), exception);
	}
}
