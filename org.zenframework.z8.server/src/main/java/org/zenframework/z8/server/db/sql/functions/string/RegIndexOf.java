package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.Nvl;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.integer;

public class RegIndexOf extends SqlToken {
	private SqlToken string;
	private SqlToken pattern;

	public RegIndexOf(SqlToken string, SqlToken pattern) {
		this.string = string;
		this.pattern = pattern;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		string.collectFields(fields);
		pattern.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		String result = null;

		if(vendor == DatabaseVendor.Oracle) {
			result = "RegExp_instr(" + pattern.format(vendor, options) + ", " + string.format(vendor, options) + ") - 1";
		} else if(vendor == DatabaseVendor.SqlServer) {
			result = "patIndex(" + pattern.format(vendor, options) + ", " + string.format(vendor, options) + ") - 1";
		} else
			throw new UnknownDatabaseException();

		return (new Nvl(new SqlStringToken(result, FieldType.Integer), new SqlConst(new integer(-1)))).format(vendor, options);
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
