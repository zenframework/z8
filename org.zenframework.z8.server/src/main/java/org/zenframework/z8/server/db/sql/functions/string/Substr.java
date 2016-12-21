package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Add;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.integer;

public class Substr extends StringFunction {
	private SqlToken string;
	private SqlToken position;
	private SqlToken length;

	public Substr(SqlToken string, SqlToken position) {
		this(string, position, null);
	}

	public Substr(SqlToken string, SqlToken position, SqlToken length) {
		this.string = string;
		this.position = new Add(position, Operation.Add, new SqlConst(new integer(1)));
		this.length = length != null ? length : new Length(string);
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		string.collectFields(fields);
		position.collectFields(fields);
		length.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		String result = "";

		if(vendor == DatabaseVendor.Oracle || vendor == DatabaseVendor.Postgres)
			result = "substr(";
		else if(vendor == DatabaseVendor.SqlServer)
			result = "subString(";
		else
			throw new UnknownDatabaseException();

		result += string.format(vendor, options) + "," + position.format(vendor, options) + "," + length.format(vendor, options) + ")";

		return result;
	}
}
