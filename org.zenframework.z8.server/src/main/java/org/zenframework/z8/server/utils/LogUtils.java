package org.zenframework.z8.server.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.zenframework.z8.server.logs.Trace;

public class LogUtils {

	private static final int FileLimitDefault = 10 * 1024 * 1024;
	private static final int FileCountDefault = 10;

	public static class Builder {

		private String name;
		private File logFile;
		private Formatter formatter;
		private int fileLimit = FileLimitDefault;
		private int fileCount = FileCountDefault;

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setLogFile(File logFile) {
			this.logFile = logFile;
			return this;
		}

		public Builder setLogFormat(String logFormat) {
			return setFormatter(new SimpleFormatter() {
				@Override
				public synchronized String format(LogRecord record) {
					return String.format(logFormat, new Date(record.getMillis()), formatSource(record),
							record.getLoggerName(), record.getLevel(), formatMessage(record), formatThrowable(record));
				}
			});
		}

		public Builder setFormatter(Formatter formatter) {
			this.formatter = formatter;
			return this;
		}

		public Builder setFileLimit(int fileLimit) {
			this.fileLimit = fileLimit;
			return this;
		}

		public Builder setFileCount(int fileCount) {
			this.fileCount = fileCount;
			return this;
		}

		public Logger build() {
			if (logFile == null || formatter == null || name == null)
				return null;

			try {
				prepareLogFolder(logFile.getParentFile());
				Handler handler = new FileHandler(logFile.getPath(), fileLimit, fileCount, true);
				handler.setFormatter(formatter);
				Logger logger = Logger.getLogger(name);
				logger.setUseParentHandlers(false);
				logger.addHandler(handler);
				return logger;
			} catch (IOException e) {
				Trace.logError("Can't initialize log '" + name + "'", e);
				return null;
			}
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	public static void close(Logger logger) {
		if (logger == null)
			return;

		Handler[] handlers = logger.getHandlers();

		for (Handler handler : handlers) {
			try {
				handler.flush(); // Force write remaining buffered logs
				handler.close(); // Close files/sockets and release resources
				logger.removeHandler(handler);
			} catch (Throwable e) {}
		}

		logger.setUseParentHandlers(false);
		logger.setLevel(Level.OFF);
	}

	private static void prepareLogFolder(File folder) {
		folder.mkdirs();
		for (File file : folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".lck");
			}
		}))
			file.delete();
	}

	private static String formatSource(LogRecord record) {
		if (record.getSourceClassName() == null)
			return record.getLoggerName();

		String source = record.getSourceClassName();

		if (record.getSourceMethodName() != null)
			source += " " + record.getSourceMethodName();

		return source;
	}

	private static String formatThrowable(LogRecord record) {
		if (record.getThrown() == null)
			return "";

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println();
		record.getThrown().printStackTrace(pw);
		pw.close();
		return sw.toString();
	}

}
