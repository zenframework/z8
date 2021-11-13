package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.fts.Fts;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_string;

public class StringExpression extends Expression {
	public static class CLASS<T extends StringExpression> extends Expression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(StringExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new StringExpression(container);
		}
	}

	public StringExpression(IObject container) {
		super(container);

		setDefault(new string());
		aggregation = Aggregation.Max;
	}

	public Fts.CLASS<? extends Fts> fts = new Fts.CLASS<Fts>(this);

	@Override
	public FieldType type() {
		return FieldType.String;
	}

	public sql_string sql_string() {
		return new sql_string(new SqlField(this));
	}

	@Override
	public string get() {
		return z8_get();
	}

	@Override
	public primary parse(String value) {
		return new string(value);
	}

	public string z8_get() {
		return (string)internalGet();
	}

	public StringExpression.CLASS<? extends StringExpression> operatorAssign(string value) {
		set(value);
		return (StringExpression.CLASS<?>)this.getCLASS();
	}

	public StringExpression.CLASS<? extends StringExpression> operatorAssign(sql_string expression) {
		setExpression(expression);
		return (StringExpression.CLASS<?>)this.getCLASS();
	}

	public sql_bool z8_isEmpty() {
		return sql_string().z8_isEmpty();
	}
}
