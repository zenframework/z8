package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.base.form.Control;
import org.zenframework.z8.server.base.form.FieldGroup;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;

public class MetaAction extends ReadAction {
    public static final String StartValue = "0";
    public static final String LimitValue = "50";

    public MetaAction(ActionParameters actionParameters) {
        super(actionParameters);
    }

    @Override
    public void writeResponse(JsonObject writer) throws Throwable {
        ActionParameters actionParameters = actionParameters();
        Map<String, String> requestParameters = actionParameters.requestParameters;

        Query query = getQuery();

        writer.put(Json.isQuery, true);

        writer.put(Json.queryId, requestParameters.get(Json.queryId));

        if(actionParameters.link != null) {
            writer.put(Json.fieldId, requestParameters.get(Json.fieldId));
            writer.put(Json.linkId, actionParameters.link.id());
        }

        Collection<Field> fields = getSelectFields();
        query.writeMeta(writer, fields);

        writeSortFields(writer, actionParameters.sortFields);
        writeGroupFields(writer, actionParameters.groupFields);

        writeSections(writer, fields);
        writeSections(writer, fields);

        requestParameters.put(Json.start.get(), StartValue);
        requestParameters.put(Json.limit.get(), LimitValue);

        requestParameters.put(Json.limit.get(), LimitValue);

        if(query.showAsTree()) {
            requestParameters.put(Json.parentId.get(), guid.NULL.toString());
        }

        super.writeResponse(writer);
    }

    private void writeSortFields(JsonObject writer, Collection<Field> sortFields) {
        if(!sortFields.isEmpty()) {
            Field field = sortFields.iterator().next();

            writer.put(Json.sort, field.id());
            writer.put(Json.direction, field.sortDirection.toString());
        }
    }

    private void writeGroupFields(JsonObject writer, Collection<Field> groupFields) {
        if(!groupFields.isEmpty()) {
            JsonArray groupArr = new JsonArray();

            for(Field field : groupFields) {
                groupArr.put(field.id());
                // writer.writeProperty(Json.groupDir,
                // field.sortDirection.toString());
                // writer.writeProperty(Json.collapseGroups,
                // collapseGroups);
            }
            writer.put(Json.groupBy, groupArr);
        }
    }

    class Section {
        FieldGroup group = null;
        Collection<Object> controls = new ArrayList<Object>();

        Section(FieldGroup group) {
            this.group = group;
        }

        void add(Object control) {
            controls.add(control);
        }

        boolean isEmpty() {
            return controls.isEmpty();
        }
    }

    private Section getSections(FieldGroup group, Collection<Field> fields) {
        Section result = new Section(group);

        Collection<Control> controls = group == null ? collectControls() : group.getControls();

        for(Control control : controls) {
            if(control instanceof Field) {
                if(fields.contains(control)) {
                    result.add(control);
                }
            }
            else if(control instanceof FieldGroup) {
                Section section = getSections((FieldGroup)control, fields);

                if(section != null) {
                    result.add(section);
                }
            }
        }

        return result.isEmpty() ? null : result;
    }

    @SuppressWarnings("unchecked")
    private Collection<Control> collectControls() {
        Query query = getQuery();

        if(actionParameters().link != null) {
            return (Collection)getSelectFields();
        }

        Collection<Control> controls = new ArrayList<Control>();

        Query context = query.getContext();

        if(context != null) {
            controls = context.getControls();
        }

        if(controls.isEmpty()) {
            controls = query.getControls();
        }

        Query rootQuery = query.getRootQuery();

        if(controls.isEmpty() && rootQuery != query) {
            controls = rootQuery.getControls();
        }

        return controls;
    }

    private void writeSections(JsonObject writer, Collection<Field> fields) {
        Section section = getSections(null, fields);

        if(section == null) {
            return;
        }

        JsonObject sectionObj = new JsonObject();
        writeSection(sectionObj, section);
        writer.put(Json.section, sectionObj);
    }

    private void writeSection(JsonObject writer, Section section) {
        if(section.group != null) {
            section.group.writeMeta(writer);
        }

        writer.put(Json.isSection, true);

        JsonArray controlsArr = new JsonArray();

        for(Object control : section.controls) {
            if(control instanceof Field) {
                Field field = (Field)control;
                if(!field.system.get()) {
                    JsonObject obj = new JsonObject();
                    obj.put(Json.id, field.id());
                    controlsArr.put(obj);
                }
            }
            else if(control instanceof Section) {
                JsonObject obj = new JsonObject();
                writeSection(obj, (Section)control);
                controlsArr.put(obj);
            }
        }

        writer.put(Json.controls, controlsArr);
    }
}
