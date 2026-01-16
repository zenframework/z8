package org.zenframework.z8.server.logs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.zenframework.z8.server.base.Executable;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.utils.DatespanParser;

public class LogCleanupJob extends Executable {

	private File systemLogsDirectory;
	private String systemLogsRegex;
	private long keepForMillis;

	public static class CLASS<T extends LogCleanupJob> extends Executable.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(LogCleanupJob.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new LogCleanupJob(container);
		}
	}

	public LogCleanupJob(IObject container) {
		super(container);
		useTransaction = bool.False;

		String keepFor = ServerConfig.logCleanupJobKeepFor();
		this.keepForMillis = DatespanParser.parseToMilliseconds(keepFor);

		initializeSystemLogsConfiguration();
	}

	private void initializeSystemLogsConfiguration() {
		File loggingPropertiesFile = new File(ServerConfig.configPath(), "logging.properties");

		if (!loggingPropertiesFile.exists()) {
			this.systemLogsDirectory = null;
			this.systemLogsRegex = null;
			return;
		}

		try (FileInputStream fis = new FileInputStream(loggingPropertiesFile)) {
			Properties props = new Properties();
			props.load(fis);

			String pattern = props.getProperty("java.util.logging.FileHandler.pattern");

			if (pattern == null || pattern.isEmpty()) {
				this.systemLogsDirectory = null;
				this.systemLogsRegex = null;
				return;
			}

			String expandedPath = pattern.replace("%h", java.lang.System.getProperty("user.home")).replace("%t",
					java.lang.System.getProperty("java.io.tmpdir"));

			File file = new File(expandedPath);
			this.systemLogsDirectory = file.getParentFile();

			String filePattern = file.getName();
			this.systemLogsRegex = filePattern.replaceAll("%u", "\\\\d+").replaceAll("%g", "\\\\d+");

		} catch (IOException e) {
			Trace.logError("Failed to parse system logs configuration", e);
			this.systemLogsDirectory = null;
			this.systemLogsRegex = null;
		}
	}

	@Override
	protected void z8_execute(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
		long cutoffTime = java.lang.System.currentTimeMillis() - keepForMillis;

		int totalDeleted = 0;

		int jobLogsDeleted = cleanupJobLogs(cutoffTime);
		totalDeleted += jobLogsDeleted;

		int systemLogsDeleted = cleanupSystemLogs(cutoffTime);
		totalDeleted += systemLogsDeleted;

		Trace.logEvent("Log cleanup finished. Total deleted: " + totalDeleted + " files.");
	}

	private int cleanupJobLogs(long cutoffTime) {
		File jobLogsDir = Folders.Logs;

		if (!jobLogsDir.exists() || !jobLogsDir.isDirectory()) {
			Trace.logEvent("Job logs directory not found or not a directory: " + jobLogsDir.getAbsolutePath());
			return 0;
		}

		List<File> filesToDelete = new ArrayList<>();
		collectJobLogFiles(jobLogsDir, filesToDelete, cutoffTime);

		int deleted = deleteFiles(filesToDelete);

		deleteEmptyDirectories(filesToDelete, jobLogsDir);

		return deleted;
	}

	private int cleanupSystemLogs(long cutoffTime) {
		if (systemLogsDirectory == null || systemLogsRegex == null) {
			Trace.logEvent("System logs not configured, skipping cleanup");
			return 0;
		}

		if (!systemLogsDirectory.exists()) {
			Trace.logEvent("System log directory not found: " + systemLogsDirectory.getAbsolutePath());
			return 0;
		}

		List<File> filesToDelete = new ArrayList<>();
		collectSystemLogFiles(systemLogsDirectory, filesToDelete, cutoffTime, systemLogsRegex);

		return deleteFiles(filesToDelete);
	}

	private void collectJobLogFiles(File directory, List<File> result, long cutoffTime) {
		File[] files = directory.listFiles();
		if (files == null)
			return;

		for (File file : files) {
			if (file.isDirectory()) {
				collectJobLogFiles(file, result, cutoffTime);
			} else if (file.isFile() && file.getName().endsWith(".log")) {
				if (file.lastModified() < cutoffTime)
					result.add(file);
			}
		}
	}

	private void collectSystemLogFiles(File directory, List<File> result, long cutoffTime, String regex) {
		File[] files = directory.listFiles();
		if (files == null)
			return;

		for (File file : files) {
			if (file.isFile() && file.getName().matches(regex)) {
				if (file.lastModified() < cutoffTime)
					result.add(file);
			}
		}
	}

	private int deleteFiles(List<File> files) {
		int deleted = 0;

		for (File file : files) {
			if (file.delete()) {
				deleted++;
				Trace.logEvent("Deleted file: " + file.getAbsolutePath());
			} else {
				Trace.logEvent("Failed to delete: " + file.getAbsolutePath());
			}
		}

		return deleted;
	}

	private void deleteEmptyDirectories(List<File> deletedFiles, File rootDir) {
		Set<File> directories = new HashSet<>();

		for (File file : deletedFiles) {
			File parent = file.getParentFile();
			if (parent != null && !parent.equals(rootDir))
				directories.add(parent);
		}
		for (File dir : directories) {
			if (isEmptyDirectory(dir)) {
				if (!dir.delete())
					Trace.logEvent("Failed to delete empty directory: " + dir.getAbsolutePath());
			}
		}
	}

	private boolean isEmptyDirectory(File directory) {
		if (!directory.isDirectory())
			return false;

		String[] contents = directory.list();
		return contents != null && contents.length == 0;
	}
}