package org.zenframework.z8.server.db.sql.functions.geometry;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.sql.sql_integer;

public class Element extends SqlToken {

	private final SqlToken geometry, n;

	public Element(SqlToken geometry, SqlToken n) {
		this.geometry = geometry;
		this.n = n;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		geometry.collectFields(fields);
		n.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch (vendor) {
		case Postgres:
			return new StringBuilder(1024).append("ST_GeometryN(").append(geometry.format(vendor, options)).append(", ")
					.append(new sql_integer(n).operatorAdd(new sql_integer(1)).format(vendor, options)).append(')')
					.toString();
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Geometry;
	}

}
