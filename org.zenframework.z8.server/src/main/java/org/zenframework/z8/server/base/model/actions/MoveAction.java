package org.zenframework.z8.server.base.model.actions;

import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.guid;

public class MoveAction extends Action {
    public MoveAction(ActionParameters parameters) {
        super(parameters);
    }

    @Override
    public void writeResponse(JsonWriter writer) {
        TreeTable table = (TreeTable)getRootQuery();

        guid recordId = getRecordIdParameter();
        guid parentId = getParentIdParameter();

        table.move(recordId, parentId);

        writer.startArray(Json.data);
        writer.finishArray();
    }
}
