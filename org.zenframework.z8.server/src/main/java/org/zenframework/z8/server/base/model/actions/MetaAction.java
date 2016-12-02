package org.zenframework.z8.server.base.model.actions;

import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.string;

public class MetaAction extends ReadAction {
	static public final string StartValue = new string("0");
	static public final string LimitValue = new string("50");

	public MetaAction(ActionParameters actionParameters) {
		super(actionParameters);
	}

	@Override
	protected void initialize() {
		super.initialize();
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		ActionParameters actionParameters = actionParameters();
		Map<string, string> requestParameters = requestParameters();

		Query query = getQuery();

		writer.writeProperty(Json.isQuery, true);

		Collection<Field> fields = getSelectFields();
		query.writeMeta(writer, fields);

		writer.writeSort(actionParameters.sortFields);
		writer.writeGroup(actionParameters.groupFields);

		if(requestParameters.get(Json.start) == null)
			requestParameters.put(Json.start, StartValue);
		if(requestParameters.get(Json.limit) == null)
			requestParameters.put(Json.limit, LimitValue);

		super.writeResponse(writer);
	}
}
