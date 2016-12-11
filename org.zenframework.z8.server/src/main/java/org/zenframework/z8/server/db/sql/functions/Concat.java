package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Concat extends SqlToken {
	private SqlToken param1;

	public Concat(SqlToken p1) {
		param1 = p1;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		param1.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		StringBuilder str = new StringBuilder();
		switch(vendor) {
		case Postgres:
			str.append("string_agg");
			break;
		default:
			throw new UnknownDatabaseException();
		}
		return str.append('(').append(param1.format(vendor, options)).append(",',')").toString();
	}

	@Override
	public FieldType type() {
		return FieldType.String;
	}
}
