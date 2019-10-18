package org.zenframework.z8.server.base.view.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GeometryExpression;
import org.zenframework.z8.server.base.table.value.GeometryField;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Intersects;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.db.sql.functions.IsNull;
import org.zenframework.z8.server.db.sql.functions.conversion.GuidToString;
import org.zenframework.z8.server.db.sql.functions.string.Like;
import org.zenframework.z8.server.db.sql.functions.string.Lower;
import org.zenframework.z8.server.db.sql.functions.string.RegLike;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonException;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.geometry;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_date;
import org.zenframework.z8.server.types.sql.sql_geometry;
import org.zenframework.z8.server.types.sql.sql_string;

public class Expression implements IFilter {
	private Field field;
	private Operation operation;
	private String[] values;

	public Expression(JsonObject expression, Query query) {
		String field = expression.has(Json.property) ? expression.getString(Json.property) : null;
		String values = expression.has(Json.value) ? expression.getString(Json.value) : null;
		String operator = expression.has(Json.operator) ? expression.getString(Json.operator) : null;

		this.operation = operator != null ? Operation.fromString(operator) : Operation.Eq;

		this.field = field != null ? query.findFieldById(field) : null;
		this.values = parseValues(values);
	}

	private String[] parseValues(String jsonData) {
		Collection<String> result = new ArrayList<String>();

		if(jsonData == null || jsonData.isEmpty())
			return new String[0];

		char startChar = jsonData.charAt(0);

		if(startChar == '[') {
			try {
				JsonArray values = new JsonArray(jsonData);
				for(Object value : values)
					result.add((String)value);
			} catch (JsonException e) {
				result.add(jsonData);
			}
		} else
			result.add(jsonData);

		return result.toArray(new String[0]);
	}

	private Collection<primary> getValues(FieldType type) {
		Collection<primary> result = new ArrayList<primary>();

		for(String value : values) {
			if(type == FieldType.Date || type == FieldType.Datetime)
				result.add(new date(value));
			else if(type == FieldType.Guid)
				result.add(new guid(value));
			else if(type == FieldType.Decimal)
				result.add(new decimal(value));
			else if(type == FieldType.Integer)
				result.add(new integer(value));
			else if(type == FieldType.Boolean)
				result.add(new bool(value));
			else if(type == FieldType.String || type == FieldType.Text || type == FieldType.Attachments || type == FieldType.File)
				result.add(new string(value));
			else
				throw new UnsupportedOperationException();
		}

		return result;
	}

	public SqlToken where() {
		if(field == null)
			return null;

		FieldType type = field.type();

		if(values.length > 1) {
			if(operation != Operation.Eq && operation != Operation.NotEq || type == FieldType.Date || type == FieldType.Datetime)
				throw new UnsupportedOperationException();

			SqlToken result = field.inVector(getValues(type));

			if(operation == Operation.NotEq)
				result = new Unary(Operation.Not, result);

			return result;
		}

		String value = values.length != 0 ? values[0] : null;
		String values[] = {};
		SqlToken token;

		switch(type) {
		case Boolean:
			bool boolValue = operation == Operation.IsTrue ? bool.True : (operation == Operation.IsFalse ? bool.False : new bool(value));
			return new Rel(field, Operation.Eq, new sql_bool(boolValue));
		case Decimal:
			return new Rel(field, operation, new decimal(value).sql_decimal());
		case Integer:
			return new Rel(field, operation, new integer(value).sql_int());
		case Datetime:
		case Date:
			switch(operation) {
			case Eq:
			case NotEq:
			case LT:
			case LE:
			case GT:
			case GE:
				return new Rel(field, operation, new date(value).sql_date());
			case Yesterday:
				return new Rel(field, Operation.Eq, new date().addDay(-1).sql_date());
			case Today:
				return new Rel(field, Operation.Eq, new sql_date());
			case Tomorrow:
				return new Rel(field, Operation.Eq, new date().addDay(1).sql_date());

			case LastWeek:
				date date = new date().truncWeek().addDay(-7);
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addDay(7).sql_date()));
			case ThisWeek:
				date = new date().truncWeek();
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addDay(7).sql_date()));
			case NextWeek:
				date = new date().truncWeek().addDay(7);
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addDay(7).sql_date()));

			case LastMonth:
				date = new date().truncMonth().addMonth(-1);
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addMonth(1).sql_date()));
			case ThisMonth:
				date = new date().truncMonth();
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addMonth(1).sql_date()));
			case NextMonth:
				date = new date().truncMonth().addMonth(1);
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addMonth(1).sql_date()));

			case LastYear:
				date = new date().truncYear().addYear(-1);
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addYear(1).sql_date()));
			case ThisYear:
				date = new date().truncYear();
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addYear(1).sql_date()));
			case NextYear:
				date = new date().truncYear().addYear(1);
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addYear(1).sql_date()));

			case LastDays:
				int days = new integer(value).getInt();
				date = new date().truncDay().addDay(-days);
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addDay(days).sql_date()));
			case NextDays:
				days = new integer(value).getInt();
				date = new date().truncDay();
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addDay(days).sql_date()));

			case LastHours:
				int hours = new integer(value).getInt();
				date = new date().truncHour().addHour(-hours);
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addHour(hours).sql_date()));
			case NextHours:
				hours = new integer(value).getInt();
				date = new date().truncHour();
				return new And(new Rel(field, Operation.GE, date.sql_date()), new Rel(field, Operation.LT, date.addHour(hours).sql_date()));
			default:
				throw new UnsupportedOperationException();
			}
		case Guid:
		case String:
		case Text:
		case Attachments:
		case File:
			SqlToken field = new SqlField(this.field);

			if(type == FieldType.Guid) {
				if(operation == Operation.Eq || operation == Operation.NotEq) {
					SqlToken rel = new Rel(field, operation != null ? operation : Operation.Eq, new guid(value).sql_guid());
					return this.field.isRightJoined() ? new Or(rel, new IsNull(field)) : rel;
				}
				field = new GuidToString(field);
			}

			switch(operation) {
			case BeginsWith:
			case NotBeginsWith:
			case EndsWith:
			case NotEndsWith:
			case Contains:
			case NotContains:
				if(value == null || value.trim().isEmpty())
					return null;

				if(operation == Operation.BeginsWith || operation == Operation.NotBeginsWith)
					values = new String[] { value + '%' };
				else if(operation == Operation.EndsWith || operation == Operation.NotEndsWith)
					values = new String[] { '%' + value };
				else if(operation == Operation.Contains || operation == Operation.NotContains) {
					value = value.trim();
					if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
						values = new String[] { '%' + value.substring(1, value.length() - 1) + '%' };
					} else {
						values = value.split("\\s+");
						for (int i = 0; i < values.length; i++)
							values[i] = "%" + values[i] + "%";
					}
				}

				token = new Like(new Lower(field), new sql_string(values[0].toLowerCase()));
				for (int i = 1; i < values.length; i++) {
					SqlToken t = new Like(new Lower(field), new sql_string(values[i].toLowerCase()));
					token = new And(token, t);
				}

				if(operation == Operation.NotBeginsWith || operation == Operation.NotEndsWith || operation == Operation.NotContains)
					token = new Unary(Operation.Not, token);

				return token;
			case ContainsWord:
			case NotContainsWord:
				if(value == null || value.trim().isEmpty())
					return null;

				value = value.trim();
				if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
					values = new String[] { "\\y" + value.substring(1, value.length() - 1) + "\\y" };
				} else {
					values = value.split("\\s+");
					for (int i = 0; i < values.length; i++)
						values[i] = "\\y" + values[i] + "\\y";
				}

				token = new RegLike(new Lower(field), new sql_string(values[0].toLowerCase()));
				for (int i = 1; i < values.length; i++) {
					SqlToken t = new RegLike(new Lower(field), new sql_string(values[i].toLowerCase()));
					token = new And(token, t);
				}

				if(operation == Operation.NotContainsWord)
					token = new Unary(Operation.Not, token);

				return token;
			case Eq:
			case NotEq:
			case LT:
			case LE:
			case GT:
			case GE:
				if(value == null)
					return null;

				field = operation == Operation.Eq || operation == Operation.NotEq ? new Lower(this.field) : new SqlField(this.field);
				sql_string string = new sql_string(operation == Operation.Eq || operation == Operation.NotEq ? value.toLowerCase() : value);
				return new Rel(field, operation, string);
			case IsEmpty:
				return new Rel(field, Operation.Eq, new sql_string());
			case IsNotEmpty:
				return new Rel(field, Operation.NotEq, new sql_string());
			default:
				throw new UnsupportedOperationException();
			}
		case Geometry:
			return new Intersects(this.field, new sql_geometry(geometry.fromGeoJson(value, srs(this.field))));
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	static private int srs(Field field) {
		if (field instanceof GeometryField)
			return ((GeometryField) field).srs.getInt();
		if (field instanceof GeometryExpression)
			return ((GeometryExpression) field).srs.getInt();
		throw new UnsupportedOperationException();
	}

}
