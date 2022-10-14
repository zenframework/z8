package org.zenframework.z8.server.base.query;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Period extends OBJECT {
	public static class CLASS<T extends Period> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Period.class);
		}
		
		@Override
		public Object newObject(IObject container) {
			return new Period(container);
		}
	}

	public date start = date.Min;
	public date finish = date.Max;
	public Field.CLASS<? extends Field> field;

	public Period(IObject container) {
		super(container);
	}

	@SuppressWarnings("unchecked")
	public Period(Field field, date start, date finish) {
		super(null);

		this.field = (Field.CLASS<Field>)field.getCLASS();
		this.start = start != null ? start : date.Min;
		this.finish = finish != null ? finish : date.Max;
	}

	public Period(Field field, String json) {
		super(null);
		init(field, json);
	}
	
	@SuppressWarnings("unchecked")
	public void init(Field field, String json) {
		if(field == null)
			return;

		this.field = (Field.CLASS<Field>)field.getCLASS();

		JsonObject period = new JsonObject(json);
		boolean active = period.has(Json.active) ? period.getBoolean(Json.active) : false;

		if(active) {
			this.start = period.has(Json.start) ? new date(period.getString(Json.start)) : date.Min;
			this.finish = period.has(Json.finish) ? new date(period.getString(Json.finish)) : date.Max;
		}
	}
	
	public static Period.CLASS<? extends Period> z8_getPeriod(Field.CLASS<? extends Field> field, string json) {
		Period.CLASS<Period> period = new Period.CLASS<Period>(null);
		period.get().init(field.get(), json.get());
		return period;
	}
	
	public sql_bool where() {
		SqlToken where = null;

		if(field == null)
			return null;

		if(start != null && !start.equals(date.Min))
			where = new Rel(start.sql_date(), Operation.LE, field.get());

		if(finish != null && !finish.equals(date.Max)) {
			SqlToken finishRel = new Rel(field.get(), Operation.LE, finish.sql_date());
			where = where != null ? new And(where, finishRel) : finishRel;
		}

		return where != null ? new sql_bool(where) : null;
	}

	public sql_bool z8_where() {
		sql_bool where = where();
		return where != null ? where : sql_bool.True;
	}
}
