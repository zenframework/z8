package org.zenframework.z8.server.request.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.reports.BirtReport;
import org.zenframework.z8.server.reports.Column;
import org.zenframework.z8.server.reports.PrintOptions;
import org.zenframework.z8.server.reports.ReportOptions;
import org.zenframework.z8.server.types.file;

public class ExportAction extends ReadAction {
	private Collection<Field> groupFields;
	private Collection<Column> columns;

	public ExportAction(ActionConfig config) {
		super(config);
	}

	protected void initialize() {
		ActionConfig config = config();

		columns = getColumns();
		groupFields = config.groupFields;

		config.groupFields = null;
		config.fields = getFields();

		super.initialize();
	}

	private String columnHeader(Field field) {
		String header = field.columnHeader();
		if(header != null && !header.isEmpty())
			return header;
		return field.displayName();
	}

	private Collection<Column> getColumns() {
		Collection<Column> result = new ArrayList<Column>();

		JsonArray columns = new JsonArray(getColumnsParameter());

		for(int index = 0; index < columns.length(); index++) {
			JsonObject json = (JsonObject)columns.get(index);

			Field field = getQuery().findFieldById(json.getString(Json.id));

			if(field != null) {
				Column c = new Column(columnHeader(field));
				c.setField(field);
				c.setWidth(json.getInt(Json.width));
				c.setMinWidth(json.getInt(Json.minWidth));
				result.add(c);
			}
		}

		return result;
	}

	private Collection<Field> getFields() {
		Collection<Field> result = new ArrayList<Field>();

		for(Column column : columns)
			result.add(column.getField());

		for(Field field : groupFields) {
			if(!result.contains(field))
				result.add(field);
		}

		return result;
	}

	private String getReportHeader() {
		return getQuery().displayName();
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		PrintOptions printOptions = new PrintOptions(getOptionsParameter());

		ReportOptions report = new ReportOptions(printOptions);
		report.header = getReportHeader();
		report.format = getFormatParameter();
		report.action = this;

		BirtReport birtReport = new BirtReport(report);
		file file = new file(birtReport.execute(columns, groupFields));

		ApplicationServer.getMonitor().print(file);
	}
}
