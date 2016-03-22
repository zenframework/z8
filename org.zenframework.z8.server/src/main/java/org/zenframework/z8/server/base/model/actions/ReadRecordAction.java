package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.model.sql.Select;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;

public class ReadRecordAction extends ReadAction {
    public ReadRecordAction(ActionParameters parameters) {
        super(parameters, parameters.getRecordId());
    }

    @Override
    public void writeResponse(JsonWriter writer) throws Throwable {
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

            writer.startArray(Json.fields);
            for(Field field : fields) {
            	writer.startObject();
                field.writeMeta(writer);
                writer.finishObject();
            }
            writer.finishArray();

            writer.startArray(Json.data);
            if(cursor.next()) {
                for(Field field : fields) {
                    writer.startObject();
                    field.writeData(writer);
                    writer.finishObject();
                }
            }
            writer.finishArray();
        }
        finally {
            cursor.close();
        }
    }
}
