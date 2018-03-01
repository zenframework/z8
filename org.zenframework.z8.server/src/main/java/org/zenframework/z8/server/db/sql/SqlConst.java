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
		return (bool)value;
	}

	@Override
	public geometry geometry() {
		return (geometry)value;
	}

	@Override
	public guid guid() {
		return (guid)value;
	}

	@Override
	public date date() {
		return (date)value;
	}

	@Override
	public datespan datespan() {
		return (datespan)value;
	}

	@Override
	public decimal decimal() {
		return (decimal)value;
	}

	@Override
	public integer integer() {
		return (integer)value;
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
		String result = value.toDbConstant(vendor);

		if(logicalContext && value.type() == FieldType.Boolean)
			return result + "=1";

		if(value.type() == FieldType.String) {
			switch(vendor) {
			case Oracle:
			case Postgres:
			case SqlServer:
				return new SqlStringToken(result, FieldType.String).format(vendor, options, logicalContext);
			}
		}
		return result;
	}

	@Override
	public FieldType type() {
		return value.type();
	}
}
