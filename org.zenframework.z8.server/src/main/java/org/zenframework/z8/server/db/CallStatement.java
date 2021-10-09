package org.zenframework.z8.server.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.primary;

public class CallStatement extends Statement {
	private Collection<primary> parameters;

	static public CallStatement create(Connection connection, String name, Collection<primary> parameters) {
		String sql = "{ call " + connection.getVendor().quote(name) + "(";

		for(int i = 0; i < parameters.size(); i++)
			sql += (i != 0 ? ", " : "") + "?";

		sql += ")}";

		return new CallStatement(connection, sql, parameters);
	}

	private CallStatement(Connection connection, String sql, Collection<primary> parameters) {
		super(connection, sql, 0);
		this.parameters = parameters;
	}

	@Override
	protected PreparedStatement createPreparedStatement(Connection connection, String sql) throws SQLException {
		return connection.prepareCall(sql);
	}

	@Override
	protected void prepare() throws SQLException {
		int position = 1;
		for(primary parameter : parameters)
			set(position++, parameter != null ? parameter.type() : FieldType.Null, parameter);
	}

	public void execute() {
		try {
			executeCall();
		} catch(Throwable e) {
			log();
			throw new RuntimeException(e);
		} finally {
			close();
		}
	}

	private void log() {
		Trace.logEvent(getSql());
	}
}