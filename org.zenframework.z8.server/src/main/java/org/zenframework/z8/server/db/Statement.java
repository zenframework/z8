package org.zenframework.z8.server.db;

import java.sql.SQLException;

public class Statement extends BasicStatement {
	public Statement(Connection connection) {
		super(connection);
	}

	@Override
	public void prepare(String sql, int priority) throws SQLException {
		this.sql = sql;
		this.priority = priority;
		this.statement = connection().prepareStatement(sql, priority);
	}

	public static int executeUpdate(String sql) throws SQLException {
		return executeUpdate(sql, 0);
	}
	
	public static int executeUpdate(String sql, int priority) throws SQLException {
		Connection connection = ConnectionManager.get();

		Statement statement = new Statement(connection);
		statement.prepare(sql, priority);

		try {
			return statement.executeUpdate();
		} catch(SQLException e) {
			SqlExceptionConverter.rethrow(statement.vendor(), e);
		} finally {
			statement.close();
		}

		return 0;
	}
}
