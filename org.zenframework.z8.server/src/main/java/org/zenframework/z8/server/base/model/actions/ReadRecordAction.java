package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.model.sql.Select;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;

public class ReadRecordAction extends ReadAction {
    public ReadRecordAction(ActionParameters parameters) {
        super(parameters, parameters.getRecordId());
    }

    @Override
    public void writeResponse(JsonObject writer) throws Throwable {
        Select cursor = getCursor();

        try {
            Collection<Field> fetchedFields = cursor.getFields();

            Collection<Field> fields = new ArrayList<Field>();

            for(Field field : fetchedFields) {
                if(field.system.get() || field instanceof GuidField || field == getQuery().lockKey() || field.hidden.get()) {
                    continue;
                }
                fields.add(field);
            }

            JsonArray fieldsArr = new JsonArray();
            for(Field field : fields) {
                JsonObject metaObj = new JsonObject();
                field.writeMeta(metaObj);
                fieldsArr.put(metaObj);
            }
            writer.put(Json.fields, fieldsArr);

            JsonArray dataArr = new JsonArray();
            if(cursor.next()) {
                for(Field field : fields) {
                    JsonObject dataObj = new JsonObject();
                    field.writeData(dataObj);
                    dataArr.put(dataObj);
                }
            }
            writer.put(Json.data, dataArr);
        }
        finally {
            cursor.close();
        }
    }
}
