package org.zenframework.z8.server.base.model.actions;

import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;

public class MoveAction extends Action {
    public MoveAction(ActionParameters parameters) {
        super(parameters);
    }

    @Override
    public void writeResponse(JsonObject writer) {
        TreeTable table = (TreeTable)getRootQuery();

        guid recordId = getRecordIdParameter();
        guid parentId = getParentIdParameter();

        table.move(recordId, parentId);

        writer.put(Json.data, new JsonArray());
    }
}
