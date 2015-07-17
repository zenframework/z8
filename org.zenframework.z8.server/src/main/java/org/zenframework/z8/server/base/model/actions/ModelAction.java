package org.zenframework.z8.server.base.model.actions;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;

public class ModelAction extends Action {
    public ModelAction(ActionParameters parameters) {
        super(parameters);
    }

    @Override
    public void writeResponse(JsonObject writer) {
        Query query = getQuery();

        if(query.getContext() != null) {
            query = query.getContext();
        }

        writer.put(Json.isQuery, true);

        writer.put(Json.readOnly, true);

        writer.put(Json.primaryKey, Json.id);
        writer.put(Json.parentKey, Json.parentId);
        writer.put(Json.children, Json.children);

        JsonArray fieldsArray = new JsonArray();

        JsonObject idObj = new JsonObject();
        idObj.put(Json.id, Json.id);
        idObj.put(Json.serverType, FieldType.String.toString());
        fieldsArray.put(idObj);

        JsonObject parentIdObj = new JsonObject();
        parentIdObj.put(Json.id, Json.parentId);
        parentIdObj.put(Json.serverType, FieldType.String.toString());
        fieldsArray.put(parentIdObj);

        JsonObject nameObj = new JsonObject();
        nameObj.put(Json.id, Json.name);
        nameObj.put(Json.serverType, FieldType.String.toString());
        fieldsArray.put(nameObj);

        JsonObject displayNameObj = new JsonObject();
        displayNameObj.put(Json.id, Json.displayName);
        displayNameObj.put(Json.serverType, FieldType.String.toString());
        fieldsArray.put(displayNameObj);

        JsonObject childrenObj = new JsonObject();
        childrenObj.put(Json.id, Json.children);
        childrenObj.put(Json.serverType, FieldType.Integer.toString());
        fieldsArray.put(childrenObj);

        writer.put(Json.fields, fieldsArray);

        writer.put(Json.sort, Json.displayName);

        JsonObject sectionObj = new JsonObject();
        
        sectionObj.put(Json.isSection, true);
        
        JsonArray controlsArr = new JsonArray();
        
        displayNameObj = new JsonObject();
        displayNameObj.put(Json.id, Json.displayName);
        displayNameObj.put(Json.header, "Display Name");
        controlsArr.put(displayNameObj);

        nameObj = new JsonObject();
        nameObj.put(Json.id, Json.name);
        nameObj.put(Json.header, "Name");
        controlsArr.put(nameObj);

        idObj = new JsonObject();
        idObj.put(Json.id, Json.id);
        idObj.put(Json.header, "Id");
        controlsArr.put(idObj);
        
        sectionObj.put(Json.controls, controlsArr);
        
        writer.put(Json.section, sectionObj);

        JsonArray dataArr = new JsonArray();

        String parentId = actionParameters().requestParameters.get(Json.parentId);

        boolean root = parentId == null || parentId.isEmpty();

        if(!root) {
            query = query.findQueryById(parentId);

            for(Query subquery : query.getQueries()) {
                JsonObject queryObj = new JsonObject();
                queryObj.put(Json.id, subquery.id());
                queryObj.put(Json.parentId, parentId);
                queryObj.put(Json.name, subquery.name());
                queryObj.put(Json.displayName, subquery.displayName());
                queryObj.put(Json.children, subquery.queries().size());
                dataArr.put(queryObj);
            }
        }
        else {
            JsonObject queryObj = new JsonObject();
            queryObj.put(Json.id, query.id());
            queryObj.put(Json.parentId, "");
            queryObj.put(Json.name, query.name());
            queryObj.put(Json.displayName, query.displayName());
            queryObj.put(Json.children, query.queries().size());
            dataArr.put(queryObj);
        }

        writer.put(Json.data, dataArr);
    }
}
