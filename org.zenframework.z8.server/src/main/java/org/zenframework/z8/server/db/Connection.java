package org.zenframework.z8.server.db;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.encoding;

public class Connection {
	static public int TransactionReadCommitted = java.sql.Connection.TRANSACTION_READ_COMMITTED;
	static public int TransactionReadUncommitted = java.sql.Connection.TRANSACTION_READ_UNCOMMITTED;
	static public int TransactionRepeatableRead = java.sql.Connection.TRANSACTION_REPEATABLE_READ;
	static public int TransactionSerializable = java.sql.Connection.TRANSACTION_SERIALIZABLE;

	static public final int DefaultFetchSize = 1000;
	static public final int MaxBatchSize = 1000;

	static private long FiveMinutes = 5 * datespan.TicksPerMinute;

	private IDatabase database = null;
	private java.sql.Connection connection = null;
	private Thread owner = null;

	private int transactionCount = 0;
	private Batch batch;

	private long lastUsed = System.currentTimeMillis();

	private Set<Statement> statements = new HashSet<Statement>();
	private Collection<Listener> listeners;

	static private java.sql.Connection newConnection(IDatabase database) {
		ConnectionSavePoint savePoint = ConnectionSavePoint.create(database);

		try {
			Class.forName(database.driver());
			return DriverManager.getConnection(database.connection(), database.user(), database.password());
		} catch(Throwable e) {
			throw new RuntimeException(e);
		} finally {
			savePoint.restore();
		}
	}

	static public Connection connect(IDatabase database) {
		return new Connection(database, newConnection(database));
	}

	private Connection(IDatabase database, java.sql.Connection connection) {
		this.database = database;
		this.connection = connection;
	}

	public interface Listener {
		void on(Connection connection, ConnectionEvent event);
	}

	public void addListener(Listener listener) {
		if(listeners == null)
			listeners = new ArrayList<Listener>();
		listeners.add(listener);
	}

	public void close() {
		try {
			if(connection != null)
				connection.close();
		} catch(SQLException e) {
			Trace.logError(e);
		} finally {
			connection = null;
			transactionCount = 0;
			batch = null;
			statements.clear();
		}
	}

	private void reconnect() {
		close();
		connection = newConnection(database);

		initClientInfo();
	}

	public boolean isCurrent() {
		return Thread.currentThread().equals(owner);
	}

	private boolean isAlive() {
		return owner != null && owner.isAlive();
	}

	public boolean isInUse() {
		return !isCurrent() ? isAlive() : false;
	}

	public boolean isUnused() {
		return !isInUse() && System.currentTimeMillis() - lastUsed >= FiveMinutes;
	}

	public boolean isClosed() {
		return connection == null;
	}

	public boolean inTransaction() {
		return transactionCount != 0;
	}

	public void use() {
		if(inTransaction() || isClosed())
			reconnect();

		lastUsed = System.currentTimeMillis();

		owner = Thread.currentThread();
		initClientInfo();
	}

	public void release() {
		if(!inTransaction())
			owner = null;

		for(Statement statement : statements)
			statement.safeClose();

		statements.clear();
	}

	private void initClientInfo() {
		if(!ServerConfig.traceSqlConnections())
			return;

		try {
			connection.setClientInfo("ApplicationName", owner.getName());
		} catch(SQLClientInfoException e) {
		}
	}

	public java.sql.Connection getSqlConnection() {
		return connection;
	}

	public IDatabase database() {
		return database;
	}

	public String schema() {
		return database.schema();
	}

	public encoding charset() {
		return database.charset();
	}

	public DatabaseVendor vendor() {
		return database.vendor();
	}

	private void checkAndReconnect(SQLException exception) throws SQLException {
		String message = exception.getMessage();
		String sqlState = exception.getSQLState();
		int errorCode = exception.getErrorCode();

		SqlExceptionConverter.rethrowIfKnown(database.vendor(), exception);

		Trace.logEvent(message + "(error code: " + errorCode + "; sqlState: " + sqlState + ") - reconnecting...");

		// Postgres; Class 08 — Connection Exception SQLState Description
		// 08000 connection exception 
		// 08003 connection does not exist
		// 08006 connection failure
		// 08001 sqlclient unable to establish sqlconnection
		// 08004 sqlserver rejected establishment of sqlconnection
		// 08007 transaction resolution unknown
		// 08P01 protocol violation

		if(inTransaction())
			throw exception;

		reconnect();
	}

	private void setAutoCommit(boolean autoCommit) throws SQLException {
		try {
			connection.setAutoCommit(autoCommit);
		} catch(SQLException e) {
			checkAndReconnect(e);
			connection.setAutoCommit(autoCommit);
		}
	}

	public void beginTransaction() {
		try {
			if(!inTransaction()) {
				setAutoCommit(false);
				batch = new Batch();
			}

			transactionCount++;
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void commit() {
		try {
			if(!inTransaction())
				throw new RuntimeException("Connection.commit() - not in transaction");

			transactionCount--;
			if(inTransaction())
				return;

			onCommit();
			batch.commit();
			batch = null;
			connection.commit();
			setAutoCommit(true);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void onCommit() {
		onEvent(ConnectionEvent.Commit);
		listeners = null;
	}

	public void rollback() {
		try {
			if(!inTransaction())
				return;

			transactionCount--;
			if(inTransaction())
				return;

			onRollback();
			batch.rollback();
			batch = null;
			connection.rollback();
			setAutoCommit(true);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void onRollback() {
		onEvent(ConnectionEvent.Rollback);
		listeners = null;
	}

	public void execute(String sql) {
		try {
			new DmlStatement(this, sql).executeUpdate();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void flush() {
		if(!inTransaction())
			return;

		try {
			onFlush();
			batch.flush();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void onFlush() {
		onEvent(ConnectionEvent.Flush);
	}

	private void onEvent(ConnectionEvent event) {
		if(listeners == null)
			return;

		for(Listener listener : listeners)
			listener.on(this, event);
	}

	public boolean isOpen() {
		try {
			return !connection.isClosed();
		} catch(SQLException e) {
			Trace.logError(e);
		}
		return false;
	}

	public Statement getStatement(String sql) {
		return inTransaction() ? batch.statement(sql) : null;
	}

	public PreparedStatement prepareCall(String sql) throws SQLException {
		try {
			return connection.prepareCall(sql);
		} catch(SQLException e) {
			checkAndReconnect(e);
			return connection.prepareCall(sql);
		}
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		try {
			return connection.prepareStatement(sql);
		} catch(SQLException e) {
			checkAndReconnect(e);
			return connection.prepareStatement(sql);
		}
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		try {
			return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
		} catch(SQLException e) {
			checkAndReconnect(e);
			return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
		}
	}

	private ResultSet doExecuteQuery(Statement statement) throws SQLException {
		PreparedStatement preparedStatement = statement.preparedStatement();
		preparedStatement.setFetchSize(DefaultFetchSize);
		return preparedStatement.executeQuery();
	}

	public ResultSet executeQuery(Statement statement) throws SQLException {
		try {
			ResultSet resultSet = doExecuteQuery(statement);
			statements.add(statement);
			return resultSet;
		} catch(SQLException e) {
			checkAndReconnect(e);
			statement.safeClose();
			statement.prepare();
			return doExecuteQuery(statement);
		}
	}

	public int executeUpdate(Statement statement) throws SQLException {
		try {
			if(inTransaction()) {
				batch.add(statement);
				return 0;
			}
			return statement.preparedStatement().executeUpdate();
		} catch(SQLException e) {
			checkAndReconnect(e);
			statement.safeClose();
			statement.prepare();
			return statement.preparedStatement().executeUpdate();
		}
	}

	public void executeCall(Statement statement) throws SQLException {
		try {
			statement.preparedStatement().execute();
		} catch(SQLException e) {
			checkAndReconnect(e);
			statement.safeClose();
			statement.prepare();
			statement.preparedStatement().execute();
		}
	}
}
