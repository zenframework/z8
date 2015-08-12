package org.zenframework.z8.server.db.generator.fk;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.BasicSelect;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.CLASS;

public class ForeignKey {
    private String tableName;
    private String linkName;

    private CLASS<Table> table;
    private Field link;

    public ForeignKey(String tableName, String linkName) {
        this.tableName = tableName;
        this.linkName = linkName;
    }

    public Table getTable() {
        if(table == null) {
            table = findTable();
        }

        return table.get();
    }

    public Field getLink() {
        if(link == null) {
            link = findLink();
        }
        return link;
    }

    static public Collection<ForeignKey> get(Connection connection, String tableName) throws SQLException {
        Collection<ForeignKey> result = new ArrayList<ForeignKey>();

        String sql = "" + "SELECT " + "KCU.TABLE_NAME AS 'FK_TABLE_NAME', " + "KCU.COLUMN_NAME AS 'FK_COLUMN_NAME' "
                + "FROM " + "INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS RC, " + "INFORMATION_SCHEMA.KEY_COLUMN_USAGE KCU, "
                + "INFORMATION_SCHEMA.TABLE_CONSTRAINTS TC " + "WHERE "
                + "KCU.CONSTRAINT_CATALOG = RC.CONSTRAINT_CATALOG AND "
                + "KCU.CONSTRAINT_SCHEMA = RC.CONSTRAINT_SCHEMA AND " + "KCU.CONSTRAINT_NAME = RC.CONSTRAINT_NAME AND "
                + "RC.UNIQUE_CONSTRAINT_NAME = TC.CONSTRAINT_NAME"
                + (tableName != null ? " AND TC.TABLE_NAME = '" + tableName + "'" : "");

        Cursor cursor = BasicSelect.cursor(connection, sql);

        while(cursor.next()) {
            String table = cursor.getString(1).get();
            String link = cursor.getString(2).get();

            result.add(new ForeignKey(table, link));
        }

        cursor.close();

        return result;
    }

    @SuppressWarnings("unchecked")
    private CLASS<Table> findTable() {
        Collection<Table.CLASS<? extends Table>> tables = Runtime.instance().tables();

        for(CLASS<? extends Table> table : tables) {
            if(table.name().equalsIgnoreCase(tableName)) {
                return (CLASS<Table>)table;
            }
        }

        return null;
    }

    private Field findLink() {
        Table table = getTable();

        for(Field field : table.getDataFields()) {
            String name = field.name();

            if(name != null && name.equalsIgnoreCase(linkName)) {
                return field;
            }
        }

        return null;
    }
}
