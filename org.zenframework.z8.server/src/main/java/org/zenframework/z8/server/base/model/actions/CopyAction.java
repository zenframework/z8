package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.query.Style;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

public class CopyAction extends Action {
    public CopyAction(ActionParameters parameters) {
        super(parameters);
    }

    @Override
    public void writeResponse(JsonObject writer) {
        Query query = getQuery();

        guid sourceRecordId = getSourceParameter();
        guid parentId = getParentIdParameter();
        guid modelRecordId = getRecordIdParameter();

        run(query, sourceRecordId, parentId, modelRecordId);

        Collection<Field> fields = new ArrayList<Field>();

        String jsonData = getDataParameter();

        JsonObject record = new JsonObject(jsonData);

        for(String fieldId : JsonObject.getNames(record)) {
            Field field = query.findFieldById(fieldId);

            if(field != null) {
                if(canCopy(field) && !field.changed()) {
                    String value = record.getString(fieldId);
                    QueryUtils.setFieldValue(field, value);
                }

                fields.add(field);
            }
        }

        JsonArray arr = new JsonArray();

        JsonObject obj = new JsonObject();

        for(Field field : fields) {
            field.writeData(obj);
        }

        Style style = query.renderRecord();

        if(style != null) {
            style.write(obj);
        }

        arr.put(obj);

        writer.put(Json.data, obj);
    }

    static private boolean canCopy(Field field) {
        return !field.isPrimaryKey() && !field.unique.get();
    }

    static public guid run(Query query, guid sourceId, guid parentId, guid modelRecordId) {
        guid newRecordId = guid.create();

        Collection<Field> changed = query.getRootQuery().getChangedFields();

        NewAction.run(query, newRecordId, parentId, modelRecordId);

        Collection<Field> fields = query.getRootQuery().getDataFields();
        Map<Field, primary> values = new HashMap<Field, primary>();

        try {
            query.saveState();

            if(query.readRecord(sourceId, fields)) {
                for(Field field : fields) {
                    values.put(field, field.get());
                }
            }
        }
        finally {
            query.restoreState();
        }

        for(Field field : values.keySet()) {
            if(canCopy(field) && !changed.contains(field)) {
                field.set(values.get(field));
            }
        }

        query.onCopy();

        return newRecordId;
    }
}
