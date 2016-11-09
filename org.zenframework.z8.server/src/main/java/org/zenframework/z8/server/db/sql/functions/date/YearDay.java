package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToNumber;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class YearDay extends SqlToken {
	private SqlToken date;

	public YearDay(SqlToken date) {
		this.date = date;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		date.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(date.type()) {
		case Date:
		case Datetime:
			switch(vendor) {
			case Oracle:
				return new ToNumber(new SqlStringToken("TO_CHAR(" + date.format(vendor, options) + ", 'DDD')", FieldType.String)).format(vendor, options);
			case SqlServer:
				return "DATEPART(dayofyear, " + date.format(vendor, options) + ")";
			default:
				throw new UnknownDatabaseException();
			}

		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
