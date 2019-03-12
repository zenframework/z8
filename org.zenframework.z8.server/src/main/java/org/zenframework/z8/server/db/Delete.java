package org.zenframework.z8.server.db;

import java.sql.SQLException;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;

public class Delete extends Statement {
	private guid recordId;
	private Query query;

	public Delete(Query query, guid recordId) {
		super(ConnectionManager.get());

		if(recordId == null)
			throw new RuntimeException("Delete: recordId == null");

		this.query = query; 
		this.recordId = recordId;

		Connection connection = ConnectionManager.get();
		Database database = connection.database();
		DatabaseVendor vendor = connection.vendor();

		sql = "delete from " + database.tableName(query.name()) + 
				" where " + vendor.quote(query.primaryKey().name()) + "=?";
	}

	@Override
	public void prepare(String sql, int priority) throws SQLException {
		super.prepare(sql, priority);
		set(1, FieldType.Guid, recordId);
	}

	public int execute() {
		try {
			prepare(sql, Integer.MAX_VALUE - query.priority());
			return executeUpdate();
		} catch(Throwable e) {
			Trace.logEvent(sql());
			Trace.logEvent("recordId: " + recordId);
			Trace.logError(e);
			throw new RuntimeException(e);
		} finally {
			close();
		}
	}
}
