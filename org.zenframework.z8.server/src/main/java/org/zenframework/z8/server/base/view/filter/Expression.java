package org.zenframework.z8.server.base.view.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.RelDate;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.db.sql.functions.IsNull;
import org.zenframework.z8.server.db.sql.functions.conversion.GuidToString;
import org.zenframework.z8.server.db.sql.functions.string.Like;
import org.zenframework.z8.server.db.sql.functions.string.Lower;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.search.SearchEngine;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_string;
import org.zenframework.z8.server.utils.StringUtils;

public class Expression implements IFilter {
	private Field field;
	private Operation operation;
	private String[] values;

	public Expression(JsonObject expression, Query query) {
		String field = expression.has(Json.property) ? expression.getString(Json.property) : null;
		String values = expression.has(Json.value) ? expression.getString(Json.value) : null;
		String operator = expression.has(Json.operator) ? expression.getString(Json.operator) : null;

		this.operation = operator != null ? Operation.fromString(operator) : Operation.Eq;

		if(Json.__search_text__.equals(field)) {
			if(values != null && !values.isEmpty()) {
				Collection<String> foundIds = SearchEngine.INSTANCE.searchRecords(query, StringUtils.unescapeJava(values));
				this.field = query.getSearchId();
				this.values = foundIds.toArray(new String[0]);
			}
		} else {
			this.field = field != null ? query.findFieldById(field) : null;
				this.values = parseValues(values);
		}
	}

	private String[] parseValues(String jsonData) {
		Collection<String> result = new ArrayList<String>();

		if(jsonData == null || jsonData.isEmpty())
			return new String[0];

		char startChar = jsonData.charAt(0);

		if(startChar == '[') {
			JsonArray values = new JsonArray(jsonData);
			for(Object value : values)
				result.add((String)value);
		} else
			result.add(jsonData);

		return result.toArray(new String[0]);
	}

	private Collection<primary> getValues(FieldType type) {
		Collection<primary> result = new ArrayList<primary>();

		for(String value : values) {
			if(type == FieldType.Date || type == FieldType.Datetime)
				result.add(new date(value).truncDay());
			else if(type == FieldType.Guid)
				result.add(new guid(value));
			else if(type == FieldType.Decimal)
				result.add(new decimal(value));
			else if(type == FieldType.Integer)
				result.add(new integer(value));
			else if(type == FieldType.Boolean)
				result.add(new bool(value));
			else if(type == FieldType.String || type == FieldType.Text)
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

			SqlToken result = new InVector(new SqlField(field), getValues(type));

			if(operation == Operation.NotEq)
				result = new Unary(Operation.Not, result);

			return result;
		}

		String value = values.length != 0 ? values[0] : null;

		switch(type) {
		case Boolean:
			bool boolValue = operation == Operation.IsTrue ? bool.True : (operation == Operation.IsFalse ? bool.False : new bool(value));
			return new Rel(field, Operation.Eq, new sql_bool(boolValue));
		case Decimal:
			return new Rel(field, operation, new decimal(value).sql_decimal());
		case Integer:
			return new Rel(field, operation, new integer(value).sql_int());
		case Date:
			switch(operation) {
			case Eq:
				return new RelDate(field, Operation.Eq, new date(value));
			case NotEq:
				return new RelDate(field, Operation.NotEq, new date(value));
			case LT:
				return new RelDate(field, Operation.LT, new date(value));
			case LE:
				return new RelDate(field, Operation.LE, new date(value));
			case GT:
				return new RelDate(field, Operation.GT, new date(value));
			case GE:
				return new RelDate(field, Operation.GE, new date(value));
			default:
			}
		case Datetime:
			switch(operation) {
			case Eq:
			case NotEq:
			case LT:
			case LE:
			case GT:
			case GE:
				return new Rel(field, operation, new date(value).sql_date());

			case Yesterday:
				return new RelDate(field, Operation.Eq, new date().addDay(-1));
			case Today:
				return new RelDate(field, Operation.Eq, new date());
			case Tomorrow:
				return new RelDate(field, Operation.Eq, new date().addDay(1));

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
				if(value == null || value.isEmpty())
					return null;

				if(operation == Operation.BeginsWith || operation == Operation.NotBeginsWith)
					value += '%';
				else if(operation == Operation.EndsWith || operation == Operation.NotEndsWith)
					value = '%' + value;
				else if(operation == Operation.Contains || operation == Operation.NotContains)
					value = '%' + value + '%';

				SqlToken left = new Lower(field);
				SqlToken right = new sql_string(value.toLowerCase());
				SqlToken result = new Like(left, right, null);

				if(operation == Operation.NotBeginsWith || operation == Operation.NotEndsWith || operation == Operation.NotContains)
					result = new Unary(Operation.Not, result);

				return result;
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
		default:
			throw new UnsupportedOperationException();
		}
	}
}
