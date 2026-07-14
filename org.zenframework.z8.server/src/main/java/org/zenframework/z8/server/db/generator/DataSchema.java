package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.SelectStatement;
import org.zenframework.z8.server.engine.IDatabase;

public class DataSchema {
	private final Map<String, TableDescription> tables = new HashMap<String, TableDescription>();
	private final Map<String, ForeignKey> foreignKeys = new HashMap<String, ForeignKey>();

	public DataSchema initialize() {
		try {
			collectTableDescriptions();
			collectPrimaryKeys();
			collectIndixes();
			collectForeignKeys();
			return this;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<String, TableDescription> getTables() {
		return tables;
	}

	public Map<String, ForeignKey> getForeignKeys() {
		return foreignKeys;
	}

	private void collectTableDescriptions() throws SQLException {
		IDatabase database = ConnectionManager.database();
		Cursor cursor = SelectStatement.cursor(database.dialect().getTables(database));

		try {
			while (cursor.next()) {
				String tableName = cursor.getString(1).get();

				if (!tables.containsKey(tableName))
					tables.put(tableName, new TableDescription(tableName));

				String fieldName = cursor.getString(2).get();
				String typeName = cursor.getString(3).get();
				int length = ConnectionManager.dialect().formatLength(cursor.getInteger(4).getInt(), typeName);
				int scale = cursor.getInteger(5).getInt();
				boolean nullable = cursor.getBoolean(6).get();
				String defaultValue = ConnectionManager.dialect().formatDefaultValue(cursor.getString(7).get(), typeName);

				tables.get(tableName).addField(new Column(fieldName, typeName, length, scale, nullable, defaultValue));
			}
		} finally {
			cursor.close();
		}
	}

	private void collectPrimaryKeys() throws SQLException {
		IDatabase database = ConnectionManager.database();
		Cursor cursor = SelectStatement.cursor(database.dialect().getPrimaryKeys(database));

		try {
			while (cursor.next()) {
				String pk_name = cursor.getString(1).get();
				String table_name = cursor.getString(2).get();
				String col_name = cursor.getString(3).get();

				TableDescription table = tables.get(table_name);

				if (table == null)
					continue;

				PrimaryKey primaryKey = table.getPrimaryKey();

				if (primaryKey == null)
					table.setPrimaryKey(new PrimaryKey(pk_name, table_name, col_name));
				else
					primaryKey.addField(col_name);
			}
		} finally {
			cursor.close();
		}
	}

	private void collectIndixes() throws SQLException {
		IDatabase database = ConnectionManager.database();
		Cursor cursor = SelectStatement.cursor(database.dialect().getIndixes(database));

		try {
			while (cursor.next()) {
				String indexName = cursor.getString(1).get();
				String tableName = cursor.getString(2).get();
				String column = cursor.getString(3).get();
				boolean unique = cursor.getBoolean(4).get();

				TableDescription table = tables.get(tableName);

				if (table == null)
					continue;

				Index index = table.getIndex(indexName);

				if (index == null)
					table.addIndex(new Index(tableName, column, indexName, unique, true));
				else
					index.addField(column);
			}
		} finally {
			cursor.close();
		}
	}

	private void collectForeignKeys() throws SQLException {
		IDatabase database = ConnectionManager.database();
		Cursor cursor = SelectStatement.cursor(database.dialect().getForeignKeys(database));

		try {
			while (cursor.next()) {
				String referenceTable = cursor.getString(1).get();
				String referenceField = cursor.getString(2).get();
				String table = cursor.getString(3).get();
				String field = cursor.getString(4).get();
				String name = cursor.getString(5).get();

				ForeignKey foreignKey = new ForeignKey(referenceTable, referenceField, table, field, name);

				TableDescription referer = tables.get(table);
				TableDescription reference = tables.get(referenceTable);

				if (referer != null)
					referer.addForeignKey(foreignKey);
				if (reference != null)
					reference.addReferer(foreignKey);

				foreignKeys.put(foreignKey.getName(), foreignKey);
			}
		} finally {
			cursor.close();
		}
	}
}
