package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Nvl extends SqlToken {
	private SqlToken first;
	private SqlToken second;

	public Nvl(SqlToken first, SqlToken second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		first.collectFields(fields);
		second.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			return "nvl(" + first.format(vendor, options) + "," + second.format(vendor, options, logicalContext) + ")";
		case SqlServer:
			return "isNull(" + first.format(vendor, options) + "," + second.format(vendor, options, logicalContext) + ")";
		case Postgres:
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return first.type();
	}
}
