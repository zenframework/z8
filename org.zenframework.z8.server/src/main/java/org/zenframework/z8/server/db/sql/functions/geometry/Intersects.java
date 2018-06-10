package org.zenframework.z8.server.db.sql.functions.geometry;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Intersects extends SqlToken {

	private final SqlToken geometry1, geometry2;

	public Intersects(SqlToken geometry1, SqlToken geometry2) {
		this.geometry1 = geometry1;
		this.geometry2 = geometry2;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		geometry1.collectFields(fields);
		geometry2.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch (vendor) {
		case Postgres:
			return new StringBuilder(1024).append("ST_Intersects(").append(geometry1.format(vendor, options)).append(", ")
					.append(geometry2.format(vendor, options)).append(')').toString();
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Boolean;
	}

}
