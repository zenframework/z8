package org.zenframework.z8.server.db.sql.functions.vector;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Scalar extends SqlToken {

	private final SqlToken vector1, vector2;
	private final String operator;

	public Scalar(SqlToken vector1, SqlToken vector2, String operator) {
		this.vector1 = vector1;
		this.vector2 = vector2;
		this.operator = operator;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		vector1.collectFields(fields);
		vector2.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch (vendor) {
		case Postgres:
			return new StringBuilder(1024).append('(').append(vector1.format(vendor, options)).append(operator)
					.append(vector2.format(vendor, options)).append(')').toString();
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Decimal;
	}
}
