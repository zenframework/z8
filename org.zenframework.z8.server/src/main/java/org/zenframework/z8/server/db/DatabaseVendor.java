package org.zenframework.z8.server.db;

import org.zenframework.z8.server.db.dialect.DatabaseDialect;
import org.zenframework.z8.server.db.dialect.H2Dialect;
import org.zenframework.z8.server.db.dialect.OracleDialect;
import org.zenframework.z8.server.db.dialect.PostgresDialect;
import org.zenframework.z8.server.db.dialect.SqlServerDialect;

public enum DatabaseVendor {
	Oracle(new OracleDialect()),
	SqlServer(new SqlServerDialect()),
	Postgres(new PostgresDialect()),
	H2(new H2Dialect());

	private final DatabaseDialect dialect;

	private DatabaseVendor(DatabaseDialect dialect) {
		this.dialect = dialect;
	}

	public DatabaseDialect dialect() {
		return dialect;
	}

	public static DatabaseVendor fromString(String name) {
		if (name == null)
			return DatabaseVendor.Postgres;

		name = name.toUpperCase();

		if (name.contains(OracleDialect.Name.toUpperCase()))
			return DatabaseVendor.Oracle;
		else if (name.contains(SqlServerDialect.Name.toUpperCase()))
			return DatabaseVendor.SqlServer;
		else if (name.contains(PostgresDialect.Name.toUpperCase()))
			return DatabaseVendor.Postgres;
		else if (name.contains(H2Dialect.Name.toUpperCase()))
			return DatabaseVendor.H2;
		else
			return DatabaseVendor.Postgres;
	}
}
