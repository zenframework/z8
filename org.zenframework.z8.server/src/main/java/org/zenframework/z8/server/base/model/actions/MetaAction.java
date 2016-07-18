package org.zenframework.z8.server.base.model.actions;

import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class MetaAction extends ReadAction {
	static public final string StartValue = new string("0");
	static public final string LimitValue = new string("50");

	public MetaAction(ActionParameters actionParameters) {
		super(actionParameters);
	}

	@Override
	protected void initialize() {
		Map<string, string> requestParameters = requestParameters();

		Query query = getQuery();
		
		if(query.showAsTree())
			requestParameters.put(Json.parentId, new string(guid.NULL.toString()));
		
		super.initialize();
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		ActionParameters actionParameters = actionParameters();
		Map<string, string> requestParameters = requestParameters();
		
		Query query = getQuery();

		writer.writeProperty(Json.isQuery, true);

		writer.writeProperty(Json.queryId, getRequestParameter(Json.queryId));

		if(actionParameters.link != null) {
			writer.writeProperty(Json.fieldId, getRequestParameter(Json.fieldId));
			writer.writeProperty(Json.linkId, actionParameters.link.id());
		}

		Collection<Field> fields = getSelectFields();
		query.writeMeta(writer, fields);

		writeSortFields(writer, actionParameters.sortFields);
		writeGroupFields(writer, actionParameters.groupFields);

		requestParameters.put(Json.start, StartValue);
		requestParameters.put(Json.limit, LimitValue);

		requestParameters.put(Json.limit, LimitValue);

		super.writeResponse(writer);
	}

	private void writeSortFields(JsonWriter writer, Collection<Field> sortFields) {
		if(!sortFields.isEmpty()) {
			Field field = sortFields.iterator().next();

			writer.writeProperty(Json.sort, field.id());
			writer.writeProperty(Json.direction, field.sortDirection.toString());
		}
	}

	private void writeGroupFields(JsonWriter writer, Collection<Field> groupFields) {
		if(!groupFields.isEmpty()) {
			writer.startArray(Json.groupBy);

			for(Field field : groupFields)
				writer.write(field.id());

			writer.finishArray();
		}
	}
}
