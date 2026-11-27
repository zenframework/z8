package org.zenframework.z8.server.db.sql.functions.conversion;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class ToBitset extends SqlToken {
	private static final int PostgresBigintSize = 64;

	private SqlToken token;

	public ToBitset(SqlToken token) {
		this.token = token;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		token.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Postgres:
			int size;
			switch(token.type()) {
			case Integer:
				size = PostgresBigintSize;
				break;
			default:
				throw new UnsupportedOperationException();
			}
			return "CAST(" + token.format(vendor, options) + " AS BIT(" + size + "))";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Bitset;
	}
}
