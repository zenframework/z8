package org.zenframework.z8.server.base.view.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.True;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.db.sql.functions.datetime.TruncDay;
import org.zenframework.z8.server.db.sql.functions.string.Like;
import org.zenframework.z8.server.db.sql.functions.string.Lower;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;

public class Filter {
    private Collection<Field> fields;
    private Operation operation;
    private String[] values;

    public Filter(Collection<Field> fields, Operation operation, Collection<String> values) {
        this.fields = fields;
        this.values = values.toArray(new String[0]);
        this.operation = operation;
    }

    public SqlToken where() {
        SqlToken result = null;

        for(Field field : fields) {
            SqlToken where = where(field);

            if(where != null) {
                result = result == null ? where : new Or(result, where);
            }
        }

        return fields.size() > 1 ? new Group(result) : result;
    }

    List<guid> getGuidValues() {
        List<guid> result = new ArrayList<guid>();
        
        for(String value : values)
            result.add(new guid(value));
           
        return result;
    }
    
    private SqlToken where(Field field) {
        String value = values.length != 0 ? values[0] : null;
        FieldType type = field.type();
        
        if(type == FieldType.Date || type == FieldType.Datetime) { // Period or date + operation
            SqlToken sqlField =  type == FieldType.Datetime ? new TruncDay(field) : new SqlField(field);

            if(values.length == 1)
                return new Rel(sqlField, operation, new date(value).sql_date());
                
            date start = new date(values[0]);
            date finish = new date(values[1]);
            SqlToken left = new Rel(sqlField, Operation.GE, start.sql_date());
            SqlToken right = new Rel(sqlField, Operation.LE, finish.sql_date());
            
            return new And(left, right);
        }
        else if(type == FieldType.Decimal) {
            return new Rel(field, operation, new decimal(value).sql_decimal());
        }
        else if(type == FieldType.Integer) {
            return new Rel(field, operation, new integer(value).sql_int());
        }
        else if(type == FieldType.Boolean) {
            return new Rel(field, Operation.Eq, new bool(value).sql_bool());
        }
        else if(type == FieldType.Guid) {
            if(values.length != 1) {
                List<guid> guids = getGuidValues();
                
                SqlToken result = new InVector(field, guids);
                
                if(operation == Operation.Not || operation == Operation.NotEq)
                    result = new Unary(Operation.Not, result);
                
                return result;
            } else {
                return new Rel(field, operation != null ? operation : Operation.Eq, new guid(value).sql_guid());
            }
        }
        else if(type == FieldType.String || type == FieldType.Text) {
            if(operation == null || operation == Operation.BeginsWith || operation == Operation.EndsWith
                    || operation == Operation.Contains) {
                if(value.isEmpty()) {
                    return new True();
                }

                if(operation == null) {
                    boolean startStar = value.startsWith("*");

                    if(startStar) {
                        value = value.length() > 1 ? value.substring(1) : "";
                    }

                    boolean endStar = value.length() > 1 ? value.endsWith("*") : false;

                    if(endStar) {
                        value = value.substring(0, value.length() - 1);
                    }

                    if(!startStar && !endStar) {
                        if(!value.isEmpty()) {
                            value = "%" + value + "%";
                        }
                    }
                    else {
                        value = (startStar ? "%" : "") + value + (endStar ? "%" : "");
                    }
                }
                else if(operation == Operation.BeginsWith) {
                    value += '%';
                }
                else if(operation == Operation.EndsWith) {
                    value = '%' + value;
                }
                else if(operation == Operation.Contains) {
                    value = '%' + value + '%';
                }

                SqlToken left = new Lower(field);
                SqlToken right = new sql_string(value.toLowerCase());
                return new Like(left, right, null);
            }
            else if(operation == Operation.Eq || operation == Operation.NotEq || operation == Operation.LT
                    || operation == Operation.LE || operation == Operation.GT || operation == Operation.GE) {
                return new Rel(field, operation, new string(value).sql_string());
            }
        }

        return null;
    }

    @Override
    public String toString() {
        String result = "";

        for(Field field : fields) {
            String where = toString(field);

            if(where != null) {
                result += result.isEmpty() ? where : " or " + where;
            }
        }

        return result;
    }

    private String toString(Field field) {
        String value = values[0];
        FieldType type = field.type();
        
        if(type == FieldType.Date || type == FieldType.Datetime || type == FieldType.Guid) {
            return field.displayName() + " " + operation.toReadableString() + " '" + value.toString() + "'";
        }
        else if(type == FieldType.Decimal) {
            return field.displayName() + " " + operation.toReadableString() + " " + value.toString();
        }
        else if(type == FieldType.Integer) {
            return field.displayName() + " " + operation.toReadableString() + " " + value.toString();
        }
        else if(type == FieldType.Boolean) {
            return field.displayName() + " " + operation.toReadableString() + " " + new bool(value).toString();
        }
        else if(type == FieldType.String || type == FieldType.Text) {
            if(operation == null || operation == Operation.BeginsWith || operation == Operation.EndsWith
                    || operation == Operation.Contains) {
                if(value.isEmpty()) {
                    return null;
                }

                return field.displayName() + " " + Resources.get("Operation.contains") + " " + value.toString() + "'";
            }
            else if(operation == Operation.Eq || operation == Operation.NotEq || operation == Operation.LT
                    || operation == Operation.LE || operation == Operation.GT || operation == Operation.GE) {
                return field.displayName() + " " + operation.toReadableString() + " '" + value.toString() + "'";
            }
        }

        return null;
    }

}
