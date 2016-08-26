package org.zenframework.z8.server.base.model.sql;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.Statement;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;

public class Delete {
	private Query query;
	private guid recordId;

	public Delete(Query query, guid recordId) {
		if(recordId == null)
			throw new RuntimeException("Delete: recordId == null");
		this.query = query;
		this.recordId = recordId;
	}

	public int execute() {
		Query rootQuery = query.getRootQuery();
		Field primaryKey = rootQuery.primaryKey();

		Connection connection = ConnectionManager.get();
		Database database = connection.database();
		DatabaseVendor vendor = connection.vendor();

		String sql = "delete from " + database.tableName(rootQuery.name());

		sql += " where " + vendor.quote(primaryKey.name()) + "=" + recordId.sql_guid().format(vendor, new FormatOptions(), true);

		try {
			return Statement.executeUpdate(connection, sql);
		} catch(Throwable e) {
			System.out.println(sql);

			Trace.logError(e);

			throw new RuntimeException(e);
		}
	}
}
