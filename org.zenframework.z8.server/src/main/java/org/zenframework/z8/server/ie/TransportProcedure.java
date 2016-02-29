package org.zenframework.z8.server.ie;

import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.logs.Trace;
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
            setAttribute(Job, "");
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

        String selfAddress = context.get().check().getProperty(TransportContext.SelfAddressProperty);
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
                z8_beforeImport(message);
                ApplicationServer.disableEvents();
                try {
                    Import.importRecords(message.get());
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
                        z8_afterExport(message);
                    } catch (TransportException e) {
                        transport.close();
                        throw e;
                    }
                }
                connection.commit();
            } catch (Throwable e) {
                connection.rollback();
                log("Can't send messsage '" + messages.recordId() + "'", e);
            }
        }

        // Чтение входящих сообщений
        for (Transport transport : engine.getEnabledTransports(context.get())) {
            try {
                transport.connect();
                for (Message message = transport.receive(); message != null; message = transport.receive()) {
                    try {
                        connection.beginTransaction();
                        messages.addMessage(message, transport.getProtocol());
                        Import.importFiles(message, filesTable);
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

    private static void beginProcessMessage(ExportMessages messages) {
        if (preserveExportMessages) {
            messages.processed.get().set(new bool(true));
            messages.update(messages.recordId());
        } else {
            messages.destroy(messages.recordId());
        }
    }

    protected void z8_init() {}

    protected void z8_beforeImport(Message.CLASS<? extends Message> message) {}

    protected void z8_afterImport(Message.CLASS<? extends Message> message) {}

    protected void z8_beforeExport(Message.CLASS<? extends Message> message) {}

    protected void z8_afterExport(Message.CLASS<? extends Message> message) {}

}
