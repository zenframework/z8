package org.zenframework.z8.server.db.sql.functions.datetime;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class SetDate extends SqlToken {
	private SqlToken year;
	private SqlToken month;
	private SqlToken day;

	private SqlToken hour;
	private SqlToken minute;
	private SqlToken second;

	public SetDate(SqlToken year, SqlToken month, SqlToken day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}

	public SetDate(SqlToken year, SqlToken month, SqlToken day, SqlToken hour, SqlToken minute, SqlToken second) {
		this(year, month, day);
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}

	@Override
	public FieldType type() {
		return hour != null ? FieldType.Datetime : FieldType.Date;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		year.collectFields(fields);
		month.collectFields(fields);
		day.collectFields(fields);

		if(hour != null) {
			hour.collectFields(fields);
			minute.collectFields(fields);
			second.collectFields(fields);
		}
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case SqlServer: {
			String convert = "convert(varchar, " + year.format(vendor, options) + ") + '-' + " + "convert(varchar, " + month.format(vendor, options) + ") + '-' + " + "convert(varchar, " + day.format(vendor, options) + ")";

			if(hour != null)
				convert += 'T' + "convert(varchar, " + hour.format(vendor, options) + ") + ':' + " + "convert(varchar, " + minute.format(vendor, options) + ") + ':' + " + "convert(varchar, " + second.format(vendor, options) + ")";

			return "cast(" + convert + " as " + (hour == null ? "Date" : "Datetime") + ")";
		}
		default:
			throw new UnknownDatabaseException();
		}
	}
}
