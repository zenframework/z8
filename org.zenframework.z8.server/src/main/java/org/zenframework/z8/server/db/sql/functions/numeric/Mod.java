package org.zenframework.z8.server.db.sql.functions.numeric;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Mod extends SqlToken {
	private SqlToken value;
	private SqlToken base;

	public Mod(SqlToken value, SqlToken base) {
		this.value = value;
		this.base = base;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		value.collectFields(fields);
		base.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			return "Mod(" + value.format(vendor, options) + ", " + base.format(vendor, options) + ")";
		case SqlServer:
			return value.format(vendor, options) + " % " + base.format(vendor, options);
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
