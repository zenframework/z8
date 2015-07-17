package org.zenframework.z8.server.base.view.command;

import org.zenframework.z8.server.base.model.command.IParameter;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.INamedObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Parameter extends OBJECT implements IParameter {
    public static class CLASS<T extends Parameter> extends OBJECT.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(Parameter.class);
            setAttribute(Native, Parameter.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Parameter(container);
        }
    }

    public string text = new string();

    public primary value = new string();

    public String queryId = null;
    public String fieldId = null;
    public guid recordId = null;

    public Parameter(IObject container) {
        super(container);
    }

    @Override
    public String id() {
        return text.get();
    }

    public String query() {
        return queryId;
    }

    public String field() {
        return fieldId;
    }

    @Override
    public String displayName() {
        return text.get();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof INamedObject ? id().equals(((INamedObject)object).id()) : false;
    }

    @Override
    public int compareTo(INamedObject object) {
        return id().hashCode() - object.id().hashCode();
    }

    @Override
    public FieldType getType() {
        if(value instanceof bool) {
            return FieldType.Boolean;
        }
        else if(value instanceof date) {
            return FieldType.Date;
        }
        else if(value instanceof datetime) {
            return FieldType.Datetime;
        }
        else if(value instanceof decimal) {
            return FieldType.Decimal;
        }
        else if(value instanceof integer) {
            return FieldType.Integer;
        }
        else if(value instanceof file) {
            return FieldType.File;
        }
        else if(value instanceof guid) {
            return FieldType.Guid;
        }

        return FieldType.String;
    }

    @Override
    public primary get() {
        return value;
    }

    @Override
    public void set(primary value) {
        this.value = value;
    }

    @Override
    public void parse(String value) {
        FieldType type = getType();

        if(type == FieldType.String) {
            set(new string(value));
        }
        else if(type == FieldType.Integer) {
            set(new integer(value));
        }
        else if(type == FieldType.Decimal) {
            set(new decimal(value));
        }
        else if(type == FieldType.Boolean) {
            set(new bool(value));
        }
        else if(type == FieldType.Date) {
            set(value.isEmpty() ? new date() : new date(value));
        }
        else if(type == FieldType.Datetime) {
            set(value.isEmpty() ? new datetime() : new datetime(value));
        }
        else if(type == FieldType.Datespan) {
            set(value.isEmpty() ? new datespan() : new datespan(value));
        }
        else if(type == FieldType.Guid) {
            set(new guid(value));
        }
        else if(type == FieldType.File) {
            FileInfo info = new FileInfo(value);
            set(new file(info.name));
        }
        else {
            assert (false);
        }
    }

    @Override
    public void write(JsonObject writer) {
        writer.put(Json.id, text);
        writer.put(Json.text, text);
        writer.put(Json.serverType, getType().toString());
        writer.put(Json.queryId, queryId);
        writer.put(Json.fieldId, fieldId);
        writer.put(Json.recordId, recordId);
        if(recordId == null) {
            writer.put(Json.value, value);
        }
    }

    public bool z8_bool() {
        return (bool)value;
    }

    public guid z8_guid() {
        return (guid)value;
    }

    public integer z8_int() {
        return (integer)value;
    }

    public date z8_date() {
        return (date)value;
    }

    public datetime z8_datetime() {
        return (datetime)value;
    }

    public decimal z8_decimal() {
        return (decimal)value;
    }

    public file z8_file() {
        return (file)value;
    }

    public string z8_string() {
        return value.z8_toString();
    }

    static public Parameter.CLASS<? extends Parameter> z8_create(string name, primary value) {
        Parameter.CLASS<Parameter> parameter = new Parameter.CLASS<Parameter>();
        parameter.get().text.set(name);
        parameter.get().value = value;
        return parameter;
    }

    static public Parameter.CLASS<? extends Parameter> z8_create(string name, Query.CLASS<? extends Query> queryCls,
            Field.CLASS<? extends Field> fieldCls) {
        return z8_create(name, queryCls, fieldCls, guid.NULL);
    }

    static public Parameter.CLASS<? extends Parameter> z8_create(string name, Query.CLASS<? extends Query> queryCls,
            Field.CLASS<? extends Field> fieldCls, guid recordId) {
        Parameter.CLASS<? extends Parameter> parameter = z8_create(name, new string());
        parameter.get().value = new guid();
        parameter.get().queryId = queryCls.classId();
        parameter.get().fieldId = fieldCls.id().replace(queryCls.id(), "id0");
        parameter.get().recordId = recordId;
        return parameter;
    }

    static public Parameter.CLASS<? extends Parameter> z8_create(string name, Field.CLASS<? extends Field> fieldCls) {
        Field field = fieldCls.get();

        Parameter.CLASS<? extends Parameter> parameter = z8_create(name, primary.create(field.type()));
        parameter.get().fieldId = field.id();
        return parameter;
    }

    static public Parameter.CLASS<? extends Parameter> z8_create(string name, Query.CLASS<? extends Query> queryCls) {
        Parameter.CLASS<? extends Parameter> parameter = z8_create(name, new string());
        parameter.get().queryId = queryCls.classId();
        return parameter;
    }
}
