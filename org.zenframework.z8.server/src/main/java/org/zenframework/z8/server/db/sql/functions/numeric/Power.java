package org.zenframework.z8.server.db.sql.functions.numeric;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;

public class Power extends SqlToken {
	private SqlToken value;
	private SqlToken power;

	public Power(SqlToken value, SqlToken power) {
		this.value = value;
		this.power = power;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		value.collectFields(fields);
		power.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		return "POWER(" + value.format(vendor, options) + ", " + power.format(vendor, options) + ")";
	}

	@Override
	public FieldType type() {
		return value.type();
	}
}
