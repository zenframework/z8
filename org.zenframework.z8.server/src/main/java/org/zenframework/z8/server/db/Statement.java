package org.zenframework.z8.server.db;

import java.sql.SQLException;

public class Statement extends BasicStatement {
	public Statement(Connection connection) {
		super(connection);
	}

	@Override
	public void prepare(String sql) throws SQLException {
		this.sql = sql;
		this.statement = connection().prepareStatement(sql);
	}

	public static int executeUpdate(String sql) throws SQLException {
		Connection connection = ConnectionManager.get();

		Statement statement = new Statement(connection);
		statement.prepare(sql);

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
