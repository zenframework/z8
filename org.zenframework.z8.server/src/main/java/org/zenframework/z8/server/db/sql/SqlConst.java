package org.zenframework.z8.server.db.sql;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.geometry;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class SqlConst extends SqlToken {
	private primary value;

	public SqlConst(primary value) {
		this.value = value;
	}

	@Override
	public boolean isConst() {
		return true;
	}

	@Override
	public primary primary() {
		return value;
	}

	@Override
	public bool bool() {
		return value == null ? null : (bool)value;
	}

	@Override
	public geometry geometry() {
		return value == null ? null : (geometry)value;
	}

	@Override
	public guid guid() {
		return value == null ? null : (guid)value;
	}

	@Override
	public date date() {
		return value == null ? null : (date)value;
	}

	@Override
	public datespan datespan() {
		return value == null ? null : (datespan)value;
	}

	@Override
	public decimal decimal() {
		return value == null ? null : (decimal)value;
	}

	@Override
	public integer integer() {
		return value == null ? null : (integer)value;
	}

	@Override
	public string string() {
		return null;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		if(value == null)
			return "NULL";

		String result = value.toDbConstant(vendor);

		if (logicalContext && value.type() == FieldType.Boolean)
			return result + "=1";

		if (value.type() == FieldType.String)
			return new SqlStringToken(result, FieldType.String).format(vendor, options, logicalContext);

		return result;
	}

	@Override
	public FieldType type() {
		return value == null ? FieldType.Null : value.type();
	}
}
