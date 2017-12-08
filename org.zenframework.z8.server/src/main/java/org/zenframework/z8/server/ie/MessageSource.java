package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;

public class MessageSource implements RmiSerializable, Serializable {
    private static final long serialVersionUID = -145929531248527279L;

    private ExportRules exportRules = new ExportRules();
    private Collection<ExportSource> sources = new ArrayList<ExportSource>();
    private Map<String, primary> properties = new HashMap<String, primary>();

    private Collection<RecordInfo> inserts = new ArrayList<RecordInfo>();
    private Collection<RecordInfo> updates = new ArrayList<RecordInfo>();

    private Collection<file> files = new ArrayList<file>();

    public ExportRules exportRules() {
        return exportRules;
    }

    public void add(Table table, Collection<Field> fields, sql_bool where) {
        if(table.exportable())
            sources.add(new ExportSource(table, fields, where));
    }

    public void addRule(ImportPolicy importPolicy) {
        exportRules.add(importPolicy);
    }

    public void addRule(Table table, ImportPolicy policy) {
        exportRules.add(table.name(), policy);
    }

    public void addRule(Table table, guid recordId, ImportPolicy policy) {
        exportRules.add(table.name(), recordId, policy);
    }

    public void addRule(Table table, Field field, ImportPolicy policy) {
        exportRules.add(table.name(), field.name(), policy);
    }

    public void addRule(Table table, guid recordId, Field field, ImportPolicy policy) {
        exportRules.add(table.name(), recordId, field.name(), policy);
    }

    public Map<String, primary> getProperties() {
        return properties;
    }

    public primary getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, primary value) {
        properties.put(key, value);
    }

    public Collection<file> files() {
        return files;
    }

    public void exportData() {
        for(ExportSource source : sources)
            processExportSource(source);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        serialize(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        deserialize(in);
    }

    @Override
    public void serialize(ObjectOutputStream out) throws IOException {
        RmiIO.writeLong(out, serialVersionUID);

        out.writeObject(exportRules);
        out.writeObject(sources);
        out.writeObject(properties);

        out.writeObject(inserts);
        out.writeObject(updates);

        out.writeObject(files);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
        @SuppressWarnings("unused")
        long version = RmiIO.readLong(in);

        exportRules = (ExportRules)in.readObject();
        sources = (Collection<ExportSource>)in.readObject();
        properties = (Map<String, primary>)in.readObject();

        inserts = (Collection<RecordInfo>)in.readObject();
        updates = (Collection<RecordInfo>)in.readObject();

        files = (Collection<file>)in.readObject();
    }

    private Map<String, Boolean> recordStates = new HashMap<String, Boolean>();

    private void processExportSource(ExportSource source) {
        Table table = source.table();
        String tableName = source.name();

        Collection<Field> fields = source.fields();

        table.saveState();

        Collection<guid> records = source.records();
        SqlToken where = records != null ? new InVector(table.primaryKey(), records) : null;
        table.setWhere(where); // to replace existing where
        table.read(fields);

        while(table.next()) {
            guid recordId = table.recordId();

            String id = makeUniqueId(tableName, recordId);

            if(recordStates.get(id) != null)
                continue;

            recordStates.put(id, false);

            RecordInfo record = new RecordInfo(recordId, tableName);

            for(Field field : fields) {
                if(!processLink(field, recordId))
                    continue;

                processAttachments(field);

                record.add(new FieldInfo(field));
            }

            inserts.add(record);

            recordStates.put(id, true);
        }

        table.restoreState();
    }

    private void processAttachments(Field field) {
        boolean isAttachment = field instanceof AttachmentField;

        if(!isAttachment)
            return;

        for(file f : file.parse(field.string().get())) {
            if(!files.contains(f))
                files.add(f);
        }
    }

    private boolean processLink(Field field, guid recordId) {
        boolean isLink = field instanceof Link;
        boolean isParentKey = field.isParentKey();

        if(!isLink && !isParentKey)
            return true;

        Table table = (Table)(isParentKey ? field.getOwner() : ((Link)field).getQuery());

        String name = table.name();
        guid value = field.guid();

        if(value.equals(guid.NULL))
        	return true;
        
        if(BuiltinUsers.System.guid().equals(value) || BuiltinUsers.Administrator.guid().equals(value))
            return false;

        Boolean state = recordStates.get(makeUniqueId(name, value));
        boolean notInserted = state == null;
        boolean inserting = state != null && !state;

        if(notInserted) {
            ExportSource source = new ExportSource(table, null, Arrays.asList(value));
            processExportSource(source);
            return true;
        } else if(inserting) {
            processUpdate(recordId, field);
            return false;
        }

        // inserted
        return true;
    }

    private void processUpdate(guid recordId, Field field) {
        RecordInfo record = new RecordInfo(recordId, field.getOwner().name());
        record.add(new FieldInfo(field));
        updates.add(record);
    }

    private String makeUniqueId(String name, guid id) {
        return name + "/" + id;
    }

    public void importData(MergeHandler mergeHandler) {
        for(RecordInfo record : inserts)
            insert(record);

        for(RecordInfo record : updates)
            update(record, mergeHandler);

        fieldCache.clear();
        tableCache.clear();
    }

    private Map<String, Table> tableCache = new HashMap<String, Table>();
    private Map<String, Field> fieldCache = new HashMap<String, Field>();
    private Set<guid> insertedRecords = new HashSet<guid>();

    private Table getTable(String name) {
        Table table = tableCache.get(name);

        if(table == null) {
            table = (Table)Runtime.instance().getTableByName(name).newInstance();
            tableCache.put(name, table);
        }

        return table;
    }

    private Field getField(String tableName, String fieldName) {
        String key = tableName + "." + fieldName;

        Field field = fieldCache.get(key);

        if(field == null) {
            field = getTable(tableName).getFieldByName(fieldName);
            fieldCache.put(key, field);
        }

        return field;
    }

    private void insert(RecordInfo record) {
        String tableName = record.table();
        Table table = getTable(tableName);
        guid recordId = record.id();

        Collection<Field> attachments = table.getAttachments();
        boolean exists = table.readRecord(recordId, attachments);

        for(FieldInfo fieldInfo : record.fields()) {
            String fieldName = fieldInfo.name();
            Field field = getField(tableName, fieldName);

            if(field == null)
                throw new RuntimeException("Incorrect record format. Table '" + tableName + "' has no field '" + fieldName + "'");

            ImportPolicy fieldPolicy = exportRules.getPolicy(tableName, fieldName, recordId);

            if(!exists || fieldPolicy == ImportPolicy.OVERRIDE) {
                primary value = fieldInfo.value();

                if(exists && field instanceof AttachmentField)
                    value = mergeAttachments(field.string(), (string)value);

                field.set(value);
            }
        }

        if(!exists) {
            table.create(recordId);
            insertedRecords.add(recordId);
        } else
            table.update(recordId);
    }

    private void update(RecordInfo record, MergeHandler mergeHandler) {
        String tableName = record.table();
        Table target = getTable(tableName);
        guid recordId = record.id();

        boolean useMerge = false;
        for (FieldInfo fieldInfo : record.fields()) {
            if (exportRules.getPolicy(tableName, fieldInfo.name(), recordId) == ImportPolicy.MERGE) {
                useMerge = true;
                break;
            }
        }

        if (useMerge) {

            Table source = (Table) Runtime.instance().getTableByName(tableName).newInstance();
            for (FieldInfo fieldInfo : record.fields())
                source.getFieldByName(fieldInfo.name()).set(fieldInfo.value());
            mergeHandler.merge(source, target, tableName);

        } else {
            for(FieldInfo fieldInfo : record.fields()) {
                String fieldName = fieldInfo.name();

                if(!insertedRecords.contains(recordId)) {
                    ImportPolicy fieldPolicy = exportRules.getPolicy(tableName, fieldName, recordId);
                    if(fieldPolicy != ImportPolicy.OVERRIDE)
                        continue;
                }

                Field field = getField(tableName, fieldName);

                if(field == null)
                    throw new RuntimeException("Incorrect record format. Table '" + tableName + "' has no field '" + fieldName + "'");

                field.set(fieldInfo.value());
            }
        }

        target.update(record.id());
    }

    static private string mergeAttachments(string value1, string value2) {
        if(value1.equals(value2) || value2.isEmpty())
            return value1;

        if(value1.isEmpty())
            return value2;

        Collection<file> files = file.parse(value1.get());

        for(file f : file.parse(value2.get())) {
            if(!files.contains(f))
                files.add(f);
        }

        return new string(new JsonArray(files).toString());
    }
}
