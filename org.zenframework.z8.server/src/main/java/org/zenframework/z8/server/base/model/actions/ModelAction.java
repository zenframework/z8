package org.zenframework.z8.server.base.model.actions;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;

public class ModelAction extends Action {
    public ModelAction(ActionParameters parameters) {
        super(parameters);
    }

    @Override
    public void writeResponse(JsonWriter writer) {
        Query query = getQuery();

        if(query.getContext() != null) {
            query = query.getContext();
        }

        writer.writeProperty(Json.isQuery, true);

        writer.writeProperty(Json.readOnly, true);

        writer.writeProperty(Json.primaryKey, Json.id);
        writer.writeProperty(Json.parentKey, Json.parentId);
        writer.writeProperty(Json.children, Json.children);

        writer.startArray(Json.fields);

        writer.startObject();
        writer.writeProperty(Json.id, Json.id);
        writer.writeProperty(Json.serverType, FieldType.String.toString());
        writer.finishObject();

        writer.startObject();
        writer.writeProperty(Json.id, Json.parentId);
        writer.writeProperty(Json.serverType, FieldType.String.toString());
        writer.finishObject();

        writer.startObject();
        writer.writeProperty(Json.id, Json.name);
        writer.writeProperty(Json.serverType, FieldType.String.toString());
        writer.finishObject();

        writer.startObject();
        writer.writeProperty(Json.id, Json.displayName);
        writer.writeProperty(Json.serverType, FieldType.String.toString());
        writer.finishObject();

        writer.startObject();
        writer.writeProperty(Json.id, Json.children);
        writer.writeProperty(Json.serverType, FieldType.Integer.toString());
        writer.finishObject();

        writer.finishArray();

        writer.writeProperty(Json.sort, Json.displayName);

        writer.startObject(Json.section);
        
        writer.writeProperty(Json.isSection, true);
        
        writer.startArray(Json.controls);
        
        writer.startObject();
        writer.writeProperty(Json.id, Json.displayName);
        writer.writeProperty(Json.header, "Display Name");
        writer.finishObject();

        writer.startObject();
        writer.writeProperty(Json.id, Json.name);
        writer.writeProperty(Json.header, "Name");
        writer.finishObject();

        writer.startObject();
        writer.writeProperty(Json.id, Json.id);
        writer.writeProperty(Json.header, "Id");
        writer.finishObject();
        
        writer.finishArray();
        
        writer.finishObject();

        writer.startArray(Json.data);

        String parentId = actionParameters().requestParameters.get(Json.parentId);

        boolean root = parentId == null || parentId.isEmpty();

        if(!root) {
            query = query.findQueryById(parentId);

            for(Query subquery : query.getQueries()) {
                writer.startObject();
                writer.writeProperty(Json.id, subquery.id());
                writer.writeProperty(Json.parentId, parentId);
                writer.writeProperty(Json.name, subquery.name());
                writer.writeProperty(Json.displayName, subquery.displayName());
                writer.writeProperty(Json.children, subquery.queries().size());
                writer.finishObject();
            }
        }
        else {
            writer.startObject();
            writer.writeProperty(Json.id, query.id());
            writer.writeProperty(Json.parentId, "");
            writer.writeProperty(Json.name, query.name());
            writer.writeProperty(Json.displayName, query.displayName());
            writer.writeProperty(Json.children, query.queries().size());
            writer.finishObject();
        }

        writer.finishArray();
    }
}
