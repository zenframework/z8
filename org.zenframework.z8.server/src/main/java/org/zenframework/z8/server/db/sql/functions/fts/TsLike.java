package org.zenframework.z8.server.db.sql.functions.fts;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class TsLike extends SqlToken {
	private SqlToken tsvector;
	private SqlToken tsquery;

	public TsLike(SqlToken tsvector, SqlToken tsquery) {
		this.tsvector = tsvector;
		this.tsquery = tsquery;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		tsvector.collectFields(fields);
		tsquery.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		StringBuilder str = new StringBuilder(1024);

		if (vendor == DatabaseVendor.SqlServer) {
			str.append(tsquery.format(vendor, options, logicalContext)).append(" @@ ");
			str.append(tsvector.format(vendor, options, logicalContext));
		} else
			throw new UnknownDatabaseException();

		return str.toString();
	}

	@Override
	public FieldType type() {
		return FieldType.Boolean;
	}
}
