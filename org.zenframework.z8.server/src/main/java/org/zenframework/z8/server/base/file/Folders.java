package org.zenframework.z8.server.base.file;

import java.io.File;

import org.zenframework.z8.server.engine.Z8Context;

public class Folders {

	public static final String Files = "files";
	public static final String Storage = "storage";
	public static final String Cache = "pdf.cache";
	public static final String Lucene = "lucene";
	public static final String Reports = "reports";
	public static final String ReportDefaults = Reports + File.separatorChar + "defaults";
	public static final String ReportsOutput = Reports + File.separatorChar + "generated";

	public static File Base = Z8Context.getWorkingPath();

}
