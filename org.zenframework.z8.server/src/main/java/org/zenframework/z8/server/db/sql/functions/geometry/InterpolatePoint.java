package org.zenframework.z8.server.db.sql.functions.geometry;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class InterpolatePoint extends SqlToken {

	private final SqlToken geometry, fraction;

	public InterpolatePoint(SqlToken geometry, SqlToken fraction) {
		this.geometry = geometry;
		this.fraction = fraction;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		geometry.collectFields(fields);
		fraction.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch (vendor) {
		case Postgres:
			return new StringBuilder(1024).append("ST_Line_Interpolate_Point(").append(geometry.format(vendor, options)).append(", ")
					.append(fraction.format(vendor, options)).append(')').toString();
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Geometry;
	}

}
