package org.zenframework.z8.server.base.model.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.DbUtil;
import org.zenframework.z8.server.db.Statement;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.primary;

public class Insert extends Statement {
    private Query query;
    private Collection<Field> fields;

    public Insert(Query query, Collection<Field> fields) {
        super(ConnectionManager.get());
        
        this.query = query;

        this.fields = new ArrayList<Field>();

        for(Field field : fields) {
            if(!(field instanceof Expression)) {
                this.fields.add(field);
            }
        }
        
        sql = buildSql();
    }

    private String buildSql() {
        Database database = database();
        DatabaseVendor vendor = vendor();

        String insertFields = "";
        String insertValues = "";

        for(Field field : fields) {
            insertFields += (insertFields.isEmpty() ? "" : ", ") + vendor.quote(field.name());
            insertValues += (insertValues.isEmpty() ? "" : ", ") + "?";
        }

        return "insert into " + database.tableName(query.getRootQuery().name()) + " " + "(" + insertFields + ") values ("
                + insertValues + ")";
    }
    
    public void execute() throws SQLException {
        try {
            prepare(sql);
            executeUpdate();
        }
        catch(Throwable e) {
            System.out.println(sql());

            for(Field field : fields) {
                System.out.println(field.name() + ": " + field.get());
            }

            Trace.logError(e);

            throw new RuntimeException(e);
        }
        finally {
            close();
        }
    }

    @Override
    public void prepare(String sql) throws SQLException {
        
        super.prepare(sql);

        int position = 1;

        for(Field field : fields) {
            primary value = field.get();
            DbUtil.addParameter(this, position, field.type(), value);
            position++;
        }
    }
}
