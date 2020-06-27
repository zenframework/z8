package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;

public class Intersects extends Rel {
	
	public Intersects(Field left, Field right) {
		this(new SqlField(left), new SqlField(right));
	}

	public Intersects(Field left, SqlToken right) {
		this(new SqlField(left), right);
	}

	public Intersects(SqlToken left, Field right) {
		this(left, new SqlField(right));
	}

	public Intersects(SqlToken left, SqlToken right) {
		super(left, Operation.None, right);
	}

	@Override
	protected String doFormat(DatabaseVendor vendor, FormatOptions options) {
		return "st_intersects(" +
			(left.isConst() ? left.geometry().toDbConstant(vendor) : left.format(vendor, options)) + ", " +
			(right.isConst() ? right.geometry().toDbConstant(vendor) : right.format(vendor, options)) + ")";
	}

	@Override
	public FieldType type() {
		return FieldType.Boolean;
	}
}
