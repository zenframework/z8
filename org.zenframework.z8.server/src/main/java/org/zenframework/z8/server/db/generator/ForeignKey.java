package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;

import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.Statement;
import org.zenframework.z8.server.engine.IDatabase;

public class ForeignKey {
	public String name;
	public String referenceTable;
	public String referenceField;
	public String table;
	public String field;

	private boolean dropped = false;

	public ForeignKey(String referenceTable, String referenceField, String table, String field, String name) {
		this.name = name;
		this.referenceTable = referenceTable;
		this.referenceField = referenceField;
		this.table = table;
		this.field = field;
	}

	public ForeignKey(String table, IForeignKey fk, int index) {
		this.name = "FK" + index + "_" + table;
		this.referenceTable = fk.getReferencedTable().name();
		this.referenceField = fk.getReferer().name();
		this.table = table;
		this.field = fk.getFieldDescriptor().name();
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o != null ? hashCode() == o.hashCode() : false;
	}

	public void drop() throws SQLException {
		if(!dropped) {
			Connection connection = ConnectionManager.get();
			IDatabase database = connection.database();
			DatabaseVendor vendor = database.vendor();

			String sql = "alter table " + database.tableName(table) + " drop constraint " + vendor.quote(name);
			Statement.executeUpdate(sql);

			dropped = true;
		}
	}
}
