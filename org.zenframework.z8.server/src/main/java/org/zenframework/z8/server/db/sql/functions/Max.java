package org.zenframework.z8.server.db.sql.functions;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Max extends Window {
	private FieldType originalType;

	public Max(SqlToken token) {
		super(token.type() == FieldType.Guid || token.type() == FieldType.Text ? new ToString(token) : token, "max");
		originalType = token.type();
	}

	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		String result = format(vendor, options, logicalContext);

		switch(vendor) {
		case Oracle:
		case SqlServer:
			return result;
		case Postgres:
			return result + (originalType == FieldType.Guid ? "::uuid" : "");
		default:
			throw new UnknownDatabaseException();
		}
	}
}
