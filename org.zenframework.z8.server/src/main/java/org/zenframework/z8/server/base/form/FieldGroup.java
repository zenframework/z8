package org.zenframework.z8.server.base.form;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.integer;

public class FieldGroup extends Control {
    public static class CLASS<T extends FieldGroup> extends Control.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(FieldGroup.class);
            setAttribute(Native, FieldGroup.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new FieldGroup(container);
        }
    }

    public integer columns = new integer(3);

    public RCollection<Control.CLASS<? extends Control>> controls = new RCollection<Control.CLASS<? extends Control>>(true);

    public FieldGroup(IObject container) {
        super(container);
    }

    @SuppressWarnings("unchecked")
    public Collection<Field.CLASS<Field>> fields() {
        Collection<Field.CLASS<Field>> result = new LinkedHashSet<Field.CLASS<Field>>();

        for(Control.CLASS control : controls) {
            if(control instanceof Field.CLASS) {
                result.add((Field.CLASS)control);
            }
            else if(control instanceof FieldGroup.CLASS) {
                FieldGroup group = (FieldGroup)control.get();
                result.addAll(group.fields());
            }
            else {
                assert (false);
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public Collection<Control.CLASS<Control>> controls() {
        return (Collection)controls;
    }

    public Collection<Control> getControls() {
        return CLASS.asList(controls);
    }

    @Override
    public void writeMeta(JsonObject writer) {
        super.writeMeta(writer);
        writer.put(Json.columns, columns);
    }
}
