package org.zenframework.z8.server.db;

import java.sql.SQLException;
import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Update extends DmlStatement {
	private Collection<Field> fields;
	private guid recordId;

	static public Update create(Query query, Collection<Field> fields, guid recordId) {
		return create(query, fields, recordId, null);
	}

	static public Update create(Query query, Collection<Field> fields, guid recordId, sql_bool where) {
		Connection connection = query.getConnection();
		IDatabase database = connection.database();
		DatabaseVendor vendor = database.vendor();

		String sql = "update " + database.tableName(query.name()) + (vendor == DatabaseVendor.SqlServer ? " as " : " ") + query.getAlias();

		String set = "";

		for(Field field : fields) {
			if(field.isPrimaryKey() || field.isExpression())
				continue;
			set += (set.isEmpty() ? "" : ", ") + vendor.quote(field.name()) + "=" + "?";
		}

		String whereClause = "";

		if(recordId != null)
			whereClause = vendor.quote(query.primaryKey().name()) + "=?";

		if(where != null)
			whereClause += (whereClause.isEmpty() ? "" : " and ") + "(" + where.format(vendor, new FormatOptions(), true) + ")";

		sql += " set " + set + (whereClause.isEmpty() ? "" : " where " + whereClause);

		Update update = (Update)connection.getStatement(sql);
		return update != null ? update.initialize(fields, recordId) : new Update(query.getConnection(), sql, query.priority(), fields, recordId);
	}

	private Update(Connection connection, String sql, int priority, Collection<Field> fields, guid recordId) {
		super(connection, sql, priority);
		initialize(fields, recordId);
	}

	private Update initialize(Collection<Field> fields, guid recordId) {
		this.fields = fields;
		this.recordId = recordId;
		return this;
	}

	@Override
	protected void prepare() throws SQLException {
		super.prepare();

		int position = 1;

		for(Field field : fields) {
			if(field.isPrimaryKey() || field.isExpression())
				continue;
			set(position++, field, field.getDefaultValue());
		}

		if(recordId != null)
			set(position, FieldType.Guid, recordId);
	}

	@Override
	protected void log() {
		Trace.logEvent(sql());
		for(Field field : fields)
			Trace.logEvent(field.name() + ": " + field.get());
	}
}