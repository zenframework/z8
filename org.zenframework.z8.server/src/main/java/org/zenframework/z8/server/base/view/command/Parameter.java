package org.zenframework.z8.server.base.view.command;

import org.zenframework.z8.server.base.model.command.IParameter;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.INamedObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
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

    private FieldType type = FieldType.None;
    private Object value = new string();

    private String queryId = null;
    private String fieldId = null;
    
    public Parameter(IObject container) {
        super(container);
    }

    @Override
    public String id() {
        return text.get();
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
        if(type != FieldType.None)
            return type;
        
        if(value instanceof bool)
            return type = FieldType.Boolean;
        else if(value instanceof date)
            return type = FieldType.Date;
        else if(value instanceof datetime) 
            return type = FieldType.Datetime;
        else if(value instanceof decimal)
            return type = FieldType.Decimal;
        else if(value instanceof integer)
            return type = FieldType.Integer;
        else if(value instanceof guid)
            return type = FieldType.Guid;
        else
            return type = FieldType.String;
    }

    @Override
    public Object get() {
        return value;
    }

    @Override
    public void set(Object value) {
        this.value = value;
    }

    @Override
    public void parse(String json) {
        FieldType type = getType();

        if(json.startsWith("[")) {
            JsonArray array = new JsonArray(json);
            
            RCollection<primary> value = new RCollection<primary>();
            
            for(int index = 0 ; index < array.length(); index++) {
                String item = array.getString(index);
                value.add(parse(item, type));
            }
            
            set(value);
        } else
            set(parse(json, type));
    }

    public primary parse(String value, FieldType type) {
        if(type == FieldType.String)
            return new string(value);
        else if(type == FieldType.Integer)
            return new integer(value);
        else if(type == FieldType.Decimal)
            return new decimal(value);
        else if(type == FieldType.Boolean)
            return new bool(value);
        else if(type == FieldType.Date)
            return value.isEmpty() ? new date() : new date(value);
        else if(type == FieldType.Datetime)
            return value.isEmpty() ? new datetime() : new datetime(value);
        else if(type == FieldType.Datespan)
            return value.isEmpty() ? new datespan() : new datespan(value);
        else if(type == FieldType.Guid)
            return new guid(value);
        
        throw new RuntimeException("Unsupported parameter type: '" + type + "'");
    }

    @Override
    public void write(JsonObject writer) {
        writer.put(Json.id, text);
        writer.put(Json.text, text);
        writer.put(Json.serverType, getType().toString());
        writer.put(Json.value, value);

        writer.put(Json.queryId, queryId);
        writer.put(Json.fieldId, fieldId);
    }

    @SuppressWarnings("rawtypes")
    public RCollection toArray() {
        return (RCollection)value;
    }

    public bool z8_bool() {
        return (bool)value;
    }

    @SuppressWarnings("rawtypes")
    public RCollection z8_boolArray() {
        return toArray();
    }

    public guid z8_guid() {
        return (guid)value;
    }

    @SuppressWarnings("rawtypes")
    public RCollection z8_guidArray() {
        return toArray();
    }
    
    public integer z8_int() {
        return (integer)value;
    }

    @SuppressWarnings("rawtypes")
    public RCollection z8_intArray() {
        return toArray();
    }

    public date z8_date() {
        return (date)value;
    }

    @SuppressWarnings("rawtypes")
    public RCollection z8_dateArray() {
        return toArray();
    }
    
    public datetime z8_datetime() {
        return (datetime)value;
    }

    @SuppressWarnings("rawtypes")
    public RCollection z8_datetimeArray() {
        return toArray();
    }
    
    public decimal z8_decimal() {
        return (decimal)value;
    }

    @SuppressWarnings("rawtypes")
    public RCollection z8_decimalArray() {
        return toArray();
    }
    public file z8_file() {
        return (file)value;
    }

    public string z8_string() {
        return (string)value;
    }

    @SuppressWarnings("rawtypes")
    public RCollection z8_stringArray() {
        return toArray();
    }
    
    static public Parameter.CLASS<? extends Parameter> z8_create(string name, primary value) {
        Parameter.CLASS<Parameter> parameter = new Parameter.CLASS<Parameter>();
        parameter.get().text.set(name);
        parameter.get().value = value;
        return parameter;
    }

    static public Parameter.CLASS<? extends Parameter> z8_create(string name, FieldType type) {
        Parameter.CLASS<Parameter> parameter = new Parameter.CLASS<Parameter>();
        parameter.get().text.set(name);
        parameter.get().type = type;
        return parameter;
    }

    @SuppressWarnings("unchecked")
    static public Parameter.CLASS<? extends Parameter> z8_create(string name, Query.CLASS<? extends Query> queryCls)
    {
        Parameter.CLASS<Parameter> parameter = (Parameter.CLASS<Parameter>)z8_create(name, new guid());
        parameter.get().queryId = queryCls.classId(); 
        return parameter;
    }
    
    @SuppressWarnings("unchecked")
    static public Parameter.CLASS<? extends Parameter> z8_create(string name, Query.CLASS<? extends Query> queryCls, Field.CLASS<? extends Field> fieldCls)
    {
        Parameter.CLASS<Parameter> parameter = (Parameter.CLASS<Parameter>)z8_create(name, new guid());
        parameter.get().queryId = queryCls.classId(); 
        parameter.get().fieldId = fieldCls.id().replace(queryCls.id(), ""); 
        return parameter;
    }
    
}
