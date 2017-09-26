package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.integer;

public class IsNull extends SqlToken {
	private SqlToken token;

	public IsNull(Field field) {
		this(new SqlField(field));
	}

	public IsNull(SqlToken token) {
		this.token = token;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		token.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		SqlToken t = new IsNullToken();
		if(!logicalContext)
			t = new If(t, new SqlConst(new integer(1)), new SqlConst(new integer(0)));

		return t.format(vendor, options, true);
	}

	@Override
	public FieldType type() {
		return FieldType.Boolean;
	}

	private class IsNullToken extends SqlToken {
		private IsNullToken() {
		}

		@Override
		public void collectFields(Collection<IField> fields) {
		}

		@Override
		public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
			return token.format(vendor, options) + " is null";
		}

		@Override
		public FieldType type() {
			return FieldType.Boolean;
		}
	}
}
