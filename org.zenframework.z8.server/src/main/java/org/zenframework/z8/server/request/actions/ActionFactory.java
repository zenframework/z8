package org.zenframework.z8.server.request.actions;

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
	private Query contextQuery;

	private Map<string, string> requestParameters;

	public static RequestAction create(Query query) {
		ActionFactory factory = new ActionFactory(query, null);
		ActionConfig config = factory.getActionConfig();

		String actionName = config.requestParameter(Json.action);

		if(actionName == null)
			return new MetaAction(config);

		if(RequestAction.Meta.equals(actionName))
			return new MetaAction(config, false);
		else if(RequestAction.Create.equals(actionName))
			return new CreateAction(config);
		else if(RequestAction.Copy.equals(actionName))
			return new CopyAction(config);
		else if(RequestAction.Read.equals(actionName))
			return new ReadAction(config);
		else if(RequestAction.Update.equals(actionName))
			return new UpdateAction(config);
		else if(RequestAction.Destroy.equals(actionName))
			return new DestroyAction(config);
		else if(RequestAction.Action.equals(actionName))
			return new CommandAction(config);
		else if(RequestAction.Export.equals(actionName))
			return new ExportAction(config);
		else if(RequestAction.Report.equals(actionName))
			return new ReportAction(config);
		else if(RequestAction.Preview.equals(actionName))
			return new PreviewAction(config);
		else if(RequestAction.Attach.equals(actionName))
			return new AttachAction(config);
		else if(RequestAction.Detach.equals(actionName))
			return new DetachAction(config);
		else if(RequestAction.Content.equals(actionName))
			return new ContentAction(config);
		else
			throw new RuntimeException("Unknown action: '" + actionName + "'");
	}

	public static ActionConfig getActionParameters(Query query) {
		ActionFactory factory = new ActionFactory(query, new HashMap<string, string>());
		return factory.getActionConfig();
	}

	private ActionFactory(Query query, Map<string, string> requestParameters) {
		this.requestParameters = requestParameters == null ? ApplicationServer.getRequest().getParameters() : requestParameters;

		if(query != null) {
			requestId = query.classId();
			contextQuery = query;
		}
	}

	public String requestParameter(string key) {
		string value = requestParameters.get(key);
		return value != null ? value.get() : null;
	}

	private ActionConfig getActionConfig() {
		ActionConfig config = new ActionConfig(requestParameters);

		config.requestId = requestId;
		config.contextQuery = config.query = contextQuery;

		String actionName = requestParameter(Json.action);

		initialize(config);

		if(actionName == null) {
			config.groupFields = new ArrayList<Field>();
			config.groupFields.addAll(config.query.groupFields());

			if(config.sortFields == null) {
				config.sortFields = new LinkedHashSet<Field>();
				config.sortFields.addAll(config.groupFields);
				config.sortFields.addAll(config.query.sortFields());
			}

			if(config.fields != null) {
				config.groupFields.retainAll(config.fields);
				config.sortFields.retainAll(config.fields);
			}
		} else {
			config.groupFields = getGroupFields(config.contextQuery);
			config.sortFields = getSortFields(config.contextQuery, config.groupFields);
		}

		return config;
	}

	private void initialize(ActionConfig config) {
		if(!initializeWithLink(config, requestParameter(Json.link)))
			initializeWithQuery(config, requestParameter(Json.query));

		Query query = config.contextQuery;
		String json = requestParameter(Json.fields);
		config.fields = QueryUtils.parseFormFields(query, json);
	}

	private boolean initializeWithLink(ActionConfig config, String id) {
		if(id != null && !id.isEmpty()) {
			ILink link = (ILink)config.query.findFieldById(id);
			config.query = link.getQuery();
			config.link = link;
			return true;
		}
		return false;
	}

	private void initializeWithQuery(ActionConfig config, String id) {
		if(id != null && !id.isEmpty())
			config.query = config.contextQuery.findQueryById(id);
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
			String dir = object.containsKey(Json.direction.get()) ? object.getString(Json.direction) : null;

			if(field != null) {
				field.sortDirection = dir != null ? SortDirection.fromString(dir) : SortDirection.Asc;
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
