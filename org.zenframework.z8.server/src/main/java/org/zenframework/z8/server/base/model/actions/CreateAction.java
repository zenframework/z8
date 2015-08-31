package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.query.Style;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;

public class CreateAction extends Action {
    public CreateAction(ActionParameters parameters) {
        super(parameters);
    }

    @Override
    public void writeResponse(JsonObject writer) {
        Field backwardLink = actionParameters().keyField;

        String jsonData = getDataParameter();

        JsonArray fieldsArr = new JsonArray();

        if(jsonData.charAt(0) == '{') {
            jsonData = "[" + jsonData + "]";
        }

        JsonArray records = new JsonArray(jsonData);

        Query query = getQuery();
        Query rootQuery = getRootQuery();

        Field primaryKey = rootQuery.primaryKey();
        Field parentKey = rootQuery.parentKey();

        assert (primaryKey != null);

        for(int index = 0; index < records.length(); index++) {
            JsonObject record = (JsonObject)records.get(index);

            Collection<Field> fields = new ArrayList<Field>();
            QueryUtils.parseRecord(record, query, fields);

            if(!fields.contains(primaryKey))
                fields.add(primaryKey);
            
            guid recordId = primaryKey.guid();

            if(recordId.isNull()) {
                recordId = guid.create();
                primaryKey.set(recordId);
            }
            
            guid parentId = parentKey != null ? parentKey.guid() : null;
            guid modelRecordId = getRecordIdParameter();

            if(backwardLink != null) {
                backwardLink.set(getRecordIdParameter());
            }

            query.insert(recordId, parentId, modelRecordId != null ? modelRecordId : recordId);

            if(query.readRecord(recordId, fields)) {
                JsonObject fieldObj = new JsonObject();

                for(Field field : fields) {
                    field.writeData(fieldObj);
                }

                Style style = query.renderRecord();

                if(style != null) {
                    style.write(fieldObj);
                }

                fieldsArr.put(fieldObj);
            }
            
            primaryKey.set(guid.NULL);
        }

        writer.put(Json.data, fieldsArr);
    }
}
