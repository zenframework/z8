package org.zenframework.z8.server.db.generator;

public interface ILogger {
    public void message(String msg);

    public void error(Throwable exception);

    public void error(Throwable exception, String msg);

    public void progress(int percentDone);
}
