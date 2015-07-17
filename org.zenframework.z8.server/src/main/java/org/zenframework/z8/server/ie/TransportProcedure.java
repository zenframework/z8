package org.zenframework.z8.server.ie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.sql.sql_bool;

public class TransportProcedure extends Procedure {

    public static final String SelfAddressProperty = "selfAddress";

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

    protected final TransportContext.CLASS<TransportContext> context = new TransportContext.CLASS<TransportContext>();
    protected final TransportEngine engine = TransportEngine.getInstance();

    public TransportProcedure(IObject container) {
        super(container);
        setUseTransaction(false);
    }

    @Override
    public void constructor2() {
        super.constructor2();
        String selfAddressDefault = Properties.getProperty(ServerRuntime.SelfAddressDefaultProperty);
        if (selfAddressDefault != null && !selfAddressDefault.isEmpty()) {
            context.get().setProperty(SelfAddressProperty, selfAddressDefault);
        }
        z8_init();
    }

    @Override
    protected void z8_exec(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {

        if (!context.get().hasProperty(SelfAddressProperty)) {
            //log("Transport context property '" + SelfAddressProperty + "' is not set");
            //return;
            throw new RuntimeException("Transport context property '" + SelfAddressProperty + "' is not set");
        }

        // Отправка внутренней очереди
        Connection connection = ConnectionManager.get();
        ExportMessages messages = new ExportMessages.CLASS<ExportMessages>().get();
        messages.read(messages.getDataFields(), Arrays.<Field> asList(messages.ordinal.get()), new sql_bool(new Unary(
                Operation.Not, new SqlField(messages.sent.get()))));
        // TODO Сохранять значение локально и обновлять через listener
        boolean preserveMessages = Boolean
                .parseBoolean(Properties.getProperty(ServerRuntime.PreserveExportMessagesProperty));
        while (messages.next()) {
            Transport transport = engine.getTransport(messages.getProtocol());
            if (transport != null) {
                try {
                    transport.connect(context.get());
                    Message.CLASS<Message> message = messages.getMessage();
                    z8_beforeExport(message);
                    connection.beginTransaction();
                    if (preserveMessages) {
                        messages.sent.get().set(new bool(true));
                        messages.update(messages.recordId());
                    } else {
                        messages.destroy(messages.recordId());
                    }
                    transport.send(message.get());
                    transport.commit();
                    connection.commit();
                } catch (JAXBException e) {
                    log("Transport messsage '" + messages.recordId() + "' is broken", e);
                } catch (TransportException e) {
                    transport.close();
                    connection.rollback();
                    log("Can't export message '" + messages.recordId() + "'. Connection rolled back", e);
                }
            }
        }

        // Чтение входящих сообщений
        for (Transport transport : engine.getEnabledTransports()) {
            try {
                ApplicationServer.disableEvents();
                transport.connect(context.get());
                for (Message message = transport.receive(); message != null; message = transport.receive()) {
                    try {
                        Trace.logEvent("Receive IE message [" + message.getId() + "] by "
                                + transport.getUrl(message.getAddress()));
                        connection.beginTransaction();
                        if (message != null && !(transport instanceof NullTransport)) {
                            importRecords(transport, message);
                        }
                        z8_afterImport((Message.CLASS<Message>) message.getMessageClass());
                        connection.commit();
                        transport.commit();
                    } catch (Throwable e) {
                        connection.rollback();
                        transport.rollback();
                        throw new RuntimeException(e);
                    }
                }
            } catch (TransportException e) {
                log("Can't import message via protocol '" + transport.getProtocol() + "'", e);
                transport.close();
            } finally {
                ApplicationServer.enableEvents();
            }
        }

    }

    private void importRecords(Transport transport, Message message) {

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
                        importFiles(table, recordId, message);
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
                    importFiles(table, recordId, message);
                }
            }
        }

    }

    private static void importFiles(Table table, guid recordId, Message message) {
        if (table.readRecord(recordId, table.getPrimaryFields())) {
            for (AttachmentField attField : table.getAttachments()) {
                String json = attField.get().string().get();
                Collection<FileInfo> attachments = FileInfo.parseArray(json);

                Files files = new Files.CLASS<Files>().get();

                Collection<FileInfo> filesToAdd = new ArrayList<FileInfo>();

                for (FileInfo attachment : attachments) {
                    for (FileInfo file : message.getFiles()) {
                        if (attachment.id.equals(file.id) && !files.hasRecord(file.id))
                            filesToAdd.add(file);
                    }
                }

                Trace.logEvent("Import files " + filesToAdd);
                attField.getAttachmentProcessor().create(table.recordId(), filesToAdd);
            }
        }
    }

    protected void z8_init() {}

    protected void z8_beforeExport(Message.CLASS<? extends Message> message) {}

    protected void z8_afterImport(Message.CLASS<? extends Message> message) {}

}
