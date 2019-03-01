package org.zenframework.z8.server.base.file;

import java.io.File;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.types.file;

public class Folders {
	public static final String Files = "files";
	public static final String Temp = Files + file.separator + "temp";
	public static final String Logs = Files + file.separator + "logs";
	public static final String Storage = "storage";
	public static final String Cache = "pdf.cache";
	public static final String Reports = "reports";
	public static final String Fonts = "fonts";
	public static final String DefaultReports = Reports + file.separator + "defaults";
	public static final String ReportsOutput = Reports + file.separator + "generated";

	public static File Base = ServerConfig.workingPath();

}
