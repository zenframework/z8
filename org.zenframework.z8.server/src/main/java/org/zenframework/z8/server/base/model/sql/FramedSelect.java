package org.zenframework.z8.server.base.model.sql;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
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
	protected String sql(FormatOptions options) {
		DatabaseVendor vendor = database().vendor();

		if(vendor == DatabaseVendor.Oracle) {
			setSubselect(new Select(this));

			setRootQuery(null);
			setLinks(null);

			setWhere(getFrameWhere(start, limit));
			setGroupBy(null);
			setOrderBy(null);
			setHaving(null);

			return super.sql(options);
		} else if(vendor == DatabaseVendor.Postgres) {
			String sql = super.sql(options);

			sql += "\nlimit " + (limit != -1 ? limit : "all") + " offset " + start;
			return sql;
		} else
			throw new UnknownDatabaseException();
	}

	private SqlToken getFrameWhere(int start, int limit) {
		SqlToken rownum = new SqlStringToken("ROWNUM", FieldType.Integer);
		SqlToken left = new Rel(rownum, Operation.GE, new sql_integer(start));
		SqlToken right = new Rel(rownum, Operation.LT, new sql_integer(start + limit));
		return limit > 0 ? new And(left, right) : left;
	}
}
