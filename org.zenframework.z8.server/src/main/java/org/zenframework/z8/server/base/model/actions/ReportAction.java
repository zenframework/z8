package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.reports.BirtReportRunner;
import org.zenframework.z8.server.reports.PageFormat;
import org.zenframework.z8.server.reports.PageOrientation;
import org.zenframework.z8.server.reports.PrintOptions;
import org.zenframework.z8.server.reports.ReportOptions;
import org.zenframework.z8.server.reports.Reports;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;

class ReportReadAction extends ReadAction {
	ReportReadAction(ActionParameters parameters) {
		super(parameters);
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

public class ReportAction extends Action {
	private Collection<ReadAction> actions = new ArrayList<ReadAction>();

	private Collection<Field> groupFields;
	private Collection<Field> columns;
	private Collection<guid> ids;

	public ReportAction(ActionParameters parameters) {
		super(parameters);

		String report = getReportParameter();
		Query query = getQuery();

		if(report != null) {
			ids = getIdList();

			String modelName = getQuery().getRootQuery().name();

			Collection<Query> queries = query.onReport(report, ids);

			for(Query reportQuery : queries) {
				parameters = new ActionParameters(requestParameters());
				parameters.query = reportQuery;

				ReadAction action = new ReportReadAction(parameters);
				actions.add(action);

				if(!reportQuery.printAsList.get() && !ids.isEmpty()) {
					if(reportQuery.getRootQuery().name().equals(modelName))
						action.addFilter(reportQuery.primaryKey(), ids.iterator().next());
				}
			}
		} else {
			groupFields = parameters.groupFields;
			parameters.groupFields = null;

			columns = getColumns();
			parameters.fields = getFields();

			ReadAction action = new ReadAction(parameters);
			actions.add(action);
		}
	}

	private Collection<Field> getColumns() {
		Collection<Field> result = new ArrayList<Field>();

		JsonArray columns = new JsonArray(getColumnsParameter());

		for(int index = 0; index < columns.length(); index++) {
			JsonObject column = (JsonObject)columns.get(index);

			Field field = getQuery().findFieldById(column.getString(Json.id));

			if(field != null && !field.system()) {
				int width = column.getInt(Json.width);
				field.width = new integer(width);
				result.add(field);
			}
		}

		return result;
	}

	private Collection<Field> getFields() {
		Collection<Field> result = new ArrayList<Field>();

		result.addAll(columns);

		for(Field field : groupFields) {
			if(!result.contains(field))
				result.add(field);
		}

		return result;
	}

	private String getReportHeader() {
		Query query = getQuery();

		String header = query.displayName();

		if(query.period != null)
			header += ", " + query.period.get().displayName();

		if(!header.isEmpty())
			header += '.';

		return header;
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		PrintOptions printOptions = new PrintOptions();

		String report = getReportParameter();

		String reportFolder = Folders.Reports;
		String reportTemplate = report != null ? report : null;
		String reportCaption = "";

		if(report == null) {
			reportTemplate = Reports.DefaultDesign;
			reportFolder = Folders.ReportDefaults;
			reportCaption = getReportHeader();

			JsonObject object = new JsonObject(getOptionsParameter());

			printOptions.pageOrientation = PageOrientation.fromString(object.getString(Json.pageOrientation));
			printOptions.pageFormat = PageFormat.fromString(object.getString(Json.pageFormat));

			printOptions.leftMargin = (float)object.getDouble(Json.leftMargin);
			printOptions.rightMargin = (float)object.getDouble(Json.rightMargin);
			printOptions.topMargin = (float)object.getDouble(Json.topMargin);
			printOptions.bottomMargin = (float)object.getDouble(Json.bottomMargin);
		}

		ReportOptions reportOptions = printOptions.getReportOptions(reportCaption, actions, reportFolder, reportTemplate);
		reportOptions.setFormat(getFormatParameter());

		BirtReportRunner reportRunner = new BirtReportRunner(reportOptions);

		String reportId = report != null ? reportRunner.execute() : reportRunner.execute(columns, groupFields);

		writer.writeProperty(Json.source, reportId);
		writer.writeProperty(Json.serverId, ApplicationServer.id);
	}

}
