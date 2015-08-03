package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zenframework.z8.server.base.model.sql.CountingSelect;
import org.zenframework.z8.server.base.model.sql.Insert;
import org.zenframework.z8.server.base.model.sql.Update;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.ITable;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.BasicSelect;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.Statement;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.db.sql.functions.IsNull;
import org.zenframework.z8.server.db.sql.functions.conversion.ToBytes;
import org.zenframework.z8.server.db.sql.functions.conversion.ToChar;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.exceptions.db.ObjectAlreadyExistException;
import org.zenframework.z8.server.exceptions.db.ObjectNotFoundException;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_guid;
import org.zenframework.z8.server.utils.ErrorUtils;

public class TableGenerator {
    private Table.CLASS<? extends Table> tableClass = null;
    private Table table = null;

    private GeneratorAction action;
    private ILogger logger;
    private Map<String, ColumnDescGen> dbFields = new HashMap<String, ColumnDescGen>();
    private Collection<ColumnDescAlter> dbFieldsAlter;
    private TableDescription dbTable;

    private Connection connection = null;

    public TableGenerator(Table.CLASS<? extends Table> tableClass, GeneratorAction action, TableDescription dbTable, ILogger logger) {
        this.action = action;
        this.dbTable = dbTable;

        this.logger = logger;

        this.tableClass = tableClass;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String name() {
        return tableClass.name();
    }

    public String displayName() {
        return tableClass.displayName();
    }

    public Table table() {
        if (table == null) {
            table = (Table) tableClass.newInstance();
        }
        return table;
    }

    public GeneratorAction getAction() {
        return action;
    }

    private Database database() {
        return connection.database();
    }

    private DatabaseVendor vendor() {
        return connection.vendor();
    }

    public void beforeGenerate() {
        table().beforeGenerate();
    }
    
    public void afterGenerate() {
        table().afterGenerate();
    }
    
    public void create(Connection connection) {
        this.connection = connection;

        try {
            if (action == GeneratorAction.Alter) {
                action = checkAlter();
            }

            if (action != GeneratorAction.Create) {
                try {
                    dropAllKeys(connection, dbTable, logger);
                } catch (ObjectNotFoundException e) {}
            }

            switch (action) {
            case None:
                break;
            case Create:
                createTable(name());
                break;
            case Alter:
                alterTable();
                break;
            case Recreate:
                recreateTable();
                break;
            default:
                assert (false);
            }
        } catch (SQLException e) {
            logger.error(
                    e,
                    Resources.format("Generator.createTableError", displayName(),
                            "[" + connection.schema() + "]." + name(), ErrorUtils.getMessage(e)));
        } finally {
            this.table = null;

            dbFields = null;
            dbFieldsAlter = null;
            dbTable = null;
        }
    }

    public void createEmptyTable() {
        table = (Table) tableClass.get();

        try {
            createTable(name());
            createPrimaryKey();
            packIndex();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            table = null;
        }
    }

    public void createForeignKeys() {
        try {
            packFK();
            packIndex();
            packUnique();
        } finally {
            this.table = null;
        }
    }

    public void createRecords() {
        createNullRecord();

        for (Map<IField, primary> record : table().getStaticRecords()) {
            createStaticRecord(record);
        }

        correctTreeTableStructure();

        this.table = null;
    }

    public void correctTreeTableStructure() {
        if (table() instanceof TreeTable) {
            new TreeTableStructure((TreeTable) table()).run();
        }
    }

    GeneratorAction checkAlter() {
        GeneratorAction result = GeneratorAction.None;

        dbFields.clear();

        for (Column c : dbTable.getColumns()) {
            dbFields.put(c.name, new ColumnDescGen(c));
        }

        dbFieldsAlter = new LinkedList<ColumnDescAlter>();

        for (Field field : table().getTableFields()) {
            ColumnDescGen columndesc = dbFields.get(field.name());

            if (null == columndesc) {
                dbFieldsAlter.add(new ColumnDescAlter(field, FieldAction.Create, true));
                result = GeneratorAction.Alter;
                continue;
            }

            columndesc.DescExist = true;

            String sqlType = field.sqlType(connection.vendor());

            if (!sqlType.startsWith(columndesc.type)) {
                result = GeneratorAction.Recreate;
                break;
            }

            ColumnDescAlter fieldDesc = null;

            if (field.type() == FieldType.Decimal || field.type() == FieldType.String) {
                int size = field.size();
                int scale = field.scale();

                if (size != columndesc.size || scale != columndesc.scale) {
                    if (size >= columndesc.size && scale >= columndesc.scale) {
                        fieldDesc = new ColumnDescAlter(field, FieldAction.Alter, columndesc.nullable);
                        result = GeneratorAction.Alter;
                    } else {
                        result = GeneratorAction.Recreate;
                        break;
                    }
                }
            }

            String defaultValue = DefaultValue.get(vendor(), field);

            if (vendor() == DatabaseVendor.SqlServer) {
                defaultValue = "(" + defaultValue + ")";

                if (field.type() == FieldType.Boolean || field.type() == FieldType.Integer) {
                    defaultValue = "(" + defaultValue + ")";
                }
            }

            if (!columndesc.defaultValue.trim().equalsIgnoreCase(defaultValue) || field.type() == FieldType.String) {
                if (fieldDesc == null) {
                    fieldDesc = new ColumnDescAlter(field, FieldAction.Default, columndesc.nullable);
                } else {
                    fieldDesc.action = FieldAction.AlterDefault;
                }

                result = GeneratorAction.Alter;
            }

            if (fieldDesc != null)
                dbFieldsAlter.add(fieldDesc);
        }

        if (GeneratorAction.Recreate != result) {
            for (ColumnDescGen field : dbFields.values()) {
                if (!field.DescExist) {
                    result = GeneratorAction.Recreate;
                    break;
                }
            }
        }

        return result;
    }

    static void renameTable(Connection connection, String oldTableName, String newTableName) throws SQLException {
        String sql = null;

        Database database = connection.database();
        DatabaseVendor vendor = connection.vendor();
        switch (vendor) {
        case Oracle:
            sql = "rename " + database.tableName(oldTableName) + " to " + vendor.quote(newTableName);
            break;
        case SqlServer:
            sql = "sp_rename " + database.tableName(oldTableName) + ", " + vendor.quote(newTableName);
            break;
        case Postgres:
            sql = "alter table " + database.tableName(oldTableName) + " rename to " + vendor.quote(newTableName);
        default:
            assert (false);
        }

        Statement.executeUpdate(connection, sql);
    }

    static void dropAllKeys(Connection connection, TableDescription tableindb, ILogger showMessage) throws SQLException {
        dropFKs(connection, tableindb.getRelations(), showMessage);
        dropIdxs(connection, tableindb, showMessage);
    }

    static void dropReferencesKeys(Connection connection, TableDescription tableindb, ILogger showMessage)
            throws SQLException {
        dropFKs(connection, tableindb.getRelationsFromPK(), showMessage);
    }

    private String getFieldForCreate(Field field) {
        DatabaseVendor vendor = vendor();
        String result = vendor.quote(field.name()) + ' ' + field.sqlType(vendor);

        if (vendor == DatabaseVendor.SqlServer || vendor == DatabaseVendor.Oracle || vendor == DatabaseVendor.Postgres) {
            result += " default " + DefaultValue.get(vendor, field);

            if (field.isPrimaryKey() || field instanceof GuidField) {
                result += " not null";
            }
        }

        return result;
    }

    private String getFieldForAlter(Field field) {
        DatabaseVendor vendor = connection.vendor();
        return vendor.quote(field.name()) + " " + field.sqlType(vendor);
    }

    private void alterTable() throws SQLException {
        boolean bAdd = false, bModify = false;

        Database database = database();
        DatabaseVendor vendor = vendor();
        
        String addColumnSql = "alter table " + database.tableName(table().name());
        String modifyColumnSql = "alter table " + database.tableName(table().name());

        switch (vendor) {
        case Postgres:
        case Oracle:
            addColumnSql += " add(";
            modifyColumnSql += " modify(";
            break;
        case SqlServer:
            addColumnSql += " add ";
            modifyColumnSql += " alter column ";
            break;
        }

        for (ColumnDescAlter fld : dbFieldsAlter) {
            if (fld.action == FieldAction.Create) {
                if (bAdd)
                    addColumnSql += ", ";
                addColumnSql += getFieldForCreate(fld.field);
                bAdd = true;
            } else if (fld.action == FieldAction.Default) {
                if (vendor == DatabaseVendor.Oracle || vendor == DatabaseVendor.Postgres) {
                    if (bModify)
                        modifyColumnSql += ", ";

                    String defaultValue = DefaultValue.get(connection.vendor(), fld.field);
                    modifyColumnSql += vendor.quote(fld.field.name()) + " default " + defaultValue;
                    bModify = true;
                } else if (vendor == DatabaseVendor.SqlServer) {
                    changeMsSQLDefault(fld);
                }
            } else if (fld.action == FieldAction.AlterDefault) {
                if (vendor == DatabaseVendor.Oracle || vendor == DatabaseVendor.Postgres) {
                    if (bModify)
                        modifyColumnSql += ", ";
                    modifyColumnSql += getFieldForCreate(fld.field);
                    bModify = true;
                } else if (vendor == DatabaseVendor.SqlServer) {
                    changeMsSQLDefault(fld);
                }
            } else if (fld.action == FieldAction.Alter) {
                if (bModify) {
                    if (vendor == DatabaseVendor.Oracle || vendor == DatabaseVendor.Postgres) {
                        modifyColumnSql += ", ";
                    } else if (vendor == DatabaseVendor.SqlServer) {
                        modifyColumnSql += " alter table " + database.tableName(table().name()) + " alter column ";
                    }
                }
                modifyColumnSql += getFieldForAlter(fld.field);
                bModify = true;
            }
        }

        switch (vendor) {
        case Postgres:
        case Oracle:
            addColumnSql += ")";
            modifyColumnSql += ")";
            break;
        case SqlServer:
            break;
        }

        if (bAdd) {
            Statement.executeUpdate(connection, addColumnSql);
        }

        if (bModify) {
            Statement.executeUpdate(connection, modifyColumnSql);
        }
    }

    private void changeMsSQLDefault(ColumnDescAlter fld) throws SQLException {
        String defaultValue = getDefault(table().name(), fld.field.name());

        if ((defaultValue != null) && (defaultValue.length() > 0)) {
            String sql = "alter table " + database().tableName(table().name()) + " drop constraint "
                    + vendor().quote(defaultValue);
            Statement.executeUpdate(connection, sql);
        }

        defaultValue = DefaultValue.get(vendor(), fld.field);

        String sql = "alter table " + database().tableName(table().name()) + " add constraint "
                + vendor().quote("DF_" + table().name() + fld.field.name()) + " default " + defaultValue + " for "
                + vendor().quote(fld.field.name());
        Statement.executeUpdate(connection, sql);
    }

    private void recreateTable() throws SQLException {
        String name = table().name() + "_" + guid.create().toString();

        try {
            createTable(name);
            moveData(name);
            dropReferencesKeys(connection, dbTable, logger);
            dropTable(connection, table().name());
            dbTable.onRecreateTable();
        } catch (SQLException e) {
            dropTable(connection, name);
            throw e;
        }

        renameTable(connection, name, table().name());
    }

    private void createTable(String name) throws SQLException {
        String sql = "create table " + database().tableName(name) + " (";

        List<Field> fields = table().getTableFields();

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            sql += (i != 0 ? ", " : "") + getFieldForCreate(field);
        }

        sql += ")";

        Statement.executeUpdate(connection, sql);
    }

    public void createPrimaryKey() {
        try {
            new PrimaryKeyGenerator(table()).run(connection);
        } catch (ObjectAlreadyExistException e) {} catch (SQLException e) {
            logger.error(
                    e,
                    Resources.format(
                            "Generator.createUniqueIndexError",
                            displayName(), "[" + connection.schema() + "]." + name(),
                                    ErrorUtils.getMessage(e)));
        } finally {
            this.table = null;
        }
    }

    private void packFK() {
        int c = 0;
        if (table().getForeignKeys() != null) {
            for (IForeignKey fkGen : table().getForeignKeys()) {
                while (true) {
                    try {
                        createFK(fkGen, c);
                        break;
                    } catch (ObjectAlreadyExistException e) {
                        c++;
                    }
                }
                c++;
            }
        }
    }

    private void createFK(IForeignKey fk, int c) {
        try {
            new ForeignKeyGenerator(new ForeignKey(name(), fk, c)).run(connection);
        } catch (SQLException e) {
            logger.error(
                    e,
                    Resources.format("Generator.createForeignKeyError", table().displayName(),
                            table().name(), fk.getReferencedTable().displayName(), fk.getReferencedTable().name(),
                            ErrorUtils.getMessage(e)));
        }
    }

    private void packIndex() {
        int c = 0;

        for (IField field : table().getIndices()) {
            while (true) {
                try {
                    createIndex(field, c);
                    break;
                } catch (ObjectAlreadyExistException e) {
                    c++;
                }
            }
            c++;
        }
    }

    private void createIndex(IField field, int id) {
        try {
            new IndexGenerator(table(), (Field) field, id, false).run(connection);
        } catch (SQLException e) {
            logger.error(
                    e,
                    Resources.format("Generator.createIndexError", field.displayName(),
                            table().displayName(), table().name(), ErrorUtils.getMessage(e)));
        }
    }

    private void packUnique() {
        int c = 0;
        for (IField field : table().getUniqueIndices()) {
            while (true) {
                try {
                    createUnique(field, c);
                    break;
                } catch (ObjectAlreadyExistException e) {
                    c++;
                }
            }
            c++;
        }
    }

    private void createUnique(IField field, int id) {
        try {
            new IndexGenerator(table(), (Field) field, id, true).run(connection);
        } catch (SQLException e) {
            logger.error(
                    e,
                    Resources.format("Generator.createUniqueIndexError", field.displayName(),
                            table().displayName(), table().name()));
        }
    }

    private void createNullRecord() {
        Query query = table();

        Collection<Field> fields = query.getDataFields();

        try {
            if (!findRecord(guid.NULL)) {
                Insert insert = new Insert(query, fields);
                insert.execute();
            } else {
                for (Field field : fields) {
                    field.set(null);
                }

                Update update = new Update(query, fields, guid.NULL);
                update.execute();

                for (Field field : fields) {
                    field.reset();
                }
            }
        } catch (SQLException e) {
            logger.error(
                    e,
                    formatTableName(table())
                            + ": "
                            + Resources.format("Generator.insertRecordsError", guid.NULL.toString(),
                                    table().displayName(), table().name(), ErrorUtils.getMessage(e)));
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void createStaticRecord(Map<IField, primary> record) {
        Field primaryKey = table().primaryKey();
        Field parentKey = table().parentKey();

        guid recordId = (guid) record.get(primaryKey);
        guid parentId = (guid) record.get(parentKey);

        if (parentKey != null && parentId == null) {
            parentId = guid.NULL;
        }

        Set<Field> fields = (Set) record.keySet();

        Query table = table();

        try {
            if (!findRecord(recordId)) {
                for (Field field : fields) {
                    primary value = record.get(field);
                    field.set(value);
                }
                table.create(recordId, parentId, guid.NULL);
            } else {
                Iterator<Field> i = fields.iterator();
                while (i.hasNext()) {
                    Field field = i.next();
                    if (field.gendb_updatable()) {
                        primary value = record.get(field);
                        field.set(value);
                    } else {
                        i.remove();
                    }
                }
                if (!(table instanceof Users) || !BuiltinUsers.System.guid().equals(recordId) && !BuiltinUsers.Administrator.guid().equals(recordId)) {
                    Update update = new Update(table, fields, recordId);
                    update.execute();
                }
            }

            for (Field field : fields) {
                field.reset();
            }
        } catch (SQLException e) {
            logger.error(
                    e,
                    formatTableName(table())
                            + ": "
                            + Resources.format("Generator.insertRecordsError", recordId.toString(),
                                    table().displayName(), table().name(), ErrorUtils.getMessage(e)));
        }

    }

    private boolean findRecord(guid recordId) throws SQLException {
        CountingSelect select = new CountingSelect();

        Query query = table();
        Field primaryKey = query.primaryKey();
        sql_bool where = new sql_bool(new Rel(primaryKey, Operation.Eq, new sql_guid(recordId)));

        select.setWhere(where);
        select.setRootQuery(query);

        return select.count() != 0;
    }

    private void moveData(String dstTableName) throws SQLException {
        String targetFields = "";
        String sourceFields = "";

        Database database = database();
        DatabaseVendor vendor = vendor();

        for (Field field : table().getTableFields()) {
            String name = field.name();

            ColumnDescGen dbField = dbFields.get(name);
            
            if (dbField != null) {
                targetFields += (targetFields.isEmpty() ? "" : ", ") + vendor.quote(name);

                if (field.type() == FieldType.Guid) {
                    SqlToken isNull = new IsNull(new SqlField(field));
                    SqlToken iif = new If(isNull, guid.NULL.sql_guid(), new SqlField(field));
                    name = iif.format(vendor, new FormatOptions());
                } else if(dbField.type.startsWith("character") && field.type() == FieldType.Text) {
                    name = new ToBytes(field).format(vendor, new FormatOptions());
                } else if(dbField.type.startsWith("bytea") && field.type() == FieldType.String) {
                    name = new ToChar(field).format(vendor, new FormatOptions());
                } else {
                    name = vendor.quote(name);
                }
                sourceFields += (sourceFields.isEmpty() ? "" : ", ") + name;
            }
        }

        if (!targetFields.isEmpty()) {
            String sql = "insert into " + database.tableName(dstTableName) + " (" + targetFields + ")";
            sql += " select " + sourceFields + " from " + database.tableName(table().name())
                    + (vendor == DatabaseVendor.SqlServer ? " as " : "") + table().getAlias();

            Statement.executeUpdate(connection, sql);
        }
    }

    private String formatTableName(ITable table) {
        return new MessageFormat("\"{0}\" ({1})").format(new Object[] { table.displayName(), table.name() });
    }

    static void dropFKs(Connection connection, Collection<ForeignKey> fks, ILogger logger) {
        for(ForeignKey fk : fks) {
            try {
                fk.drop(connection);
            } catch (SQLException e) {
                logger.error(e, Resources.format("Generator.dropForeignKeyError", fk.table, fk.name, ErrorUtils.getMessage(e)));
            }
        }
    }

    static void dropIdxs(Connection connection, TableDescription _tableindb, ILogger showMessage) {
        Collection<Index> del_idxs = new LinkedList<Index>();
        Collection<Index> Idxs = _tableindb.getIndexes();
        for (Index idx : Idxs)
            try {
                IndexGenerator.dropIndex(connection, idx.tableName, idx.name, false);
                del_idxs.add(idx);
            } catch (SQLException e) {
                showMessage.error(
                        e,
                        Resources.format("Generator.dropIndexError",
                                idx.tableName, idx.name, ErrorUtils.getMessage(e)));
            }
        Idxs.removeAll(del_idxs);

        Collection<Index> del_uidxs = new LinkedList<Index>();
        Idxs = _tableindb.getUniqueIndexes();
        for (Index idx : Idxs) {
            try {
                IndexGenerator.dropIndex(connection, idx.tableName, idx.name, true);
                del_uidxs.add(idx);
            } catch (SQLException e) {
                showMessage.error(
                        e,
                        Resources.format("Generator.dropIndexError",
                                new Object[] { idx.tableName, idx.name, ErrorUtils.getMessage(e) }));
            }
        }

        Idxs.removeAll(del_uidxs);
    }

    static void dropTable(Connection connection, String tableName) throws SQLException {
        Database database = connection.database();

        String sql = "drop table " + database.tableName(tableName);
        Statement.executeUpdate(connection, sql);
    }

    public String getDefault(String tableName, String fieldName) throws SQLException {
        String sql = "select so.name from sysobjects so, sysobjects so2, syscolumns sc where sc.name = '" + fieldName
                + "' and sc.id = so2.id and so2.name = " + database().tableName(tableName)
                + " and so.id = sc.cdefault and so.xtype = 'D'";

        Cursor cursor = BasicSelect.cursor(connection, sql);

        String result = cursor.next() ? cursor.getString(1).get() : "";
        cursor.close();
        return result;
    }
}
