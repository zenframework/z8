package org.zenframework.z8.server.base.model.actions;

import java.sql.SQLException;
import java.util.Collection;

import org.zenframework.z8.server.base.model.sql.Select;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.Style;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

public class NewAction extends Action {
    public NewAction(ActionParameters parameters) {
        super(parameters);
    }

    @Override
    public void writeResponse(JsonObject writer) throws Throwable {
        Query query = getQuery();

        guid recordId = guid.create();
        guid parentId = getParentIdParameter();
        
        Field backwardLink = actionParameters().keyField;

        if(backwardLink != null) {
            backwardLink.set(getRecordIdParameter());
        }

        Collection<Field> fields = run(query, recordId, parentId);

        initLinks(fields);

        JsonObject data = new JsonObject();

        for(Field field : fields) {
            field.writeData(data);
        }

        Style style = query.renderRecord();

        if(style != null) {
            style.write(data);
        }

        JsonArray array = new JsonArray();
        array.put(data);
        
        writer.put(Json.data, array);
    }

    private void initLinks(Collection<Field> fields) throws SQLException {
        for(Field field : fields.toArray(new Field[0])) // ConcurrentModification
        {
            if(field instanceof Link) {
                Link link = (Link)field;
                guid recordId = link.guid();

                if(!recordId.equals(guid.NULL)) {
                    Query query = link.getQuery();

                    ActionParameters parameters = actionParameters();
                    Collection<Field> queryFields = parameters.fields != null ? parameters.fields : query.getFormFields();
                    queryFields = query.getReachableFields(queryFields);

                    if(queryFields.size() != 0) {
                        ReadAction action = new ReadAction(query, queryFields);
                        action.addFilter(query.primaryKey(), recordId);
    
                        Select cursor = action.getCursor();
    
                        try {
                            if(cursor.next()) {
                                for(Field actionField : action.selectFields) {
                                    actionField.set(actionField.get());
    
                                    if(!fields.contains(actionField)) {
                                        fields.add(actionField);
                                    }
                                }
                            }
                        }
                        finally {
                            cursor.close();
                        }
                    }
                }
            }
        }
    }

    static private Collection<Field> initFields(Collection<Field> fields, guid recordId, guid parentId) {
        for(Field field : fields) {
            if(!field.changed()) {
                if(field.isPrimaryKey()) {
                    field.set(recordId);
                }
                else if(field.isParentKey() && parentId != null) {
                    field.set(parentId);
                }
                else {
                    primary value = field.getDefault();

                    if(!value.equals(field.getDefaultValue())) {
                        field.set(value);
                    }
                }
            }
        }

        return fields;
    }

    static public Collection<Field> run(Query query, guid recordId, guid parentId) {
        Collection<Field> fields = query.getRootQuery().getDataFields();

        query.onNew(recordId, parentId != null ? parentId : guid.NULL);

        initFields(fields, recordId, parentId);

        return fields;
    }
}
