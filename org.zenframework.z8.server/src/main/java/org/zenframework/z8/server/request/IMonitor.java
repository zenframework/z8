package org.zenframework.z8.server.request;

import org.zenframework.z8.server.types.file;

public interface IMonitor extends IRequestTarget {
	public void log(Throwable exception);

	public void info(String text);
	public void warning(String text);
	public void error(String text);

	public void logInfo(String text);
	public void logWarning(String text);
	public void logError(String text);

	public void print(file file);

	public file getLog();

}
