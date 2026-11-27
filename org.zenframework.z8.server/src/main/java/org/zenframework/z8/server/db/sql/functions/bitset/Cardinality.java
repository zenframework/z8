package org.zenframework.z8.server.db.sql.functions.bitset;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Cardinality extends SqlToken {

	private final SqlToken bitset;

	public Cardinality(SqlToken bitset) {
		this.bitset = bitset;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		bitset.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch (vendor) {
		case Postgres:
			return new StringBuilder(1024).append("pg_bitcount(").append(bitset.format(vendor, options)).append(')')
					.toString();
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}

}
