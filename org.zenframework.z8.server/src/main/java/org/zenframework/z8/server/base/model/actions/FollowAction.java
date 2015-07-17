package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.query.Period;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.types.guid;

public class FollowAction extends MetaAction {
    public FollowAction(ActionParameters parameters) {
        super(getActionParameters(parameters));
    }

    static private ActionParameters getActionParameters(ActionParameters actionParameters) {
        String jsonData = actionParameters.requestParameters.get(Json.link);

        JsonObject object = new JsonObject(jsonData);

        Collection<guid> ids = new ArrayList<guid>();

        String recordIdString = object.has(Json.recordId) ? object.getString(Json.recordId) : null;

        if(recordIdString != null) {
            ids.add(new guid(recordIdString));
        }
        else {
            JsonArray array = object.has(Json.groups) ? object.getJsonArray(Json.groups) : null;

            if(array != null) {
                int length = array.length();
                for(int index = 0; index < length; index++) {
                    ids.add(new guid(array.getString(index)));
                }
            }
        }

        Field field = actionParameters.query.findFieldById(object.getString(Json.fieldId));

        Query query = actionParameters.query.onFollow(field, ids);

        if(query == null) {
            throw new RuntimeException("No query to follow for field '" + field.getOwner().toString() + '.'
                    + field.displayName() + "'.");
        }

        Query query1 = (Query)Loader.getInstance(query.classId());

        query1.recordIds = query.recordIds;

        String periodData = actionParameters.requestParameters.get(Json.period);

        if(periodData != null && !periodData.isEmpty()) {
            query1.setPeriod(Period.parse(periodData));
        }

        return ActionFactory.getActionParameters(query1);
    }
}
