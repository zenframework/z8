package org.zenframework.z8.server.types.sql;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.geometry;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;

public class sql_primary extends SqlToken {
	private SqlToken token;

	public sql_primary() {
	}

	public sql_primary(SqlToken token) {
		this.token = token;
	}

	public SqlToken getToken() {
		return token;
	}

	public void setToken(SqlToken token) {
		this.token = token;
	}

	@Override
	public boolean isConst() {
		return token instanceof SqlConst;
	}

	@Override
	public primary primary() {
		return token.primary();
	}

	@Override
	public bool bool() {
		return token.bool();
	}

	@Override
	public geometry geometry() {
		return token.geometry();
	}

	@Override
	public guid guid() {
		return token.guid();
	}

	@Override
	public date date() {
		return token.date();
	}

	@Override
	public datespan datespan() {
		return token.datespan();
	}

	@Override
	public decimal decimal() {
		return token.decimal();
	}

	@Override
	public integer integer() {
		return token.integer();
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		token.collectFields(fields);
	}

	@Override
	public Collection<IField> getUsedFields() {
		return token.getUsedFields();
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		return token.format(vendor, options, logicalContext);
	}

	@Override
	public FieldType type() {
		return token.type();
	}

	public sql_string z8_toString() {
		throw new UnsupportedOperationException();
	}

}
