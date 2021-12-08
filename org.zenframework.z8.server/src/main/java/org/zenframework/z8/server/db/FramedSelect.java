package org.zenframework.z8.server.db;

import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.sql.sql_integer;

public class FramedSelect extends Select {
	int start = 0;
	int limit = -1;

	public FramedSelect(Select select, int start, int limit) {
		super(select);

		this.start = start;
		this.limit = limit;
	}

	@Override
	protected String getSql(FormatOptions options) {
		DatabaseVendor vendor = getDatabase().getVendor();

		if(vendor == DatabaseVendor.Oracle) {
			if(limit <= 0 && start == 0)
				return super.getSql(options);

			Field rowNum = new Expression(new SqlStringToken("ROWNUM", FieldType.Integer), FieldType.Integer);

			if(limit > 0) {
				setSubselect(new Select(this));
				setRootQuery(null);
				setLinks(null);
				setWhere(new Rel(rowNum, Operation.LT, new sql_integer(start + limit)));
				setGroupBy(null);
				setOrderBy(null);
				setHaving(null);

				if(start != 0)
					getFields().add(rowNum);
			}

			if(start != 0) {
				setSubselect(new Select(this));
				setRootQuery(null);
				setLinks(null);
				setWhere(new Rel(rowNum, Operation.GE, new sql_integer(start)));
				setGroupBy(null);
				setOrderBy(null);
				setHaving(null);
			}

			return super.getSql(options);
		} else if(vendor == DatabaseVendor.Postgres) {
			String sql = super.getSql(options);

			sql += "\nlimit " + (limit > 0 ? limit : "all") + " offset " + start;
			return sql;
		} else if(vendor == DatabaseVendor.H2) {
			options.disableReadLock();
			String sql = super.getSql(options);
			if (limit > 0)
				sql += "\nlimit " + (limit > 0 ? limit : "all") + " offset " + start;
			options.enableReadLock();
			sql += formatReadLock(options);
			return sql;
		} else
			throw new UnknownDatabaseException();
	}
}
