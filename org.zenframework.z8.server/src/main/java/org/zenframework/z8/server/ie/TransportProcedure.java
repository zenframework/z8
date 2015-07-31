package org.zenframework.z8.server.ie;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;

public class TransportProcedure extends Procedure {

    public static final guid PROCEDURE_ID = new guid("E43F94C6-E918-405D-898C-B915CC51FFDF");

    public static class CLASS<T extends TransportProcedure> extends Procedure.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(TransportProcedure.class);
            setAttribute(Native, TransportProcedure.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new TransportProcedure(container);
        }
    }

    private static class PreserveExportMessagesListener implements Properties.Listener {

        @Override
        public void onPropertyChange(String key, String value) {
            if (ServerRuntime.PreserveExportMessagesProperty.equalsKey(key)) {
                preserveExportMessages = Boolean.parseBoolean(value);
            }
        }

    }

    static {
        Properties.addListener(new PreserveExportMessagesListener());
    }

    private static volatile boolean preserveExportMessages = Boolean.parseBoolean(Properties
            .getProperty(ServerRuntime.PreserveExportMessagesProperty));

    protected final TransportContext.CLASS<TransportContext> context = new TransportContext.CLASS<TransportContext>();
    protected final TransportEngine engine = TransportEngine.getInstance();

    public TransportProcedure(IObject container) {
        super(container);
        setUseTransaction(false);
    }

    @Override
    public void constructor2() {
        super.constructor2();
        z8_init();
    }

    @Override
    protected void z8_exec(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {

        String selfAddress = context.get().getProperty(TransportContext.SelfAddressProperty);
        Connection connection = ConnectionManager.get();
        ExportMessages messages = ExportMessages.instance();
        Files filesTable = Files.instance();

        // Обработка внутренней входящей очереди
        messages.readImportMessages(selfAddress);
        while (messages.next()) {
            try {
                Message.CLASS<Message> message = messages.getMessage();
                connection.beginTransaction();
                beginProcessMessage(messages);
                Trace.logEvent("Receive IE message [" + message.get().getId() + "] by " + messages.getUrl());
                ApplicationServer.disableEvents();
                try {
                    importRecords(message.get());
                } finally {
                    ApplicationServer.enableEvents();
                }
                z8_afterImport(message);
                connection.commit();
            } catch (Throwable e) {
                connection.rollback();
                log("Transport messsage '" + messages.recordId() + "' is broken", e);
                messages.setError(messages.recordId(), e.getMessage());
            }
        }

        // Обработка внутренней исходящей очереди
        messages.readExportMessages(selfAddress);
        while (messages.next()) {
            try {
                Message.CLASS<Message> message = messages.getMessage();
                Transport transport = engine.getTransport(context.get(), messages.getProtocol());
                connection.beginTransaction();
                if (transport != null) {
                    beginProcessMessage(messages);
                    z8_beforeExport(message);
                    transport.connect();
                    try {
                        transport.send(message.get());
                        transport.commit();
                    } catch (TransportException e) {
                        transport.close();
                        throw e;
                    }
                }
                connection.commit();
            } catch (Throwable e) {
                connection.rollback();
                log("Transport messsage '" + messages.recordId() + "' is broken", e);
                messages.setError(messages.recordId(), e.getMessage());
            }
        }

        // Чтение входящих сообщений
        for (Transport transport : engine.getEnabledTransports(context.get())) {
            try {
                transport.connect();
                for (Message message = transport.receive(); message != null; message = transport.receive()) {
                    try {
                        connection.beginTransaction();
                        messages.addMessage(message, transport.getProtocol(), selfAddress);
                        importFiles(message, filesTable);
                        connection.commit();
                        transport.commit();
                    } catch (Throwable e) {
                        log("Can't save incoming message " + message.getId() + " from '" + transport.getUrl(message.getAddress()) + "'", e);
                        connection.rollback();
                        transport.rollback();
                    }
                }
            } catch (TransportException e) {
                log("Can't import message via protocol '" + transport.getProtocol() + "'", e);
                transport.close();
            }
        }

    }

    private void importRecords(Message message) {
        // Импорт записей
        for (ExportEntry.Records.Record record : message.getExportEntry().getRecords().getRecord()) {
            Table table = (Table) Loader.getInstance(record.getTable());
            guid recordId = new guid(record.getRecordId());
            if (!IeUtil.isBuiltinRecord(recordId)) {
                if (table.hasRecord(recordId)) {
                    // Если запись уже существует
                    ImportPolicy policy = ImportPolicy.getPolicy(record.getPolicy());
                    if (policy.isOverride()) {
                        // Если запись должна быть обновлена согласно политике,
                        // обновить
                        Trace.logEvent("Import: update record " + IeUtil.toString(record));
                        IeUtil.fillTableRecord(table, record);
                        table.update(recordId);
                    } else {
                        // Если запись не должна быть обновлена, ничего не
                        // делать
                        Trace.logEvent("Import: skip record " + IeUtil.toString(record));
                    }
                } else {
                    // Если запись не существует, создать
                    Trace.logEvent("Import: create record " + IeUtil.toString(record));
                    IeUtil.fillTableRecord(table, record);
                    table.create(recordId);
                }
            }
        }
    }

    private static void importFiles(Message message, Files filesTable) {
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
    
    private static void beginProcessMessage(ExportMessages messages) {
        if (preserveExportMessages) {
            messages.processed.get().set(new bool(true));
            messages.update(messages.recordId());
        } else {
            messages.destroy(messages.recordId());
        }
    }

    protected void z8_init() {}

    protected void z8_afterImport(Message.CLASS<? extends Message> message) {}

    protected void z8_beforeExport(Message.CLASS<? extends Message> message) {}

}
