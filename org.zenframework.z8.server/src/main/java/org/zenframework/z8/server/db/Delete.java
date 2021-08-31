package org.zenframework.z8.server.db;

import java.sql.SQLException;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;

public class Delete extends DmlStatement {
	private guid recordId;

	static public Delete create(Query query, guid recordId) {
		Connection connection = query.getConnection();
		IDatabase database = connection.database();
		DatabaseVendor vendor = database.vendor();

		String sql = "delete from " + database.tableName(query.name()) + " where " + vendor.quote(query.primaryKey().name()) + "=?";

		Delete delete = (Delete)connection.getStatement(sql);
		return delete != null ? delete : new Delete(query.getConnection(), sql, Integer.MAX_VALUE - query.priority(), recordId);
	}

	private Delete(Connection connection, String sql, int priority, guid recordId) {
		super(connection, sql, priority);
		this.recordId = recordId;
	}

	@Override
	public void prepare() throws SQLException {
		super.prepare();
		set(1, FieldType.Guid, recordId);
	}

	@Override
	protected void log() {
		Trace.logEvent(sql() + '\n' + "recordId: " + recordId);
	}
}
