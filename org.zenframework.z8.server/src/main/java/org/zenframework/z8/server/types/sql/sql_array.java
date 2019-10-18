package org.zenframework.z8.server.types.sql;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.array.Contains;
import org.zenframework.z8.server.db.sql.functions.array.Element;

public class sql_array extends SqlToken {
	private SqlToken token;

	public sql_array(SqlToken token) {
		this.token = token;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		token.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean isLogicalContext) {
		return '(' + token.format(vendor, options, isLogicalContext) + ')';
	}

	@Override
	public FieldType type() {
		return token.type();
	}

	public sql_bool z8_contains(sql_primary value) {
		return new sql_bool(new Contains(this, value));
	}

	public sql_bool z8_getBool(sql_integer index) {
		return new sql_bool(new Element(this, index));
	}

	public sql_guid z8_getGuid(sql_integer index) {
		return new sql_guid(new Element(this, index));
	}

	public sql_integer z8_getInt(sql_integer index) {
		return new sql_integer(new Element(this, index));
	}

	public sql_decimal z8_getDecimal(sql_integer index) {
		return new sql_decimal(new Element(this, index));
	}

	public sql_date z8_getDate(sql_integer index) {
		return new sql_date(new Element(this, index));
	}

	public sql_string z8_getString(sql_integer index) {
		return new sql_string(new Element(this, index));
	}

}
