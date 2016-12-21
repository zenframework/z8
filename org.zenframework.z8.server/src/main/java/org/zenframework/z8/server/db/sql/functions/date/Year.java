package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToNumber;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Year extends SqlToken {
	private SqlToken date;

	public Year(SqlToken date) {
		this.date = date;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		date.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Postgres:
			return "cast(date_part('year', " + date.format(vendor, options) + ") as bigint)";
		case Oracle:
			return new ToNumber(new SqlStringToken("TO_CHAR(" + date.format(vendor, options) + ", 'YYYY')", FieldType.String)).format(vendor, options);
		case SqlServer:
			return "Year(" + date.format(vendor, options) + ")";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
