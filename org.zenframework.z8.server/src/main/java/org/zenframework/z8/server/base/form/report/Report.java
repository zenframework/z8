package org.zenframework.z8.server.base.form.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.file.InputOnlyFileItem;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.reports.BirtReport;
import org.zenframework.z8.server.reports.ReportOptions;
import org.zenframework.z8.server.reports.Reports;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class Report extends OBJECT implements Runnable, IReport {
	static public class CLASS<T extends Report> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Report.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Report(container);
		}
	}

	static public final string Pdf = new string(Reports.Pdf);
	static public final string Excel = new string(Reports.Excel);
	static public final string Word = new string(Reports.Word);
	static public final string Html = new string(Reports.Html);
	static public final string Powerpoint = new string(Reports.Powerpoint);

	public string template;
	public string name;
	public string format;

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
		writer.writeProperty(Json.id, id());
		writer.writeProperty(Json.text, displayName());
		writer.writeProperty(Json.description, description());
		writer.writeProperty(Json.icon, icon());
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
		ReportOptions report = new ReportOptions();
		report.templateFolder = Folders.Reports;
		report.template = template.get() + '.' + Reports.DesignExtension;
		report.queries = queries();
		report.header = name != null ? name.get() : template.get();
		report.format = format().get();

		File diskFile = new File(Folders.Base, new BirtReport(report).execute().getPath());
		file file = new file(diskFile);
		file.set(new InputOnlyFileItem(diskFile, diskFile.getName()));

		return file;
	}

	public file z8_run(guid recordId) {
		return run(recordId);
	}
}
