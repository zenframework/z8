package org.zenframework.z8.server.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.zenframework.z8.server.engine.IDatabase;

public class BasicSelect extends BasicStatement {
	private Cursor cursor = null;

	public BasicSelect(Connection connection) {
		super(connection);
	}

	@Override
	public void prepare(String sql, int priority) throws SQLException {
		this.sql = sql;
		this.statement = connection().prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	public Cursor execute() throws SQLException {
		if(cursor != null)
			cursor.close();
		return cursor = new Cursor(this);
	}

	@Override
	protected void cleanup() throws SQLException {
		if(cursor != null && !cursor.isClosed()) {
			cursor.close();
			cursor = null;
		}
		super.cleanup();
	}

	public static Cursor cursor(String sql) throws SQLException {
		return cursor(ConnectionManager.get(), sql);
	}

	public static Cursor cursor(IDatabase database, String sql) throws SQLException {
		return cursor(ConnectionManager.get(database), sql);
	}

	public static Cursor cursor(Connection connection, String sql) throws SQLException {
		BasicSelect select = new BasicSelect(connection);
		select.prepare(sql);
		return select.execute();
	}
}
