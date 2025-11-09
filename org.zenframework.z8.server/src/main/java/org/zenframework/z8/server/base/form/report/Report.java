package org.zenframework.z8.server.base.form.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.file.InputOnlyFileItem;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.reports.BirtReport;
import org.zenframework.z8.server.reports.Reports;
import org.zenframework.z8.server.reports.poi.PoiReport;
import org.zenframework.z8.server.reports.poi.Util;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class Report extends OBJECT implements Runnable, IReport {
	static public class CLASS<T extends Report> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Report.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Report(container);
		}
	}

	static public final string BIRT = new string(Reports.BIRT);
	static public final string POI = new string(Reports.POI);

	static public final string Pdf = new string(Reports.Pdf);
	static public final string Excel = new string(Reports.Excel);
	static public final string Word = new string(Reports.Word);
	static public final string WordX = new string(Reports.WordX);
	static public final string Html = new string(Reports.Html);
	static public final string Powerpoint = new string(Reports.Powerpoint);

	public string engine = BIRT;
	public string template;
	public string name;
	public string format;

	public RCollection<Range.CLASS<Range>> ranges = new RCollection<Range.CLASS<Range>>();
	public RCollection<string> hiddenColumns = new RCollection<string>();

	public Report(IObject container) {
		super(container);
	}

	private string format() {
		return format != null ? format : Report.Pdf;
	}

	@Override
	public Collection<Query> queries() {
		Collection<Query> datasets = new ArrayList<Query>();

		for(IClass<? extends IObject> member : members()) {
			if(member instanceof Query.CLASS)
				datasets.add((Query)member.get());
		}

		return datasets;
	}

	@Override
	public void run() {
	}

	@Override
	public void write(JsonWriter writer) {
		super.write(writer);
		writer.writeProperty(Json.format, format());
	}

	public file run(guid recordId) {
		prepare(recordId);
		return execute(recordId);
	}

	protected void prepare(guid recordId) {
		z8_prepare(recordId);
	}

	protected file execute(guid recordId) {
		return z8_execute(recordId);
	}

	protected void z8_prepare(guid recordId) {
	}

	protected file z8_execute(guid recordId) {
		File reportFile;

		if (engine.get().equals(BIRT.get()))
			reportFile = executeBirt();
		else if (engine.get().equals(POI.get()))
			reportFile = executePoi();
		else
			throw new IllegalStateException();

		file file = new file(reportFile);
		file.set(new InputOnlyFileItem(reportFile, reportFile.getName()));

		return file;
	}

	private File executeBirt() {
		org.zenframework.z8.server.reports.ReportOptions report = new org.zenframework.z8.server.reports.ReportOptions();
		report.templateFolder = Folders.Reports;
		report.template = template.get() + '.' + Reports.DesignExtension;
		report.queries = queries();
		report.format = format().get();
		report.setName(name != null ? name.get() : template.get());

		return new File(Folders.WorkingPath, new BirtReport(report).execute().getPath());
	}

	private File executePoi() {
		org.zenframework.z8.server.reports.poi.ReportOptions options = new org.zenframework.z8.server.reports.poi.ReportOptions()
				.setContext(this)
				.setTemplate(template.get())
				.setName(name != null ? name.get() : null)
				.setRanges(Range.asPoiRanges(ranges))
				.setHiddenColumns(columnsToInt(hiddenColumns));

		PoiReport report = new PoiReport(options);

		IMonitor monitor = ApplicationServer.getMonitor();

		if (monitor != null) {
			for (String error : report.getErrors())
				monitor.warning(error);
		}

		return report.execute();
	}

	public file z8_run(guid recordId) {
		return run(recordId);
	}

	private static Set<Integer> columnsToInt(Collection<string> columns) {
		Set<Integer> result = new HashSet<Integer>();

		for (string column : columns)
			result.add(Util.columnToInt(column.get()));

		return result;
	}
}
