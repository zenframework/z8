package org.zenframework.z8.server.ie;

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.types.guid;

public class Import {

    private static final Log LOG = LogFactory.getLog(Import.class);

    private static JsonObject STRUCTURE = null;

    public static void importRecords(Message message) {

        // Сортировка записей
        if (RecordsSorter.getSortingMode().onImport) {
            RecordsSorter sorter = new RecordsSorter();
            for (ExportEntry.Records.Record record : message.getExportEntry().getRecords().getRecord()) {
                guid recordId = new guid(record.getRecordId());
                sorter.addRecord(record.getTable(), recordId);
                for (ExportEntry.Records.Record.Field field : record.getField()) {
                    JsonObject tableInfo = getStructure().getJsonObject(record.getTable());
                    if (tableInfo == null)
                        throw new RuntimeException("No structure for '" + record.getTable() + "'");
                    if (tableInfo.has(field.getId())) {
                        String linkTableName = tableInfo.getString(field.getId());
                        Table linkTable = (Table) Loader.getInstance(record.getTable());
                        guid linkId = new guid(field.getValue());
                        if (!IeUtil.isBuiltinRecord(linkTable, linkId)) {
                            sorter.addLink(record.getTable(), recordId, linkTableName, linkId);
                        }
                    }
                }
            }
            Collections.sort(message.getExportEntry().getRecords().getRecord(), sorter.getComparator());
        }

        // Импорт записей
        for (ExportEntry.Records.Record record : message.getExportEntry().getRecords().getRecord()) {
            Table table = (Table) Loader.getInstance(record.getTable());
            guid recordId = new guid(record.getRecordId());
            if (!IeUtil.isBuiltinRecord(table, recordId)) {
                if (table.hasRecord(recordId)) {
                    // Если запись уже существует
                    ImportPolicy policy = ImportPolicy.getPolicy(record.getPolicy());
                    if (policy.isOverride()) {
                        // Если запись должна быть обновлена согласно политике,
                        // обновить
                        LOG.debug("Import: update record " + IeUtil.toString(record));
                        IeUtil.fillTableRecord(table, record);
                        table.update(recordId);
                    } else {
                        // Если запись не должна быть обновлена, ничего не
                        // делать
                        LOG.debug("Import: skip record " + IeUtil.toString(record));
                    }
                } else {
                    // Если запись не существует, создать
                    LOG.debug("Import: create record " + IeUtil.toString(record));
                    IeUtil.fillTableRecord(table, record);
                    table.create(recordId);
                }
            }
        }

    }

    public static void importFiles(Message message, Files filesTable) {
        for (FileInfo file : message.getFiles()) {
            boolean idIsNull = file.id == null || file.id.isNull();
            if (idIsNull || !filesTable.hasRecord(file.id)) {
                if (!idIsNull) {
                    filesTable.recordId.get().set(file.id);
                }
                filesTable.name.get().set(file.name);
                filesTable.file.get().set(file.getInputStream());
                filesTable.path.get().set(file.path);
                file.id = filesTable.create();
            }
        }
    }

    private static JsonObject getStructure() {
        if (STRUCTURE == null) {
            STRUCTURE = getTablesStructure();
        }
        return STRUCTURE;
    }
    
    public static JsonObject getTablesStructure() {
        JsonObject structure = new JsonObject();
        for (Table.CLASS<? extends Table> tableClass : Runtime.instance().tables()) {
            fillStructure(structure, tableClass);
        }
        return structure;
    }

    @SuppressWarnings("unchecked")
    private static void fillStructure(JsonObject structure, Table.CLASS<? extends Table> tableClass) {
        if (!structure.has(tableClass.classId())) {
            JsonObject jsonTable = new JsonObject();
            structure.put(tableClass.classId(), jsonTable);
            Table table = tableClass.newInstance();
            for (IForeignKey fkey : table.getForeignKeys()) {
                String fieldIndex = table.getFieldByName(fkey.getFieldDescriptor().name()).getIndex();
                jsonTable.put(fieldIndex, ((Table) fkey.getReferencedTable()).classId());
                fillStructure(structure,
                        (Table.CLASS<? extends Table>) Loader.loadClass(((Table) fkey.getReferencedTable()).classId()));
            }
        }
    }
    
}
