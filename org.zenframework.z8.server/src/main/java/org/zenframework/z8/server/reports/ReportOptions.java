package org.zenframework.z8.server.reports;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.model.actions.ReadAction;
import org.zenframework.z8.server.base.query.Query;

import com.lowagie.text.FontFactory;

public class ReportOptions {
	public String format = Reports.Pdf;

	public int indentGroupsBy = Reports.DefaultGroupIndentation;

	public boolean markGroupLevel = true;

	public boolean markTotals = true;

	public boolean markGrandTotals = true;

	public boolean dropGroupDetail = false;

	public boolean useBlackWhiteColors = false;

	public int pagesWide = 1;

	public boolean scaleContent = true;

	public boolean splitContent = false;

	public int pageOverlapping = Reports.DefaultPageOverlapping;

	public String templateFolder = Folders.DefaultReports;
	public String template = Reports.DefaultDesign;

	public ReadAction action = null;
	public Collection<Query> queries = null;

	public String header = "document";

	public PrintOptions printOptions;

	private static IReportEngine reportEngine = null;
	private static IDesignEngine designEngine = null;

	static public File getReportOutputFolder() {
		File folder = new File(Folders.Base, Folders.ReportsOutput);
		folder.mkdirs();
		return folder;
	}

	public ReportOptions() {
		this(new PrintOptions());
	}

	public ReportOptions(PrintOptions printOptions) {
		this.printOptions = printOptions;

		initializeEngine();
	}

	private void initializeEngine() throws RuntimeException {
		if(reportEngine != null) {
			return;
		}

		try {
			registerFonts();

			EngineConfig engineConfig = new EngineConfig();
			DesignConfig designConfig = new DesignConfig();

			Platform.startup(engineConfig);

			IReportEngineFactory reportEngineFactory = (IReportEngineFactory)Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
			reportEngine = reportEngineFactory.createReportEngine(engineConfig);

			IDesignEngineFactory designEngineFactory = (IDesignEngineFactory)Platform.createFactoryObject(IDesignEngineFactory.EXTENSION_DESIGN_ENGINE_FACTORY);
			designEngine = designEngineFactory.createDesignEngine(designConfig);
		} catch(BirtException e) {
			throw new RuntimeException(e);
		}
	}

	private void registerFonts() {
		File fontsFolder = new File(Folders.Base, Folders.Fonts);

		File[] files = fontsFolder.listFiles();

		for(File file : files) {
			try {
				FontFactory.register(file.getPath());
			} catch(Throwable e) {
			}
		}
	}

	public IReportEngine reportEngine() {
		return reportEngine;
	}

	public IDesignEngine designEngine() {
		return designEngine;
	}

	private String getReportDesignFileName(String format) {
		return format + '.' + Reports.DesignExtension;
	}

	public File getReportDesign() {
		String fileName = getReportDesignFileName(format);

		File file = FileUtils.getFile(Folders.Base, templateFolder, fileName);

		if(file.exists())
			return file;

		return FileUtils.getFile(Folders.Base, templateFolder, template);
	}

	public float pageWidth() {
		return printOptions.pageWidth();
	}

	public float pageHeight() {
		return printOptions.pageHeight();
	}

	public float leftMargin() {
		return printOptions.leftMargin();
	}

	public float rightMargin() {
		return printOptions.rightMargin();
	}

	public float topMargin() {
		return printOptions.topMargin();
	}

	public float bottomMargin() {
		return printOptions.bottomMargin();
	}

	public float horizontalMargins() {
		return leftMargin() + rightMargin();
	}

	public float verticalMargins() {
		return topMargin() + bottomMargin();
	}

	public String documentName() {
		return header;
	}
}
