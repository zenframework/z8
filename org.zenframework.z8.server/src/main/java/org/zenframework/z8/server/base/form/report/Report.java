package org.zenframework.z8.server.base.form.report;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.reports.BirtReport;
import org.zenframework.z8.server.reports.ReportOptions;
import org.zenframework.z8.server.reports.Reports;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
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

	public Report(IObject container) {
		super(container);
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

	public String execute(guid recordId) {
		z8_execute(recordId);

		ReportOptions options = new ReportOptions();
		options.templateFolder = Folders.Reports;
		options.template = template.get() + '.' + Reports.DesignExtension;

		return new BirtReport(options).execute();
	}

	public void z8_execute(guid recordId) {
	}
}
