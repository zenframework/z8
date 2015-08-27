package org.zenframework.z8.server.logs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zenframework.z8.server.exceptions.AccessDeniedException;

public class Trace {
    
    private static final Log LOG = LogFactory.getLog("Z8");
    
    static public void logEvent(Object message) {
        LOG.info(message);
    }

    static public void logError(Throwable error) {
        if (error instanceof AccessDeniedException)
            return;

        LOG.error(error, error);
    }

    static public void logError(Object message, Throwable error) {
        if (error instanceof AccessDeniedException)
            return;

        LOG.error(message, error);
    }

}
