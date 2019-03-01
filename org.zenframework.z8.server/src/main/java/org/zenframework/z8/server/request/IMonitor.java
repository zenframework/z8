package org.zenframework.z8.server.request;

import java.util.Collection;

import org.zenframework.z8.server.types.file;

public interface IMonitor extends IRequestTarget {
	public void info(String text);
	public void warning(String text);
	public void error(String text);
	public void error(Throwable text);

	public void logInfo(String text);
	public void logWarning(String text);
	public void logError(String text);
	public void logError(Throwable text);

	public void print(file file);

	public Collection<file> getFiles();
	public file getLog();

	public boolean hasErrors();
}
