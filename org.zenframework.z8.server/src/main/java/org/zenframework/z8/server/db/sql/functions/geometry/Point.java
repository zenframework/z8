package org.zenframework.z8.server.db.sql.functions.geometry;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Point extends SqlToken {

	private final SqlToken x, y, srs;

	public Point(SqlToken x, SqlToken y, SqlToken srs) {
		this.x = x;
		this.y = y;
		this.srs = srs;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		x.collectFields(fields);
		y.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch (vendor) {
		case Postgres:
			return new StringBuilder(1024).append("ST_SetSRID(ST_Point(").append(x.format(vendor, options)).append(", ")
					.append(y.format(vendor, options)).append("), ").append(srs.format(vendor, options)).append(')').toString();
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Geometry;
	}

}
