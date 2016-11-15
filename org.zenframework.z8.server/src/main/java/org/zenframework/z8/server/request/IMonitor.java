package org.zenframework.z8.server.request;

import org.zenframework.z8.server.types.file;

public interface IMonitor extends IRequestTarget {
	public void log(String text);

	public void log(Throwable exception);

	public void print(String text);

	public void print(file file);

	public file getLog();

}
