package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.form.report.Report;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.guid;

class ReportReadAction extends ReadAction {
	ReportReadAction(ActionConfig config) {
		super(config);
	}

	@Override
	public String getFilterParameter() {
		return null;
	}

	public List<Field> getFields() {
		List<Field> result = new ArrayList<Field>();

		for(Field field : super.getSelectFields()) {
			if(field.isDataField() && !field.system())
				result.add(field);
		}

		return result;
	}
}

public class ReportAction extends RequestAction {
	private Collection<ReadAction> actions = new ArrayList<ReadAction>();

	private Collection<Field> sortFields;
	private Collection<guid> ids;

	public ReportAction(ActionConfig config) {
		super(config);

/*		String report = getReportParameter();
		Query query = getQuery();

		ids = getIdList();

		Collection<Query> queries = query.onReport(report, ids);

		for(Query reportQuery : queries) {
			sortFields = config.sortFields;

			config = new ActionConfig(requestParameters());
			config.query = reportQuery;
			config.sortFields = sortFields;

			ReadAction action = new ReportReadAction(config);
			actions.add(action);
		}
*/
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		Report report = getContextQuery().findReportById(getRequestParameter(Json.id));
		String reportId = report.execute(getRecordIdParameter());

		writer.writeProperty(Json.source, reportId);
		writer.writeProperty(Json.server, ApplicationServer.id);
	}
}
