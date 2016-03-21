package org.zenframework.z8.server.base.model.actions;

import java.util.List;

import org.zenframework.z8.server.base.file.AttachmentProcessor;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.types.guid;

public class AttachAction extends Action {

    public AttachAction(ActionParameters parameters) {
        super(parameters);
    }

    @Override
    public void writeResponse(JsonWriter writer) throws Throwable {

        IRequest request = request();
        List<FileInfo> files = request.getFiles();

        Query query = getRootQuery();
        guid target = getRecordIdParameter();
        String fieldId = getFieldParameter();
        String type = getTypeParameter();
        
        Field field = fieldId != null ? query.findFieldById(fieldId) : null;
        
        AttachmentProcessor processor = new AttachmentProcessor((AttachmentField) field);

        writer.startArray(Json.data);
        
        for(FileInfo file : processor.update(target, files, type))
            writer.write(file.toJsonObject());

        writer.finishArray();
    }
}
