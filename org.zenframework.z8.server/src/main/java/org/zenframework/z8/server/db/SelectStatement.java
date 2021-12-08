package org.zenframework.z8.server.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.zenframework.z8.server.engine.Database;

public class SelectStatement extends Statement {
	private Cursor cursor = null;

	public SelectStatement(Connection connection, String sql) {
		super(connection, sql, 0);
	}

	@Override
	protected PreparedStatement createPreparedStatement(Connection connection, String sql) throws SQLException {
		return  connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	@Override
	protected void prepare() throws SQLException {
	}

	public Cursor execute() throws SQLException {
		if(cursor != null)
			cursor.close();
		return cursor = new Cursor(this);
	}

	@Override
	public void close() {
		if(cursor != null && !cursor.isClosed()) {
			cursor.close();
			cursor = null;
		}
		super.close();
	}

	public static Cursor cursor(String sql) throws SQLException {
		return cursor(ConnectionManager.get(), sql);
	}

	public static Cursor cursor(Database database, String sql) throws SQLException {
		return cursor(ConnectionManager.get(database), sql);
	}

	public static Cursor cursor(Connection connection, String sql) throws SQLException {
		SelectStatement select = new SelectStatement(connection, sql);
		return select.execute();
	}
}
