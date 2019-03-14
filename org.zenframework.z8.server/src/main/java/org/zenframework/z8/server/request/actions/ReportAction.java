package org.zenframework.z8.server.request.actions;

import org.zenframework.z8.server.base.form.report.Report;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;

public class ReportAction extends RequestAction {
	public ReportAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		String id = getRequestParameter(Json.id);
		String format = getRequestParameter(Json.format);
		String name = getRequestParameter(Json.name);
		Report report = getContextQuery().findReportById(id);

		if(report == null)
			throw new RuntimeException("No report with id: '" + id + "'");

		report.format = format != null ? new string(format) : report.format;
		report.name = name != null ? new string(name) : report.name;

		file file = report.run(getRecordIdParameter());
		ApplicationServer.getMonitor().print(file);
	}
}
