package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
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

    public void writeMeta(JsonObject writer) {
        writer.put(Json.id, id());
        writer.put(Json.header, displayName());
        writer.put(Json.description, description());
        writer.put(Json.label, label());
        writer.put(Json.rowspan, rowspan);
        writer.put(Json.colspan, colspan);
        writer.put(Json.showLabel, showLabel);
        writer.put(Json.hidable, hidable);
    }
}
