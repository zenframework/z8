package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Concat extends StringFunction {
	private SqlToken left;
	private SqlToken right;

	public Concat(Field left, Field right) {
		this(new SqlField(left), new SqlField(right));
	}

	public Concat(Field left, SqlToken right) {
		this(new SqlField(left), right);
	}

	public Concat(SqlToken left, Field right) {
		this(left, new SqlField(right));
	}

	public Concat(SqlToken left, SqlToken right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		left.collectFields(fields);
		right.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
		case Postgres:
			return "Concat(" + left.format(vendor, options) + ", " + right.format(vendor, options) + ")";
		case SqlServer:
			return "(" + left.format(vendor, options) + "+" + right.format(vendor, options) + ")";
		default:
			throw new UnknownDatabaseException();
		}
	}
}
