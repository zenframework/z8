package org.zenframework.z8.server.base.model.actions;

import org.zenframework.z8.server.base.model.sql.Delete;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;

public class DestroyAction extends Action {
    public DestroyAction(ActionParameters parameters) {
        super(parameters);
    }

    @Override
    public void writeResponse(JsonObject writer) {
        String jsonData = getDataParameter();

        if(jsonData.charAt(0) != '[') {
            jsonData = "[" + jsonData + "]";
        }

        JsonArray records = new JsonArray(jsonData);

        for(int index = 0; index < records.length(); index++) {
            Object object = records.get(index);
            // data: [{recordId: guid}] or data: [guid] 
            String value = object instanceof JsonObject ? ((JsonObject)object).getString(Json.recordId) : (String)object;
            guid recordId = new guid(value);
            guid modelRecordId = getRecordIdParameter();
            run(getQuery(), recordId, modelRecordId != null ? modelRecordId : recordId);
        }
    }

    static public void run(Query query, guid id, guid modelRecordId) {
        if(!guid.NULL.equals(id)) {
            Query model = Query.getModel(query);

            query.beforeDestroy(id, model, modelRecordId);

            new Delete(query, id).execute();

            query.afterDestroy(id, model, modelRecordId);
        }
    }
}
