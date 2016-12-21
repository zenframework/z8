package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Mul;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.functions.conversion.ToDate;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.integer;

public class TruncSecond extends SqlToken {
	private SqlToken time;

	public TruncSecond(SqlToken time) {
		this.time = time;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		time.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(time.type()) {
		case Date:
		case Datetime:
			switch(vendor) {
			case Oracle:
				return new ToDate(time).format(vendor, options);
			case Postgres:
				return "date_trunc('second', " + time.format(vendor, options) + ")";
			case SqlServer:
				return "Convert(datetime, convert(varchar(19)," + time.format(vendor, options) + ", 120), 120)";
			default:
				throw new UnknownDatabaseException();
			}

		case Datespan:
			return new Mul(new TotalMinute(time), Operation.Mul, new SqlConst(new integer(datespan.TicksPerMinute))).format(vendor, options);

		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public FieldType type() {
		return time.type();
	}
}
