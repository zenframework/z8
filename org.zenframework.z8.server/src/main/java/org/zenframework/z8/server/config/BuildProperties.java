package org.zenframework.z8.server.config;

import java.io.File;

import org.zenframework.z8.server.base.file.Folders;

public class BuildProperties extends Config {
	private static final long serialVersionUID = 1L;

	public static final String FileName = "build.properties";

	public static final String ApplicationName = "application.name";
	public static final String ApplicationVersion = "application.version";
	public static final String BuildTimestamp = "build.timestamp";
	public static final String GitCommit = "git.commit";
	public static final String GitBranch = "git.branch";

	private static final String Version = "version";
	private static final String Unknown = "unknown";

	private static BuildProperties instance;

	private BuildProperties(File file) {
		super(file);
	}

	public static BuildProperties instance() {
		if (instance == null)
			instance = new BuildProperties(new File(Folders.WorkingPath, FileName));
		return instance;
	}

	public static String getApplicationName() {
		return instance().getProperty(ApplicationName, Unknown);
	}

	public static String getApplicationVersion() {
		return instance().getProperty(ApplicationVersion, Unknown);
	}

	public static String getBuildTimestamp() {
		return instance().getProperty(BuildTimestamp, Unknown);
	}

	public static String getGitCommit() {
		return instance().getProperty(GitCommit, Unknown);
	}

	public static String getGitBranch() {
		return instance().getProperty(GitBranch, Unknown);
	}

	public static String getModuleVersion(String module) {
		return instance().getProperty(module.trim() + '.' + Version, Unknown);
	}
}
