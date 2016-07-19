package org.zenframework.z8.server.base.view.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.db.sql.functions.datetime.TruncDay;
import org.zenframework.z8.server.db.sql.functions.string.Like;
import org.zenframework.z8.server.db.sql.functions.string.Lower;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.search.SearchEngine;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;
import org.zenframework.z8.server.utils.StringUtils;

public class Filter {
    private Field field;
    private Operation operation;
    private String[] values;

    public Filter(Field field, Operation operation, Collection<String> values) {
        this.field = field;
        this.values = values.toArray(new String[0]);
        this.operation = operation;
    }

    public SqlToken where() {
        return where(field);
    }

    private List<guid> getGuidValues() {
        List<guid> result = new ArrayList<guid>();

        for (String value : values)
            result.add(new guid(value));

        return result;
    }

    private List<string> getStringValues() {
        List<string> result = new ArrayList<string>();

        for (String value : values)
            result.add(new string(value));

        return result;
    }

    private SqlToken where(Field field) {
        String value = values.length != 0 ? values[0] : null;
        FieldType type = field.type();

        if (type == FieldType.Date || type == FieldType.Datetime) { // Period or
                                                                    // date +
                                                                    // operation
            SqlToken sqlField = type == FieldType.Datetime ? new TruncDay(field) : new SqlField(field);

            if (values.length == 1)
                return new Rel(sqlField, operation, new date(value).sql_date());

            date start = new date(values[0]);
            date finish = new date(values[1]);
            SqlToken left = new Rel(sqlField, Operation.GE, start.sql_date());
            SqlToken right = new Rel(sqlField, Operation.LE, finish.sql_date());

            return new And(left, right);
        } else if (type == FieldType.Decimal) {
            return new Rel(field, operation, new decimal(value).sql_decimal());
        } else if (type == FieldType.Integer) {
            return new Rel(field, operation, new integer(value).sql_int());
        } else if (type == FieldType.Boolean) {
            return new Rel(field, Operation.Eq, new bool(value).sql_bool());
        } else if (type == FieldType.Guid) {
            if (values.length != 1) {
                List<guid> guids = getGuidValues();

                SqlToken result = new InVector(field, guids);

                if (operation == Operation.Not || operation == Operation.NotEq)
                    result = new Unary(Operation.Not, result);

                return result;
            } else {
                return new Rel(field, operation != null ? operation : Operation.Eq, new guid(value).sql_guid());
            }
        } else if (type == FieldType.String || type == FieldType.Text) {
            if (values.length != 1) {
                List<string> strings = getStringValues();

                SqlToken result = new InVector(field, strings);

                if (operation == Operation.Not || operation == Operation.NotEq)
                    result = new Unary(Operation.Not, result);

                return result;
            } else if (operation == null || operation == Operation.BeginsWith || operation == Operation.EndsWith
                    || operation == Operation.Contains) {
                
            	if (value.isEmpty())
                    return null;

                if (operation == null) {
                    boolean startStar = value.startsWith("*");

                    if (startStar) {
                        value = value.length() > 1 ? value.substring(1) : "";
                    }

                    boolean endStar = value.length() > 1 ? value.endsWith("*") : false;

                    if (endStar) {
                        value = value.substring(0, value.length() - 1);
                    }

                    if (!startStar && !endStar) {
                        if (!value.isEmpty()) {
                            value = "%" + value + "%";
                        }
                    } else {
                        value = (startStar ? "%" : "") + value + (endStar ? "%" : "");
                    }
                } else if (operation == Operation.BeginsWith) {
                    value += '%';
                } else if (operation == Operation.EndsWith) {
                    value = '%' + value;
                } else if (operation == Operation.Contains) {
                    value = '%' + value + '%';
                }

                SqlToken left = new Lower(field);
                SqlToken right = new sql_string(value.toLowerCase());
                return new Like(left, right, null);
            } else if (operation == Operation.Eq || operation == Operation.NotEq || operation == Operation.LT
                    || operation == Operation.LE || operation == Operation.GT || operation == Operation.GE) {
                return new Rel(field, operation, new string(value).sql_string());
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return toString(field);
    }

    static protected Collection<String> parseValues(String jsonData) {
        Collection<String> result = new ArrayList<String>();

        if (jsonData.isEmpty()) {
            result.add("");
            return result;
        }

        char startChar = jsonData.charAt(0);

        if (startChar == '[') { // array or guids
            JsonArray values = new JsonArray(jsonData);

            for (int index = 0; index < values.length(); index++) {
                String value = values.getString(index);
                result.add(value);
            }
        } else if (startChar == '{') { // Period
            JsonObject values = new JsonObject(jsonData);
            String start = values.getString(Json.start);
            String finish = values.getString(Json.finish);

            result.add(start);
            result.add(finish);
        } else
            result.add(jsonData);

        return result;
    }

    static protected Filter getFieldFilter(Query query, String name, String values, String comparison) {
        Operation operation = comparison != null ? Operation.fromString(comparison) : null;

        if (Json.__search_text__.equals(name)) {
            if (values.isEmpty())
                return null;

            Collection<String> foundIds = SearchEngine.INSTANCE.searchRecords(query, StringUtils.unescapeJava(values));

            return new Filter(query.getSearchId(), operation, foundIds);
        } else {
            Field field = query.findFieldById(name);
            return field != null ? new Filter(field, operation, parseValues(values)) : null;
        }
    }

    static public Collection<Filter> parse(Collection<String> json, Query query) {
        List<Filter> result = new ArrayList<Filter>();

        if (json == null)
            return result;

        JsonArray filters = new JsonArray(json);

        return parse(filters, query);
    }

    static public Collection<Filter> parse(String json, Query query) {
        List<Filter> result = new ArrayList<Filter>();

        if (json == null)
            return result;

        if (!json.startsWith("["))
        	json = "[" + json + "]";

        JsonArray filters = new JsonArray(json);

        return parse(filters, query);
    }

    static public Collection<Filter> parse(JsonArray json, Query query) {
        List<Filter> result = new ArrayList<Filter>();

        for (int index = 0; index < json.length(); index++) {
            Object obj = json.get(index);
            JsonObject filter = obj instanceof JsonObject ? (JsonObject) obj : new JsonObject(obj.toString());

            if (filter.has(Json.value)) {
                String fields = filter.getString(filter.has(Json.field) ? Json.field : Json.property);
                String values = filter.getString(Json.value);
                String comparison = filter.has(Json.comparison) ? filter.getString(Json.comparison) : filter
                        .has(Json.operator) ? filter.getString(Json.operator) : null;

                Filter flt = getFieldFilter(query, fields, values, comparison);

                if (flt != null)
                    result.add(flt);
            }
        }

        return result;
    }

    private String toString(Field field) {
        String value = values[0];
        FieldType type = field.type();

        if (type == FieldType.Date || type == FieldType.Datetime || type == FieldType.Guid) {
            return field.displayName() + " " + operation.toReadableString() + " '" + value.toString() + "'";
        } else if (type == FieldType.Decimal) {
            return field.displayName() + " " + operation.toReadableString() + " " + value.toString();
        } else if (type == FieldType.Integer) {
            return field.displayName() + " " + operation.toReadableString() + " " + value.toString();
        } else if (type == FieldType.Boolean) {
            return field.displayName() + " " + operation.toReadableString() + " " + new bool(value).toString();
        } else if (type == FieldType.String || type == FieldType.Text) {
            if (values.length != 1) {
                StringBuilder result = new StringBuilder();
                result.append(field.displayName()).append(' ');
                if (operation == Operation.Not || operation == Operation.NotEq)
                	result.append(Resources.get("Operation.not")).append(' ');
                result.append(Resources.get("Operation.inVector")).append(' ').append(Arrays.toString(values));
                return result.toString();
            } if (operation == null || operation == Operation.BeginsWith || operation == Operation.EndsWith
                    || operation == Operation.Contains) {
                if (value.isEmpty()) {
                    return null;
                }
                return field.displayName() + " " + Resources.get("Operation.contains") + " '" + value.toString() + "'";
            } else if (operation == Operation.Eq || operation == Operation.NotEq || operation == Operation.LT
                    || operation == Operation.LE || operation == Operation.GT || operation == Operation.GE) {
                return field.displayName() + " " + operation.toReadableString() + " '" + value.toString() + "'";
            }
        }

        return null;
    }

}
