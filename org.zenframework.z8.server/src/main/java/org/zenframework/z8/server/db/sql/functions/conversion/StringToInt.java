package org.zenframework.z8.server.db.sql.functions.conversion;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class StringToInt extends SqlToken {
	private SqlToken string;

	public StringToInt(SqlToken string) {
		this.string = string;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		string.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			return "TO_NUMBER(" + string.format(vendor, options) + ")";
		case SqlServer:
			return "CONVERT(int, " + string.format(vendor, options) + ")";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
