package org.zenframework.z8.server.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class JsonWriter {
    StringBuilder stream;
    List<Boolean> scopes;
    boolean quoteName = false;

    public JsonWriter() {
        reset();
    }

    public JsonWriter(boolean quoteName) {
        this();

        this.quoteName = quoteName;
    }

    public void reset() {
        stream = new StringBuilder();
        scopes = new ArrayList<Boolean>();
        openScope();
    }

    private String comma() {
        int size = scopes.size();
        return (size > 0 && scopes.get(size - 1)) ? "," : "";
    }

    private void openScope() {
        scopes.add(false);
    }

    private void closeScope() {
        scopes.remove(scopes.size() - 1);
        startSeparate();
    }

    private void startSeparate() {
        if (scopes.size() != 0) {
            scopes.set(scopes.size() - 1, true);
        }
    }

    public void startObject(string name) {
        startObject(name != null ? name.get() : null);
    }

    public void startObject(String name) {
        if (name != null) {
            stream.append(comma() + name + ":" + '{');
            openScope();
        } else {
            startObject();
        }
    }

    public void startObject() {
        stream.append(comma() + '{');
        openScope();
    }

    public void finishObject() {
        stream.append('}');
        closeScope();
    }

    public void startArray(string name) {
        startArray(name != null ? name.get() : null);
    }

    public void startArray(String name) {
        if (name != null) {
            name = quoteName ? JsonObject.quote(name) : name;
            stream.append(comma() + name + ":" + '[');
            openScope();
        } else {
            startArray();
        }

    }

    public void startArray() {
        stream.append(comma() + '[');
        openScope();
    }

    public void finishArray() {
        stream.append(']');
        closeScope();
    }

    public void write(String value) {
        write(value, true);
    }

    public void write(String value, boolean quote) {
        if (value != null) {
            stream.append(comma() + (quote ? JsonObject.quote(value) : value));
            startSeparate();
        }
    }

    public void write(double value) {
        write(Double.toString(value), false);
    }

    public void write(BigDecimal value) {
        write(value.toString(), false);
    }

    public void write(long value) {
        write(Long.toString(value), false);
    }

    public void write(int value) {
        write(Integer.toString(value), false);
    }

    public void write(boolean value) {
        write(Boolean.toString(value), false);
    }

    public void write(FileInfo value) {
        startObject();
        writeProperty(Json.name, value.name);
        writeProperty(Json.type, value.type);
        writeProperty(Json.path, value.path);
        writeProperty(Json.id, value.id);
        finishObject();
    }

    public void write(primary value) {
        if (value == null) {
            write("null", false);
        } else if (value instanceof bool) {
            write(((bool) value).get());
        } else if (value instanceof integer) {
            write(((integer) value).get());
        } else if (value instanceof decimal) {
            write(((decimal) value).get());
        } else if (value instanceof date) {
            date d = (date) value;
            boolean minMax = d.equals(date.MIN) || d.equals(date.MAX);
            write(minMax ? "" : value.toString());
        } else if (value instanceof datetime) {
            datetime dt = (datetime) value;
            boolean minMax = dt.equals(datetime.MIN) || dt.equals(datetime.MAX);
            write(minMax ? "" : value.toString());
        } else {
            write(value.toString());
        }
    }

    public void write(RCollection<primary> value) {
        startArray();
        for (primary v : value) {
            write(v);
        }
        finishArray();
    }
    
    public void write(JsonObject object) {
        // TODO
    }

    public void writeProperty(string name, String value) {
        writeProperty(name.get(), value, true);
    }

    public void writeProperty(String name, String value, boolean quoteValue) {
        if (value != null) {
            name = quoteName ? JsonObject.quote(name) : name;
            value = quoteValue ? JsonObject.quote(value) : value;
            stream.append(comma() + name + ":" + value);
            startSeparate();
        }
    }

    public void writeProperty(string name, primary value) {
        writeProperty(name.get(), value);
    }

    public void writeProperty(String name, primary value) {
        if (value == null) {
            writeNull(name);
        } else if (value instanceof bool) {
            writeProperty(name, ((bool) value).get());
        } else if (value instanceof integer) {
            writeProperty(name, ((integer) value).get());
        } else if (value instanceof decimal) {
            writeProperty(name, ((decimal) value).get());
        } else if (value instanceof date) {
            date d = (date) value;
            boolean minMax = d.equals(date.MIN) || d.equals(date.MAX);
            writeProperty(name, minMax ? "" : value.toString(), true);
        } else if (value instanceof datetime) {
            datetime dt = (datetime) value;
            boolean minMax = dt.equals(datetime.MIN) || dt.equals(datetime.MAX);
            writeProperty(name, minMax ? "" : value.toString(), true);
        } else {
            writeProperty(name, value.toString(), true);
        }
    }

    public void writeProperty(IValue field) {
        FieldType type = field.type();

        if (type == FieldType.Boolean || type == FieldType.Integer || type == FieldType.Decimal) {
            writeProperty(field.id(), field.get());
        } else {
            writeProperty(field.id(), field.get());
        }
    }

    public void writeProperty(string name, double value) {
        writeProperty(name.get(), value);
    }

    public void writeProperty(String name, double value) {
        name = quoteName ? JsonObject.quote(name) : name;
        stream.append(comma() + name + ":" + Double.toString(value));
        startSeparate();
    }

    public void writeProperty(string name, BigDecimal value) {
        writeProperty(name.get(), value);
    }

    public void writeProperty(String name, BigDecimal value) {
        writeProperty(name, value.toString(), false);
    }

    public void writeProperty(string name, long value) {
        writeProperty(name.get(), value);
    }

    public void writeProperty(String name, long value) {
        writeProperty(name, Long.toString(value), false);
    }

    public void writeProperty(string name, int value) {
        writeProperty(name.get(), value);
    }

    public void writeProperty(String name, int value) {
        writeProperty(name, Integer.toString(value), false);
    }

    public void writeProperty(string name, boolean value) {
        writeProperty(name.get(), value);
    }

    public void writeProperty(String name, boolean value) {
        writeProperty(name, Boolean.toString(value), false);
    }

    public void writeProperty(String name, FileInfo value) {
        name = quoteName ? JsonObject.quote(name) : name;
        stream.append(comma() + name + ":");
        write(value);
    }

    public void writeNull(String name) {
        writeProperty(name, "null", false);
    }

    public void startResponse(String requestId, boolean success) {
        startObject();
        writeProperty(new string(Json.requestId), requestId);
        writeProperty(new string(Json.success), success);
        writeProperty(new string(Json.type), "event");
    }

    public void startResponse(String requestId, boolean success, int status) {
        startResponse(requestId, success);
        writeProperty(Json.status, status);
    }

    public void writeInfo(String message) {
        startObject(Json.info);

        startArray(Json.messages);
        write(message);
        finishArray();

        finishObject();
    }

    public void writeInfo(String[] messages, file log) {
        startObject(Json.info);

        startArray(Json.messages);
        for (String message : messages) {
            write(message);
        }
        finishArray();

        if (log != null) {
            writeProperty(new string(Json.serverId), ApplicationServer.Id);
            writeProperty(new string(Json.log), log.getRelativePath());
        }

        finishObject();
    }

    public void finishResponse() {
        finishObject();
    }

    @Override
    public String toString() {
        return stream.toString();
    }
}
