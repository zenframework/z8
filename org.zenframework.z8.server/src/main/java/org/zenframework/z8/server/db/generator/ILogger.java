package org.zenframework.z8.server.db.generator;

public interface ILogger {
	public void info(String msg);
	public void warning(String msg);

	public void error(Throwable exception);

	public void error(Throwable exception, String msg);

	public void progress(int percentDone);
}
