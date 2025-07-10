package org.zenframework.z8.server.base.file;

import java.io.File;

import org.zenframework.z8.server.config.ServerConfig;

public class Folders {
	public static final File ApplicationPath = ServerConfig.applicationPath();
	public static final File Fonts = new File(ApplicationPath, "fonts");
	public static final File Resources = new File(ApplicationPath, "resources");

	public static final File ConfigPath = ServerConfig.configPath();
	
	public static final File WorkingPath = ServerConfig.workingPath();
	public static final File ReportsOutput = new File(WorkingPath, "reports");
	public static final File Files = new File(WorkingPath, "files");
	public static final File PdfCache = new File(WorkingPath, "files");
	public static final File Temp = new File(Files, "temp");
	public static final File Logs = new File(Files, "logs");

	public static final String Reports = "reports";
	public static final String DefaultReports = Reports + File.separator + "defaults";
}
