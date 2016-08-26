package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.UnsupportedException;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.datespan;

public class AddSecond extends SqlToken {
	private SqlToken date;
	private SqlToken seconds;

	public AddSecond(SqlToken date, SqlToken seconds) {
		this.date = date;
		this.seconds = seconds;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		date.collectFields(fields);
		seconds.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(date.type()) {
		case Date:
		case Datetime:
			switch(vendor) {
			case Oracle:
				return "(" + date.format(vendor, options) + "+(" + seconds.format(vendor, options) + ")/(24*60*60))";
			case Postgres:
				return "(" + date.format(vendor, options) + " + (" + seconds.format(vendor, options) + ") * interval '1 second')";
			case SqlServer:
				return "DATEADD(ss, " + seconds.format(vendor, options) + ", " + date.format(vendor, options) + ")";
			default:
				throw new UnknownDatabaseException();
			}

		case Datespan:
			return date.format(vendor, options) + "+(" + seconds.format(vendor, options) + "*" + datespan.TicksPerSecond + ")";

		default:
			throw new UnsupportedException();
		}
	}

	@Override
	public FieldType type() {
		return date.type();
	}
}
