package org.zenframework.z8.server.db.sql.fts;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class TsRank extends SqlToken {
	private SqlToken tsvector;
	private SqlToken tsquery;
	private FtsConfig config;

	public TsRank(SqlToken tsvector, SqlToken tsquery, FtsConfig config) {
		this.tsvector = tsvector;
		this.tsquery = tsquery;
		this.config = config;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		tsvector.collectFields(fields);
		tsquery.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		StringBuilder str = new StringBuilder(1024);

		if (vendor == DatabaseVendor.Postgres) {
			str.append("ts_rank");
			if (config.coatingDensity.get())
				str.append("_cd");
			str.append('(');
			if (config.weight != null && !config.weight.isEmpty()) {
				str.append('{').append(config.weight.get(0));
				for (int i = 1; i < config.weight.size(); i++)
					str.append(", ").append(config.weight.get(i));
				str.append("}, ");
			}
			str.append(tsvector.format(vendor, options, logicalContext)).append(", ");
			str.append(tsquery.format(vendor, options, logicalContext)).append(", ");
			str.append(config.normalization.getInt()).append(')');
		} else
			throw new UnknownDatabaseException();

		return str.toString();
	}

	@Override
	public FieldType type() {
		return FieldType.Decimal;
	}
}
