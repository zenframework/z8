package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.db.sql.SortDirection;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.string;

public class ActionFactory {
	private String requestId;
	private Query requestQuery;

	private Map<string, string> requestParameters;

	public static Action create(Query query) {
		ActionFactory factory = new ActionFactory(query, null);
		ActionParameters actionParameters = factory.getActionParameters();

		String actionName = actionParameters.requestParameter(Json.action);

		if(actionName == null)
			return new MetaAction(actionParameters);

		if(Action.newAction.equals(actionName))
			return new CreateAction(actionParameters); // new NewAction(actionParameters);
		else if(Action.createAction.equals(actionName))
			return new CreateAction(actionParameters);
		else if(Action.copyAction.equals(actionName))
			return new CopyAction(actionParameters);
		else if(Action.readAction.equals(actionName))
			return new ReadAction(actionParameters, actionParameters.getId());
		else if(Action.updateAction.equals(actionName))
			return new UpdateAction(actionParameters);
		else if(Action.destroyAction.equals(actionName))
			return new DestroyAction(actionParameters);
		else if(Action.commandAction.equals(actionName))
			return new CommandAction(actionParameters);
		else if(Action.reportAction.equals(actionName))
			return new ReportAction(actionParameters);
		else if(Action.previewAction.equals(actionName))
			return new PreviewAction(actionParameters);
		else if(Action.attachAction.equals(actionName))
			return new AttachAction(actionParameters);
		else if(Action.detachAction.equals(actionName))
			return new DetachAction(actionParameters);
		else
			throw new RuntimeException("Unknown CRUD action: '" + actionName + "'");
	}

	public static ActionParameters getActionParameters(Query query) {
		ActionFactory factory = new ActionFactory(query, new HashMap<string, string>());
		return factory.getActionParameters();
	}

	private ActionFactory(Query query, Map<string, string> requestParameters) {
		this.requestParameters = requestParameters == null ? ApplicationServer.getRequest().getParameters() : requestParameters;

		if(query != null) {
			requestId = query.classId();
			requestQuery = query;
		}
	}

	public String requestParameter(string key) {
		string value = requestParameters.get(key);
		return value != null ? value.get() : null;
	}

	private ActionParameters getActionParameters() {
		ActionParameters result = new ActionParameters(requestParameters);

		result.requestId = requestId;

		result.requestQuery = requestQuery;

		String actionName = requestParameter(Json.action);

		result.query = requestQuery;

		initialize(result);

		if(actionName == null) {
			result.groupFields = new ArrayList<Field>();
			result.groupFields.addAll(result.query.groupFields());

			if(result.sortFields == null) {
				result.sortFields = new LinkedHashSet<Field>();
				result.sortFields.addAll(result.groupFields);
				result.sortFields.addAll(result.query.sortFields());
			}

			if(result.fields != null) {
				result.groupFields.retainAll(result.fields);
				result.sortFields.retainAll(result.fields);
			}
		} else {
			result.groupFields = getGroupFields(result.requestQuery);
			result.sortFields = getSortFields(result.requestQuery, result.groupFields);
		}

		return result;
	}

	private void initialize(ActionParameters result) {
		if(!initializeWithLink(result, requestParameter(Json.link)))
			initializeWithQuery(result, requestParameter(Json.query));

		Query query = result.requestQuery;
		String json = requestParameter(Json.fields);
		result.fields = QueryUtils.parseFormFields(query, json);
	}

	private boolean initializeWithLink(ActionParameters result, String id) {
		if(id != null && !id.isEmpty()) {
			ILink link = (ILink)result.query.findFieldById(id);
			result.query = link.getQuery();
			result.link = link;
			return true;
		}
		return false;
	}

	private void initializeWithQuery(ActionParameters result, String id) {
		if(id != null && !id.isEmpty())
			result.query = result.requestQuery.findQueryById(id);
	}

	private Collection<Field> getGroupFields(Query query) {
		return parseGroupFields(query);
	}

	private Collection<Field> getSortFields(Query query, Collection<Field> groupFields) {
		Collection<Field> sortFields = parseSortFields(query);

		if(sortFields.isEmpty() && groupFields.isEmpty())
			return null;

		Collection<Field> fields = new LinkedHashSet<Field>();
		fields.addAll(groupFields);
		fields.addAll(sortFields);

		return fields;
	}

	private Collection<Field> parseSortFields(Query query) {
		Collection<Field> fields = new ArrayList<Field>();

		String jsonData = requestParameter(Json.sort);

		if(jsonData == null || jsonData.isEmpty())
			return fields;

		JsonArray array = new JsonArray(jsonData);

		for(int index = 0; index < array.length(); index++) {
			JsonObject object = array.getJsonObject(index);

			Field field = query.findFieldById(object.getString(Json.property));
			String dir = object.getString(Json.direction);

			if(field != null) {
				field.sortDirection = SortDirection.fromString(dir);
				fields.add(field);
			}
		}

		return fields;
	}

	private Collection<Field> parseGroupFields(Query query) {
		String jsonData = requestParameter(Json.group);

		Collection<Field> fields = new ArrayList<Field>();

		if(jsonData == null || jsonData.isEmpty())
			return fields;

		if(jsonData.charAt(0) != '[')
			jsonData = '[' + jsonData + ']';

		JsonArray array = new JsonArray(jsonData);

		for(int index = 0; index < array.length(); index++) {
			JsonObject object = array.getJsonObject(index);

			Field field = query.findFieldById(object.getString(Json.property));

			if(field != null)
				fields.add(field);
		}

		return fields;
	}
}
