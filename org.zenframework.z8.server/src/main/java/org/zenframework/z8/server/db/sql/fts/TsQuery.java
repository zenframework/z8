package org.zenframework.z8.server.db.sql.fts;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class TsQuery extends SqlToken {

	private SqlToken string;
	private FtsConfig config;

	public TsQuery(SqlToken string, FtsConfig config) {
		this.string = string;
		this.config = config;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		string.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		StringBuilder str = new StringBuilder(1024);

		if (vendor == DatabaseVendor.Postgres) {
			switch(config.queryType) {
			case Phrase:
				str.append("phrase");
				break;
			case Plain:
				str.append("plain");
				break;
			case Default:
				break;
			}
			str.append("to_tsquery(");
			if (config.config != null)
				str.append('\'').append(config.config.get()).append("', ");
			str.append(new ToString(string).format(vendor, options, logicalContext)).append(')');
		} else
			throw new UnknownDatabaseException();

		return str.toString();
	}

	@Override
	public FieldType type() {
		return FieldType.None;
	}
}
