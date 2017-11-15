package org.zenframework.z8.server.request.actions;

import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.string;

public class MetaAction extends ReadAction {
	public MetaAction(ActionConfig config) {
		super(config);
	}

	@Override
	protected void initialize() {
		super.initialize();
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		ActionConfig config = config();
		Map<string, string> requestParameters = requestParameters();

		Query query = getQuery();

		query.setSelectFields(getSelectFields());
		query.writeMeta(writer, getContextQuery());

		writer.writeSort(config.sortFields);
		writer.writeGroup(config.groupFields);

		if(requestParameters.get(Json.start) == null)
			requestParameters.put(Json.start, new string(DefaultStart));
		if(requestParameters.get(Json.limit) == null)
			requestParameters.put(Json.limit, new string(DefaultLimit));

		super.writeResponse(writer);
	}
}
