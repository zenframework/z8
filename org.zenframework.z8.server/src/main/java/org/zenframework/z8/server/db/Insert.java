package org.zenframework.z8.server.db;

import java.sql.SQLException;
import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.logs.Trace;

public class Insert extends DmlStatement {
	private Collection<Field> fields;

	static public Insert create(Query query, Collection<Field> fields) {
		Connection connection = query.getConnection();
		IDatabase database = connection.database();
		DatabaseVendor vendor = database.vendor();

		String insertFields = "";
		String insertValues = "";

		for(Field field : fields) {
			if(field.isExpression())
				continue;
			insertFields += (insertFields.isEmpty() ? "" : ", ") + vendor.quote(field.name());
			insertValues += (insertValues.isEmpty() ? "" : ", ") + "?";
		}

		String sql = "insert into " + database.tableName(query.name()) + " " + "(" + insertFields + ") values (" + insertValues + ")";

		Insert insert = (Insert)connection.getStatement(sql);
		return insert != null ? insert : new Insert(query.getConnection(), sql, query.priority(), fields);
	}

	private Insert(Connection connection, String sql, int priority, Collection<Field> fields) {
		super(connection, sql, priority);
		this.fields = fields;
	}

	@Override
	public void prepare() throws SQLException {
		int position = 1;

		for(Field field : fields) {
			if(field.isExpression())
				continue;
			set(position++, field,  field.getDefault());
		}
	}

	@Override
	protected void log() {
		Trace.logEvent(sql());

		for(Field field : fields) {
			if(field.isExpression())
				continue;
			Trace.logEvent(field.name() + ": " + field.getDefault());
		}
	}
}
