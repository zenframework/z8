package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.db.sql.SortDirection;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.RequestTarget;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.guid;

public class ActionFactory {
    private String requestId;
    private Query requestQuery;

    private Query model;
    private Query context;

    private Map<String, String> requestParameters;

    public static Action create(Query query) {
        ActionFactory factory = new ActionFactory(query, null);
        ActionParameters actionParameters = factory.getActionParameters();

        String actionName = actionParameters.requestParameters.get(Json.action);

        if(actionName == null) {
            return new MetaAction(actionParameters);
        }
        if(Action.newAction.equals(actionName)) {
            return actionParameters.getBoolean(Json.save.get()) ? new CreateAction(actionParameters) : new NewAction(actionParameters);
        }
        else if(Action.createAction.equals(actionName)) {
            return new CreateAction(actionParameters);
        }
        else if(Action.copyAction.equals(actionName)) {
            return new CopyAction(actionParameters);
        }
        else if(Action.readAction.equals(actionName)) {
            return new ReadAction(actionParameters, actionParameters.getId());
        }
        else if(Action.updateAction.equals(actionName)) {
            return new UpdateAction(actionParameters);
        }
        else if(Action.destroyAction.equals(actionName)) {
            return new DestroyAction(actionParameters);
        }
        else if(Action.moveAction.equals(actionName)) {
            return new MoveAction(actionParameters);
        }
        else if(Action.commandAction.equals(actionName)) {
            return new CommandAction(actionParameters);
        }
        else if(Action.reportAction.equals(actionName)) {
            return new ReportAction(actionParameters);
        }
        else if(Action.followAction.equals(actionName)) {
            return new FollowAction(actionParameters);
        }
        else if(Action.readRecordAction.equals(actionName)) {
            return new ReadRecordAction(actionParameters);
        }
        else if(Action.modelAction.equals(actionName)) {
            return new ModelAction(actionParameters);
        }
        else if(Action.attachAction.equals(actionName)) {
            return new AttachAction(actionParameters);
        }
        else if(Action.detachAction.equals(actionName)) {
            return new DetachAction(actionParameters);
        }
        else {
            throw new RuntimeException("Unknown CRUD action: '" + actionName + "'");
        }
    }

    public static ActionParameters getActionParameters(Query query) {
        ActionFactory factory = new ActionFactory(query, new HashMap<String, String>());
        return factory.getActionParameters();
    }

    private ActionFactory(Query query, Map<String, String> requestParameters) {
        this.requestParameters = requestParameters == null ? RequestTarget.getParameters() : requestParameters;

        if(query != null) {
            requestId = query.classId();
            requestQuery = query;

            model = query.getModel() != null ? query.getModel() : query;
            context = model.getContext();

            String queryId = this.requestParameters.get(Json.queryId);

            if(context != null && queryId != null) {
                Query q = context.findQueryById(queryId);
                context.setRootQuery(q);
                q.setContext(context);
            }
        }
    }

    private ActionParameters getActionParameters() {
        ActionParameters result = new ActionParameters();

        result.requestId = requestId;
        result.requestParameters = this.requestParameters;

        result.requestQuery = requestQuery;

        String queryId = requestParameters.get(Json.queryId);
        String fieldId = requestParameters.get(Json.fieldId);
        String actionName = requestParameters.get(Json.action);

        result.query = model;

        Query fieldsQuery = model;

        if(context != null) {
            if(queryId != null) {
                result.query = context.getRootQuery();

                assert (result.query.id().equals(queryId));

                if(fieldId == null) {
                    Collection<ILink> path = result.query.getPath(model);
                    result.keyField = (Field)path.toArray(new ILink[0])[path.size() - 1];

                    result.fields = context.getFieldsVia(result.query);
                    result.fields.removeAll(context.getFieldsVia(model));
                }

            }
            else if(fieldId == null) {
                result.fields = context.getFieldsVia(model);
            }

            fieldsQuery = context;
        }

        if(actionName == null || actionName.equals(Action.readAction)) {
            result.query.onRender();
        }

        if(fieldId != null) {
            result.fields = new LinkedHashSet<Field>();

            Field field = result.query.findFieldById(fieldId);

            Collection<ILink> path = result.query.getPath(field);

            result.fields.add(field);

            for(ILink link : path) {
                for(Field f : fieldsQuery.getFieldsVia(link.getQuery())) {
                    result.fields.add(f);
                }
            }

            if(context != null && result.query != fieldsQuery && result.query != model) {
                Collection<Field> invisibleFields = model.getReachableFields(result.fields);
                result.fields.removeAll(invisibleFields);
            }

            result.query = path.iterator().next().getQuery();
            result.query.setContext(context != null ? context : requestQuery);

            Collection<Field> columns = field.getColumns();

            for(Field f : columns) {
                if(result.query.findFieldById(f.id()) == f) {
                    result.fields.add(f);
                }
            }

            for(Field f : result.fields) {
                if(f != field && !columns.contains(f)) {
                    f.hidden.set(true);
                }
                else {
                    f.visible.set(true);
                }
            }

            if(!result.fields.isEmpty()) {
                result.sortFields = new LinkedHashSet<Field>();
                result.sortFields.add(field);
            }

            Field modelPrimaryKey = model.primaryKey();

            if(result.query.findFieldById(modelPrimaryKey.id()) != null) {
                result.keyField = modelPrimaryKey;
            }

            result.link = path.iterator().next();
        }

        if(result.query != null) {
            if(actionName == null) {
                result.groupFields = new LinkedHashSet<Field>();
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
            }
            else {
                result.groupFields = getGroupFields(result.query);
                result.sortFields = getSortFields(result.query);
            }

            result.groupBy = result.query.collectGroupByFields();
            result.aggregateBy = result.query.collectAggregateByFields();

            result.totalsBy = getTotalsBy(result.query);
            setAggregation(result.query);

            RCollection<guid> ids = getIds();

            if(ids != null) {
                result.query.recordIds = ids;
            }
        }

        return result;
    }

    private RCollection<guid> getIds() {
        String jsonData = requestParameters.get(Json.ids);

        if(jsonData == null || jsonData.isEmpty()) {
            return null;
        }

        RCollection<guid> ids = new RCollection<guid>();

        JsonArray array = new JsonArray(jsonData);

        if(array != null) {
            int length = array.length();
            for(int index = 0; index < length; index++) {
                ids.add(new guid(array.getString(index)));
            }
        }

        return ids;
    }

    private Field getTotalsBy(Query query) {
        String totalsBy = requestParameters.get(Json.totalsBy);
        return totalsBy != null ? query.findFieldById(totalsBy) : null;
    }

    private void setAggregation(Query query) {
        String aggregation = requestParameters.get(Json.aggregation);

        if(aggregation != null) {
            JsonArray array = new JsonArray(aggregation);

            for(int index = 0; index < array.length(); index++) {
                Field field = query.findFieldById(array.getString(index));

                if(field != null) {
                    field.aggregation = Aggregation.Sum;
                }
            }
        }
    }

    private Collection<Field> parseSortFieldsInOldSchoolManner(Query query, String jsonData) {
        Collection<Field> fields = new ArrayList<Field>();

        Field field = query.findFieldById(jsonData);
        String dir = requestParameters.get(Json.dir);

        if(field != null) {
            field.sortDirection = SortDirection.fromString(dir);
            fields.add(field);
        }
        
        return fields;
    }
    
    private Collection<Field> parseSortFields(Query query) {
        Collection<Field> fields = new ArrayList<Field>();

        String jsonData = requestParameters.get(Json.sort);

        if(jsonData == null || jsonData.isEmpty()) {
            return fields;
        }

        if(jsonData.charAt(0) != '[') {
            return parseSortFieldsInOldSchoolManner(query, jsonData);
        }

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
        Collection<Field> fields = new ArrayList<Field>();

        String groupDir = requestParameters.get(Json.groupDir);
        String jsonData = requestParameters.get(Json.groupBy);

        if(jsonData == null) {
            return fields;
        }

        JsonArray array = new JsonArray(jsonData);

        for(int index = 0; index < array.length(); index++) {
            Field field = query.findFieldById(array.getString(index));

            if(field != null) {
                field.sortDirection = groupDir == null || groupDir.isEmpty() ? SortDirection.Asc : SortDirection
                        .fromString(groupDir);
                fields.add(field);
            }
        }

        return fields;
    }

    private Collection<Field> getGroupFields(Query query) {
        return parseGroupFields(query);
    }
    
    private Collection<Field> getSortFields(Query query) {
        Collection<Field> sortFields = parseSortFields(query);
        Collection<Field> groupFields = parseGroupFields(query);

        if(sortFields.isEmpty() && groupFields.isEmpty()) {
            return null;
        }

        Collection<Field> fields = new LinkedHashSet<Field>();
        fields.addAll(groupFields);
        fields.addAll(sortFields);

        return fields;
    }
}
