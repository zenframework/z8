package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class ArrayAgg extends Agg {
	public ArrayAgg(SqlToken token, boolean distinct) {
		super(token, distinct);
	}

	public ArrayAgg(SqlToken token, boolean distinct, Collection<Field> orderBy) {
		super(token, distinct, orderBy);
	}

	@Override
	protected SqlToken modifyOnDistinct(SqlToken token) {
		return new Distinct(token);
	}

	@Override
	protected String getSql(DatabaseVendor vendor, String orderFields, String expression) {
		switch(vendor) {
		case Oracle:
		case H2: {
			String orderSql = orderFields.isEmpty() ? " WITHIN GROUP (ORDER BY (SELECT NULL))"
													: " WITHIN GROUP (ORDER BY " + orderFields + ")";
			return "LISTAGG(" + expression + ", ',')" + orderSql;
		}
		case Postgres: {
			String orderSql = orderFields.isEmpty() ? "" : " ORDER BY " + orderFields;
			return "array_agg(" + expression + orderSql + ")";
		}
		case SqlServer: {
			String orderSql = orderFields.isEmpty() ? "" : " WITHIN GROUP (ORDER BY " + orderFields + ")";
			return "STRING_AGG(" + expression + ", ',')" + orderSql;
		}
		default:
			throw new UnknownDatabaseException();
		}
	}
}
