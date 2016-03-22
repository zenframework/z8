package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class Control extends OBJECT {
    public Control(IObject container) {
        super(container);
    }

    public static class CLASS<T extends Control> extends OBJECT.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(Control.class);
            setAttribute(Native, Control.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Control(container);
        }
    }

    public integer rowspan = new integer(1);
    public integer colspan = new integer(1);
    public bool showLabel = new bool(true);
    public bool hidable = new bool(false);

    public void writeMeta(JsonWriter writer) {
        writer.writeProperty(Json.id, id());
        writer.writeProperty(Json.header, displayName());
        writer.writeProperty(Json.description, description());
        writer.writeProperty(Json.label, label());
        writer.writeProperty(Json.rowspan, rowspan);
        writer.writeProperty(Json.colspan, colspan);
        writer.writeProperty(Json.showLabel, showLabel);
        writer.writeProperty(Json.hidable, hidable);
    }
}
