package org.zenframework.z8.server.db.sql.functions.geometry;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Extract extends SqlToken {

	private final SqlToken geometry, type;

	public Extract(SqlToken geometry, SqlToken type) {
		this.geometry = geometry;
		this.type = type;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		geometry.collectFields(fields);
		type.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch (vendor) {
		case Postgres:
			return new StringBuilder(1024).append("ST_CollectionExtract(").append(geometry.format(vendor, options))
					.append(", ").append(type.format(vendor, options)).append(')').toString();
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Geometry;
	}

}
