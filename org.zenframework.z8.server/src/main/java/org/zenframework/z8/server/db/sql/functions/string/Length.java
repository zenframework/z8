package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.Nvl;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.integer;

public class Length extends SqlToken {
	private SqlToken string;

	public Length(SqlToken string) {
		this.string = string;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		string.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		String length = string.format(vendor, options);

		switch(vendor) {
		case Oracle:
			return new Nvl(new SqlStringToken("length(" + length + ")", FieldType.Integer), new SqlConst(new integer(0))).format(vendor, options);
		case Postgres:
			return new SqlStringToken("length(" + length + ")", FieldType.Integer).format(vendor, options);
		case SqlServer:
			return new Nvl(new SqlStringToken("len(" + length + ")", FieldType.Integer), new SqlConst(new integer(0))).format(vendor, options);
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
