package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.model.sql.Update;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;

public class UpdateAction extends Action {
    public UpdateAction(ActionParameters parameters) {
        super(parameters);
    }

    @Override
    public void writeResponse(JsonObject writer) {
        String jsonData = getDataParameter();

        if(jsonData.charAt(0) == '{') {
            jsonData = "[" + jsonData + "]";
        }

        JsonArray records = new JsonArray(jsonData);

        Query query = getQuery();

        for(int index = 0; index < records.length(); index++) {
            JsonObject record = (JsonObject)records.get(index);

            List<Field> fields = new ArrayList<Field>();

            guid keyValue = QueryUtils.parseRecord(record, query, fields);

            guid modelRecordId = getRecordIdParameter();

            run(query, keyValue, fields, modelRecordId != null ? modelRecordId : keyValue);
        }
    }

    static public void run(Query query, guid keyValue, Collection<Field> fields, guid modelRecordId) {
        if(!fields.isEmpty() && (keyValue == null || !keyValue.equals(guid.NULL))) {
            Query model = Query.getModel(query);

            if(keyValue != null)
                query.beforeUpdate(keyValue, fields, model, modelRecordId);

            Collection<Field> changedFields = query.getRootQuery().getChangedFields();

            new Update(query, changedFields, keyValue).execute();

            if(keyValue != null)
                query.afterUpdate(keyValue, fields, model, modelRecordId);

            for(Field field : fields)
                field.reset();
        }
    }
}
