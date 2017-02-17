package org.zenframework.z8.server.base.model.actions;

import org.zenframework.z8.server.base.form.report.Report;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;

public class ReportAction extends RequestAction {
	public ReportAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		String id = getRequestParameter(Json.id);
		Report report = getContextQuery().findReportById(id);

		if(report == null)
			throw new RuntimeException("No report with id: '" + id + "'");

		String file = report.execute(getRecordIdParameter());

		writer.writeProperty(Json.source, file);
		writer.writeProperty(Json.server, ApplicationServer.id);
	}
}
