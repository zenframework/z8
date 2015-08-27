package org.zenframework.z8.server.base.query;

import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class Style extends OBJECT {
    public static class CLASS<T extends Style> extends OBJECT.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(Style.class);
            setAttribute(Native, Style.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Style(container);
        }
    }

    public Color color = Color.Black;
    public Color background = Color.White;

    public Style(IObject container) {
        super(container);
    }

    public boolean isDefault() {
        return color == Color.Black && background == Color.White;
    }

    @Override
    public void write(JsonObject writer) {
        if(!isDefault()) {
            JsonObject styleObj = new JsonObject();
            styleObj.put(Json.color, color.toString());
            styleObj.put(Json.background, background.toString());
            writer.put(Json.style, styleObj);
        }
    }

    static public Style.CLASS<? extends Style> z8_create(Color color) {
        return z8_create(color, Color.Black);
    }

    static public Style.CLASS<? extends Style> z8_create(Color color, Color background) {
        Style.CLASS<? extends Style> style = new Style.CLASS<Style>();
        style.get().color = color;
        style.get().background = background;
        return style;
    }
}