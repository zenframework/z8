package org.zenframework.z8.server.db.dialect;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.engine.IDatabase;

public class H2Dialect extends PostgresDialect {

	public static final String Name = "H2";

	@Override
	public String name() {
		return Name;
	}

	@Override
	public DatabaseVendor vendor() {
		return DatabaseVendor.H2;
	}

	@Override
	public String getForeignKeys(IDatabase database) {
		return "select pktable_name, pkcolumn_name, fktable_name, fkcolumn_name, fk_name from information_schema.cross_references"
				+ " where fktable_schema = '" + database.schema() + "'";
	}

}
