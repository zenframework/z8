package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
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
			return actionParameters.getBoolean(Json.save) ? new CreateAction(actionParameters) : new NewAction(actionParameters);
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
		else if(Action.moveAction.equals(actionName))
			return new MoveAction(actionParameters);
		else if(Action.commandAction.equals(actionName))
			return new CommandAction(actionParameters);
		else if(Action.reportAction.equals(actionName))
			return new ReportAction(actionParameters);
		else if(Action.previewAction.equals(actionName))
			return new PreviewAction(actionParameters);
		else if(Action.readRecordAction.equals(actionName))
			return new ReadRecordAction(actionParameters);
		else if(Action.modelAction.equals(actionName))
			return new ModelAction(actionParameters);
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

		if(actionName == null || actionName.equals(Action.readAction))
			result.query.onRender();

		initialize(result);

		if(result.query != null) {
			if(actionName == null) {
				result.groupFields = new ArrayList<Field>();
				result.groupFields.addAll(result.query.collectGroupFields());

				if(result.sortFields == null) {
					result.sortFields = new LinkedHashSet<Field>();
					result.sortFields.addAll(result.groupFields);
					result.sortFields.addAll(result.query.collectSortFields());
				}

				if(result.fields != null) {
					result.groupFields.retainAll(result.fields);
					result.sortFields.retainAll(result.fields);
				}
			} else {
				result.groupFields = getGroupFields(result.query);
				result.sortFields = getSortFields(result.query, result.groupFields);
			}

			result.groupBy = result.query.collectGroupByFields();
			result.aggregateBy = result.query.collectAggregateByFields();

			result.totalsBy = getTotalsBy(result.query);
			setAggregation(result.query);
		}

		return result;
	}

	private void initialize(ActionParameters result) {
		String linkId = requestParameter(Json.link);

		if(linkId == null || linkId.isEmpty())
			return;

		result.fields = new ArrayList<Field>();

		Link link = (Link)result.query.findFieldById(linkId);
		result.query = link.getQuery();

		Collection<Field> fields = result.requestQuery.getFormFields();

		for(Field field : fields) {
			if(result.query.findFieldById(field.id()) != null)
				result.fields.add(field);
		}

		/*
		 * String sort = requestParameter(Json.sort);
		 * 
		 * if(!result.fields.isEmpty()) { result.sortFields = new
		 * ArrayList<Field>(); result.sortFields.add(field); }
		 */
	}

	private Field getTotalsBy(Query query) {
		String totalsBy = requestParameter(Json.totalsBy);
		return totalsBy != null ? query.findFieldById(totalsBy) : null;
	}

	private void setAggregation(Query query) {
		String aggregation = requestParameter(Json.aggregation);

		if(aggregation != null) {
			JsonArray array = new JsonArray(aggregation);

			for(int index = 0; index < array.length(); index++) {
				Field field = query.findFieldById(array.getString(index));

				if(field != null)
					field.aggregation = Aggregation.Sum;
			}
		}
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

		if(jsonData.charAt(0) != '[')
			return parseSortFieldsInOldSchoolManner(query, jsonData);

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

		if(jsonData == null)
			return parseGroupFieldsInOldSchoolManner(query);

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

	private Collection<Field> parseSortFieldsInOldSchoolManner(Query query, String jsonData) {
		Collection<Field> fields = new ArrayList<Field>();

		Field field = query.findFieldById(jsonData);
		String dir = requestParameter(Json.dir);

		if(field != null) {
			field.sortDirection = SortDirection.fromString(dir);
			fields.add(field);
		}

		return fields;
	}

	private Collection<Field> parseGroupFieldsInOldSchoolManner(Query query) {
		Collection<Field> fields = new ArrayList<Field>();

		String jsonData = requestParameter(Json.groupBy);

		if(jsonData == null || jsonData.isEmpty())
			return fields;

		String groupDir = requestParameter(Json.groupDir);

		JsonArray array = new JsonArray(jsonData);

		for(int index = 0; index < array.length(); index++) {
			Field field = query.findFieldById(array.getString(index));

			if(field != null) {
				field.sortDirection = groupDir == null || groupDir.isEmpty() ? SortDirection.Asc : SortDirection.fromString(groupDir);
				fields.add(field);
			}
		}

		return fields;
	}
}
