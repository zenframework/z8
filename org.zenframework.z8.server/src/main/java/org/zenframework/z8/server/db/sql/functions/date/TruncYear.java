package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class TruncYear extends SqlToken {
	private SqlToken date;

	public TruncYear(SqlToken date) {
		this.date = date;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		date.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			throw new UnsupportedOperationException();
//			return "Trunc(" + date.format(vendor, options) + ", 'YYYY')";
		case Postgres:
			return "date_trunc('year', " + date.format(vendor, options) + ")";
		case SqlServer:
			return "Convert(datetime, convert(varchar(5)," + date.format(vendor, options) + ", 120) + '01/01', 120)";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return date.type();
	}
}
