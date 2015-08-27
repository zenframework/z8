package org.zenframework.z8.server.base.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class QueryUtils {
    static public Collection<Query> getOwners(Collection<ILink> links) {
        Collection<Query> queries = new ArrayList<Query>();

        for(ILink link : links) {
            Field field = (Field)link;
            queries.add(field.getOwner());
        }

        return queries;
    }

    static public Collection<Filter> getFilters(Collection<Query> queries) {
        Collection<Filter> filters = new ArrayList<Filter>();

        for(Query query : queries) {
            filters.addAll(query.getFilters());
        }

        return filters;
    }

    static public void setFieldValue(Field field, String value) {
        FieldType type = field.type();

        if(type == FieldType.String || type == FieldType.Text) {
            field.set(new string(value));
        }
        else if(type == FieldType.Integer) {
            field.set(value == null || value.isEmpty() ? new integer() : new integer(value));
        }
        else if(type == FieldType.Decimal) {
            field.set(value == null || value.isEmpty() ? new decimal() : new decimal(value));
        }
        else if(type == FieldType.Boolean) {
            field.set(value == null || value.isEmpty() ? new bool() : new bool(value));
        }
        else if(type == FieldType.Date) {
            field.set(value == null || value.isEmpty() ? new date(date.MIN) : new date(value));
        }
        else if(value == null || type == FieldType.Datetime) {
            field.set(value == null || value.isEmpty() ? new datetime(datetime.MIN) : new datetime(value));
        }
        else if(type == FieldType.Datespan) {
            field.set(value == null || value.isEmpty() ? new datespan() : new datespan(value));
        }
        else if(type == FieldType.Guid) {
            field.set(value == null || value.isEmpty() ? new guid() : new guid(value));
        }
        else {
            assert (false);
        }
    }

    static public void updateFields(Collection<Field> fields, Collection<String> values) {
        assert (values.size() == fields.size());

        Iterator<Field> fieldsIterator = fields.iterator();
        Iterator<String> valuesIterator = values.iterator();

        while(fieldsIterator.hasNext()) {
            Field field = fieldsIterator.next();
            String value = valuesIterator.next();

            setFieldValue(field, value);
        }
    }

    static public guid parseRecord(JsonObject record, Query query, Collection<Field> fields) {
        assert (fields.size() == 0);

        guid recordId = null;

        List<String> values = new ArrayList<String>();

        Query rootQuery = query.getRootQuery();

        for(String fieldId : JsonObject.getNames(record)) {
            Field field = rootQuery.findFieldById(fieldId);

            String value = record.getString(fieldId);

            if(field != null) {
                fields.add(field);
                values.add(value);

                if(field.isPrimaryKey()) {
                    recordId = new guid(value);
                }
            }
        }

        QueryUtils.updateFields(fields, values);

        return recordId;
    }
}
