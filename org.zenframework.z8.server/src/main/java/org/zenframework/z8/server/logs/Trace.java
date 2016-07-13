package org.zenframework.z8.server.logs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.utils.ErrorUtils;

public class Trace {
    private static final Log LOG = LogFactory.getLog("Z8");
    
    static public void logEvent(Object message) {
        LOG.info("\n\t" + message);
    }

    static public void logEvent(Throwable exception) {
        LOG.info("\n\t" + ErrorUtils.getMessage(exception));
    }

    static public void logError(Throwable exception) {
        if (exception instanceof AccessDeniedException)
            return;
        LOG.error("\n\t" + exception, exception);
    }

    static public void logError(Object message, Throwable exception) {
        if (exception instanceof AccessDeniedException)
            return;
        LOG.error("\n\t" + message, exception);
    }
}
