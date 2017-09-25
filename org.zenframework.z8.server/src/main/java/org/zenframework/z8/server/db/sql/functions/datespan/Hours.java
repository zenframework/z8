package org.zenframework.z8.server.db.sql.functions.datespan;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.numeric.Mod;
import org.zenframework.z8.server.db.sql.functions.numeric.Round;
import org.zenframework.z8.server.types.integer;

public class Hours extends SqlToken {
	private SqlToken span;

	public Hours(SqlToken span) {
		this.span = span;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		span.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		return new Round(new Mod(new TotalHours(span), new SqlConst(new integer(24))), null).format(vendor, options);
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}