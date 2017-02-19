package org.zenframework.z8.server.base.form.report;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.engine.ApplicationServer;
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

	public string template;
	public string name;

	public Report(IObject container) {
		super(container);
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
	}

	public void execute(guid recordId) {
		prepare(recordId);

		ReportOptions report = new ReportOptions();
		report.templateFolder = Folders.Reports;
		report.template = template.get() + '.' + Reports.DesignExtension;
		report.queries = queries();
		report.header = name.get();

		file file = new file(new BirtReport(report).execute());
		ApplicationServer.getMonitor().print(file);
	}

	public void prepare(guid recordId) {
		z8_prepare(recordId);
	}

	public void z8_prepare(guid recordId) {
	}

	public void z8_execute(guid recordId) {
		execute(recordId);
	}
}
