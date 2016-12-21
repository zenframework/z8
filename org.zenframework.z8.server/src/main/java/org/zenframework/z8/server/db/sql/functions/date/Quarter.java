package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Quarter extends SqlToken {
	private SqlToken date;

	public Quarter(SqlToken date) {
		this.date = date;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		date.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			return "TRUNC((TO_NUMBER(TO_CHAR(" + date.format(vendor, options) + ", 'MM'))-1)/3)+1";
		case SqlServer:
			return "DatePart(q, " + date.format(vendor, options) + ")";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
