package org.zenframework.z8.server.db;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.utils.IOUtils;

public class DmlStatement extends Statement {
	private List<Closeable> closeables = new ArrayList<Closeable>(10);

	protected DmlStatement(Connection connection, String sql) {
		this(connection, sql, 0);
	}

	public static int execute(String sql) throws SQLException {
		return execute(sql, 0);
	}

	public static int execute(String sql, int priority) throws SQLException {
		DmlStatement statement = new DmlStatement(ConnectionManager.get(), sql);

		try {
			return statement.executeUpdate();
		} catch(SQLException e) {
			SqlExceptionConverter.rethrow(statement.getVendor(), e);
		} finally {
			statement.close();
		}

		return 0;
	}

	protected DmlStatement(Connection connection, String sql, int priority) {
		super(connection, sql, priority);
	}

	@Override
	protected PreparedStatement createPreparedStatement(Connection connection, String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	@Override
	protected void prepare() throws SQLException {
	}

	public int execute() {
		try {
			return executeUpdate();
		} catch(Throwable e) {
			log();
			throw new RuntimeException(e);
		} finally {
			close();
		}
	}

	protected void log() {
	}

	protected void setBinary(int position, binary value) throws SQLException {
		super.setBinary(position, value);

		InputStream stream = value.get();
		if(stream instanceof FileInputStream)
			closeables.add(stream);
	}

	@Override
	public void close() {
		if(getConnection().inTransaction())
			return;

		IOUtils.closeQuietly(closeables);
		closeables.clear();

		super.close();
	}
}
