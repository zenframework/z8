package org.zenframework.z8.server.base.file;

import java.io.File;

import org.zenframework.z8.server.config.ServerConfig;

public class Folders {
	public static final String Files = "files";
	public static final String Storage = "storage";
	public static final String Cache = "pdf.cache";
	public static final String Reports = "reports";
	public static final String Fonts = "fonts";
	public static final String Temp = "temp";
	public static final String DefaultReports = Reports + File.separatorChar + "defaults";
	public static final String ReportsOutput = Reports + File.separatorChar + "generated";

	public static File Base = ServerConfig.workingPath();

}
