package org.zenframework.z8.server.db;

import java.sql.SQLException;
import java.util.Collection;

import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.primary;

public class Call extends BasicStatement {
	private Collection<primary> parameters;

	public Call(String name, Collection<primary> parameters) {
		this(name, parameters, null);
	}

	public Call(String name, Collection<primary> parameters, Connection connection) {
		super(connection);

		this.parameters = parameters;

		connection = connection();
		DatabaseVendor vendor = connection.vendor();

		sql = "{ call " + vendor.quote(name) + "(";

		for(int i = 0; i < parameters.size(); i++)
			sql += (i != 0 ? ", " : "") + "?";

		sql += ")}";
	}

	@Override
	public void prepare(String sql, int priority) throws SQLException {
		this.sql = sql;
		this.priority = priority;
		this.statement = connection().prepareCall(sql);

		int position = 1;
		for(primary parameter : parameters) {
			set(position, parameter != null ? parameter.type() : FieldType.Null, parameter);
			position++;
		}
	}

	public void execute() {
		try {
			prepare(sql);
			executeCall();
		} catch(Throwable e) {
			Trace.logEvent(sql());
			Trace.logError(e);
			throw new RuntimeException(e);
		} finally {
			close();
		}
	}
}
