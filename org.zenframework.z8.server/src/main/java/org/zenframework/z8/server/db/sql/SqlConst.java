package org.zenframework.z8.server.db.sql;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.primary;

public class SqlConst extends SqlToken {
	private primary value;

	public SqlConst(primary value) {
		this.value = value;
	}

	public primary getValue() {
		return value;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		String result = value.toDbConstant(vendor);

		if(logicalContext && value.type() == FieldType.Boolean) {
			return result + "=1";
		}

		if(value.type() == FieldType.String) {
			switch(vendor) {
			case Oracle:
			case Postgres:
			case SqlServer:
				return new SqlStringToken(result).format(vendor, options, logicalContext);
			}
		}
		return result;
	}

	@Override
	public FieldType type() {
		return value.type();
	}
}
