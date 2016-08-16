package org.zenframework.z8.server.db.sql.expressions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.exceptions.UnsupportedException;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.sql.sql_integer;

public class Unary extends SqlToken {
	private Operation operation;
	private SqlToken token;

	public Unary(Operation operation, Field field) {
		this(operation, new SqlField(field));
	}

	public Unary(Operation operation, SqlToken token) {
		this.operation = operation;
		this.token = token;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		if(token != null)
			token.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		switch(operation) {
		case Not: {
			SqlToken t = new UNOTToken();
			if(!logicalContext)
				t = new If(t, new sql_integer(1), new sql_integer(0));
			return t.format(vendor, options, logicalContext);
		}

		case Minus:
			return "(-" + token.format(vendor, options) + ")";

		default:
			throw new UnsupportedException();
		}
	}

	@Override
	public FieldType type() {
		switch(operation) {
		case Not: {
			return FieldType.Boolean;
		}

		case Minus: {
			return token.type();
		}

		default:
			throw new UnsupportedException();
		}
	}

	private class UNOTToken extends SqlToken {
		@Override
		public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
			return "not(" + token.format(vendor, options, true) + ")";
		}

		@Override
		public void collectFields(Collection<IValue> fields) {
		}

		@Override
		public FieldType type() {
			return FieldType.Boolean;
		}
	}
}
