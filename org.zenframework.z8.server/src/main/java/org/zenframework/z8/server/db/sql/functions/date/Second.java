package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.numeric.Mod;
import org.zenframework.z8.server.db.sql.functions.numeric.Round;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.integer;

public class Second extends SqlToken {
	private SqlToken time;

	public Second(SqlToken time) {
		this.time = time;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		time.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(time.type()) {
		case Date:
		case Datetime:
			switch(vendor) {
			case Oracle:
				return "TO_NUMBER(TO_CHAR(" + time.format(vendor, options) + ", 'SS'))";
			case SqlServer:
				return "DatePart(second, " + time.format(vendor, options) + ")";
			default:
				throw new UnknownDatabaseException();
			}

		case Datespan:
			return new Round(new Mod(new TotalSecond(time), new SqlConst(new integer(60))), null).format(vendor, options);

		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
