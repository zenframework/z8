package org.zenframework.z8.server.db.generator.fk;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.job.JobMonitor;
import org.zenframework.z8.server.base.model.sql.Update;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.utils.ArrayUtils;

public class RecordsMergeJob extends Procedure {
    public static class CLASS<T extends RecordsMergeJob> extends Procedure.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(RecordsMergeJob.class);
            setDisplayName("RecordsMergeJob");
        }

        @Override
        public Object newObject(IObject container) {
            return new RecordsMergeJob(container);
        }
    }

    private String tableName;
    private guid recordId;
    private List<guid> records = new ArrayList<guid>();

    public RecordsMergeJob(IObject container) {
        super(container);
    }

    @Override
    protected void z8_exec() {
        parseParameters();

        if(records.size() == 0) {
            JobMonitor monitor = getMonitor();
            monitor.setTotalWork(100);
            monitor.setWorked(100);
            return;
        }

        try {
            updateTables();
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseParameters() {
        IRequest request = ApplicationServer.getRequest();
        tableName = request.getParameter(Json.table);
        recordId = new guid(request.getParameter(Json.recordId));

        JsonArray records = new JsonArray(request.getParameter(Json.records));

        for(int index = 0; index < records.length(); index++) {
            guid id = new guid(records.getString(index));

            if(!id.equals(recordId)) {
                this.records.add(new guid(records.getString(index)));
            }
        }
    }

    private void updateTables() throws SQLException {
        Collection<ForeignKey> keys = org.zenframework.z8.server.db.generator.fk.ForeignKey.get(ConnectionManager.get(), tableName);

        JobMonitor monitor = getMonitor();

        if(keys.size() == 0) {
            monitor.setTotalWork(100);
            monitor.setWorked(100);
            return;
        }

        monitor.setTotalWork(keys.size());

        int index = 0;

        for(ForeignKey key : keys) {
            Table table = key.getTable();
            Field link = key.getLink();

            link.set(recordId);

            Collection<Field> fields = new ArrayList<Field>();
            fields.add(link);

            sql_bool where = new sql_bool(new InVector(new SqlStringToken('[' + link.name() + ']'), records));

            Update update = new Update(table, ArrayUtils.collection(link), where);
            update.execute();

            index++;

            monitor.print(table.displayName() + " [" + table.name() + "]");
            monitor.setWorked(index);

        }

        monitor.print(Resources.format("RecordsMergerJob.tablesChanged", keys.size()));
    }
}
