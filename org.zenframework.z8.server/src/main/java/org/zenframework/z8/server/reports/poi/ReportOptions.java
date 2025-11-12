package org.zenframework.z8.server.reports.poi;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.reports.ReportUtils;
import org.zenframework.z8.server.reports.Reports;

public class ReportOptions {

	public static final String Xlsx = "xlsx";

	private static final String Ext = "." + Xlsx;

	private String templateFolder = Folders.Reports;
	private String template = Reports.DefaultDesign;
	private String name = null;

	public String getTemplateFolder() {
		return templateFolder;
	}

	public ReportOptions setTemplateFolder(String templateFolder) {
		this.templateFolder = templateFolder;
		return this;
	}

	public String getTemplate() {
		return template;
	}

	public ReportOptions setTemplate(String template) {
		this.template = template;
		return this;
	}

	public File getTemplateFile() {
		String template = this.template.toLowerCase().endsWith(Ext) ? this.template : this.template + Ext;
		return FileUtils.getFile(Folders.ApplicationPath, templateFolder, template);
	}

	public String getName() {
		return name != null ? name : !template.toLowerCase().endsWith(Ext) ? template
				: template.substring(0, template.length() - Ext.length());
	}

	public ReportOptions setName(String name) {
		this.name = name;
		return this;
	}

	public File getOutputFile() {
		File outputFolder = Folders.ReportsOutput;
		outputFolder.mkdirs();
		return ReportUtils.getUniqueFileName(outputFolder, getName(), ReportOptions.Xlsx);
	}
}
