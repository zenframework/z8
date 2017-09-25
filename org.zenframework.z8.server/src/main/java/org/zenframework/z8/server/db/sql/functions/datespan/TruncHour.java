package org.zenframework.z8.server.db.sql.functions.datespan;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Mul;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.integer;

public class TruncHour extends SqlToken {
	private SqlToken span;

	public TruncHour(SqlToken span) {
		this.span = span;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		span.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		return new Mul(new Days(span), Operation.Mul, new SqlConst(new integer(datespan.TicksPerDay))).format(vendor, options);
	}

	@Override
	public FieldType type() {
		return FieldType.Datespan;
	}
}
