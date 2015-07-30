package org.zenframework.z8.server.ie;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Export extends OBJECT {

    private static final String NULL_PROTOCOL = "null";
    
    public static class CLASS<T extends Export> extends OBJECT.CLASS<T> {

        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(Export.class);
            setName(Export.class.getName());
            setDisplayName(Export.class.getName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Export(container);
        }

    }

    public final RLinkedHashMap<string, primary> properties = new RLinkedHashMap<string, primary>();

    protected final TransportContext.CLASS<TransportContext> context = new TransportContext.CLASS<TransportContext>();

    private final Map<String, ImportPolicy> policies = new HashMap<String, ImportPolicy>();
    private final List<Table> recordsets = new LinkedList<Table>();
    private boolean exportAttachments = false;
    private URI transportUrl;

    public Export(IObject container) {
        super(container);
    }

    @Override
    public void constructor2() {
        super.constructor2();
        z8_init();
    }

    public void addRecordset(Table table) {
        table.read(table.getPrimaryFields());
        recordsets.add(table);
    }

    public void addRecordset(Table table, sql_bool where) {
        table.read(table.getPrimaryFields(), where);
        recordsets.add(table);
    }

    public void setPolicy(Table table, ImportPolicy policy) {
        policies.put(table.classId(), policy);
    }

    public void setPolicy(String tableClass, ImportPolicy policy) {
        policies.put(tableClass, policy);
    }

    public ImportPolicy getPolicy(Table table) {
        return getPolicy(table.classId());
    }

    public ImportPolicy getPolicy(String tableClass) {
        return policies.containsKey(tableClass) ? policies.get(tableClass) : ImportPolicy.DEFAULT;
    }

    public boolean isExportAttachments() {
        return exportAttachments;
    }

    public void setExportAttachments(boolean exportAttachments) {
        this.exportAttachments = exportAttachments;
    }

    public String getTransportUrl() {
        return transportUrl.toString();
    }

    public void setTransportUrl(String transportUrl) {
        try {
            this.transportUrl = new URI(transportUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Incorrect transport URL [" + transportUrl + "]", e);
        }
    }

    public void execute() {
        RecordsSorter recordsSorter = new RecordsSorter();
        Message message = new Message();
        try {
            String protocol = transportUrl.getScheme();
            if (!protocol.equals(NULL_PROTOCOL)) {
                // Если протокол НЕ "null", экспортировать записи БД
                for (Table recordset : recordsets) {
                    while (recordset.next()) {
                        exportRecord(recordset, message, getPolicy(recordset.classId()), recordsSorter, 0);
                    }
                }
                // Сортировка записей в соответствии со ссылками по foreign keys и parentId
                Collections.sort(message.getExportEntry().getRecords().getRecord(), recordsSorter.getComparator());
                Trace.logEvent("Sorted records:");
                for (ExportEntry.Records.Record record : message.getExportEntry().getRecords().getRecord()) {
                    Trace.logEvent(record.getTable() + '[' + record.getRecordId() + ']');
                }
            }
            // Свойства экспортируются для любого протокола
            if (!properties.isEmpty()) {
                ExportEntry.Properties properties = new ExportEntry.Properties();
                for (Map.Entry<string, primary> entry : this.properties.entrySet()) {
                    ExportEntry.Properties.Property property = new ExportEntry.Properties.Property();
                    property.setKey(entry.getKey().get());
                    property.setValue(entry.getValue().toString());
                    property.setType(entry.getValue().type().toString());
                    properties.getProperty().add(property);
                }
                message.getExportEntry().setProperties(properties);
            }
            // Запись сообщения в таблицу ExportMessages
            message.setAddress(transportUrl.getHost());
            ExportMessages.instance().addMessage(message, transportUrl.getScheme(),
                    context.get().getProperty(TransportContext.SelfAddressProperty));
        } catch (Exception e) {
            throw new exception("Can't marshal records", e);
        }
    }

    public static URI getTransportUrl(String protocol, String address) throws URISyntaxException {
        return new URI(protocol + "://" + address);
    }

    public void z8_addRecordset(Table.CLASS<? extends Table> cls) {
        addRecordset(cls.get());
    }

    public void z8_addRecordset(Table.CLASS<? extends Table> cls, sql_bool where) {
        addRecordset(cls.get(), where);
    }

    public void z8_setPolicy(Table.CLASS<? extends Table> cls, ImportPolicy policy) {
        setPolicy(cls.get(), policy);
    }

    public void z8_setExportAttachments(bool exportAttachments) {
        setExportAttachments(exportAttachments.get());
    }

    public void z8_setTransportUrl(string transportUrl) {
        setTransportUrl(transportUrl.get());
    }

    public void z8_init() {}
    
    public void z8_execute() {
        execute();
    }

    public static string z8_getTransportUrl(string protocol, string address) {
        try {
            return new string(getTransportUrl(protocol.get(), address.get()).toString());
        } catch (URISyntaxException e) {
            throw new exception(e.getMessage(), e);
        }
    }

    private void exportRecord(Table recordSet, Message message, ImportPolicy policy, RecordsSorter recordsSorter, int level)
            throws DatatypeConfigurationException {
        guid recordId = recordSet.recordId();
        String table = recordSet.classId();
        if (!recordsSorter.contains(table, recordId) && !IeUtil.isBuiltinRecord(recordId)) {
            if (policies.containsKey(recordSet.classId())) {
                policy = policies.get(recordSet.classId());
            }
            Trace.logEvent(getSpaces(level) + "Export record " + recordSet.name() + '[' + recordId + ']');
            ExportEntry.Records.Record record = IeUtil.tableToRecord(recordSet,
                    policy == null ? null : policy.getSelfPolicy(), isExportAttachments());
            message.getExportEntry().getRecords().getRecord().add(record);
            recordsSorter.addRecord(table, recordId);
            // Вложения
            if (isExportAttachments()) {
                for (AttachmentField attField : recordSet.getAttachments()) {
                    Collection<FileInfo> fileInfos = FileInfo.parseArray(attField.get().string().get());
                    message.getExportEntry().getFiles().getFile()
                            .addAll(IeUtil.fileInfosToFiles(fileInfos, policy == null ? null : policy.getSelfPolicy()));
                }
            }
            // Ссылки на другие таблицы
            // TODO Переделать через links
            for (IForeignKey fkey : recordSet.getForeignKeys()) {
                if (fkey.getReferencedTable() instanceof Table) {
                    Trace.logEvent(getSpaces(level + 1) + fkey.getFieldDescriptor().name() + " --> "
                            + fkey.getReferencedTable().name() + '['
                            + recordSet.getFieldByName(fkey.getFieldDescriptor().name()).guid().toString() + ']');
                    Table refRecord = (Table) Loader.getInstance(((Table) fkey.getReferencedTable()).classId());
                    guid refGuid = recordSet.getFieldByName(fkey.getFieldDescriptor().name()).guid();
                    if (refRecord.readRecord(refGuid, refRecord.getPrimaryFields())) {
                        exportRecord(refRecord, message, policy == null ? null : policy.getRelationsPolicy(), recordsSorter,
                                level + 1);
                        recordsSorter.addLink(table, recordId, refRecord.classId(), refGuid);
                    }
                }
            }
            if (recordSet instanceof TreeTable) {
                TreeTable treeRecordSet = (TreeTable) recordSet;
                guid parentId = treeRecordSet.parentId.get().guid();
                if (!guid.NULL.equals(parentId)) {
                    TreeTable parentRecord = (TreeTable) Loader.getInstance(recordSet.classId());
                    if (parentRecord.readRecord(parentId, parentRecord.getPrimaryFields())) {
                        exportRecord(parentRecord, message, policy == null ? null : policy.getRelationsPolicy(),
                                recordsSorter, level + 1);
                        recordsSorter.addLink(table, recordId, table, parentId);
                    }
                }
            }
        }
    }

    private static String getSpaces(int level) {
        char buf[] = new char[level * 4];
        Arrays.fill(buf, ' ');
        return new String(buf);
    }

}
