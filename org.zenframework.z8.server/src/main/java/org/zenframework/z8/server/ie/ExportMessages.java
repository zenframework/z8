package org.zenframework.z8.server.ie;

import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;

public class ExportMessages extends Table {

    public static final String TableName = "SystemExportMessages";

    public static class CLASS<T extends ExportMessages> extends Table.CLASS<T> {

        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(ExportMessages.class);
            setName(TableName);
            setDisplayName(Resources.get(strings.Title));
        }

        @Override
        public Object newObject(IObject container) {
            return new ExportMessages(container);
        }

    }

    static public class strings {
        public final static String Title = "ExportMessages.title";
        public final static String Sender = "ExportMessages.sender";
        public final static String Receiver = "ExportMessages.receiver";
        public final static String Protocol = "ExportMessages.protocol";
        public final static String Message = "ExportMessages.message";
        public final static String Ordinal = "ExportMessages.ordinal";
        public final static String Processed = "ExportMessages.processed";
        public final static String Error = "ExportMessages.error";
    }

    public IntegerField.CLASS<IntegerField> ordinal = new IntegerField.CLASS<IntegerField>(this);
    public TextField.CLASS<TextField> message = new TextField.CLASS<TextField>(this);
    public BoolField.CLASS<BoolField> processed = new BoolField.CLASS<BoolField>(this);
    public BoolField.CLASS<BoolField> error = new BoolField.CLASS<BoolField>(this);

    private ExportMessages(IObject container) {
        super(container);
    }

    public void addMessage(Message message, String protocol, String sender) throws JAXBException {
        this.id.get().set(new string(sender));
        this.id1.get().set(new string(message.getAddress()));
        this.name.get().set(new string(protocol));
        this.ordinal.get().set(new integer(this.ordinal.get().getSequencer().next()));
        this.message.get().set(new string(IeUtil.marshalExportEntry(message.getExportEntry())));
        create(new guid(message.getId()));
    }
    
    public void setError(guid messageId, String description) {
        this.error.get().set(new bool(true));
        this.description.get().set(new string(description));
        update(messageId);
    }

    public String getSender() {
        return id.get().get().toString();
    }

    public String getReceiver() {
        return id1.get().get().toString();
    }

    public String getProtocol() {
        return name.get().get().toString();
    }

    public String getUrl() {
        return getProtocol() + ":" + getReceiver();
    }

    public Message.CLASS<Message> getMessage() throws JAXBException {
        Message.CLASS<Message> message = new Message.CLASS<Message>();
        message.get().setId(recordId().get());
        message.get().setAddress(getReceiver());
        message.get().setExportEntry(IeUtil.unmarshalExportEntry(this.message.get().get().toString()));
        return message;
    }

    @Override
    public void constructor2() {
        super.constructor2();
        id.setDisplayName(Resources.get(strings.Sender));
        id1.setDisplayName(Resources.get(strings.Receiver));
        name.setDisplayName(Resources.get(strings.Protocol));
        ordinal.setName("Ordinal");
        ordinal.setIndex("ordinal");
        ordinal.setDisplayName(Resources.get(strings.Ordinal));
        ordinal.get().unique.set(true);
        processed.setName("Sent");
        processed.setIndex("sent");
        processed.setDisplayName(Resources.get(strings.Processed));
        error.setName("Error");
        error.setIndex("error");
        error.setDisplayName(Resources.get(strings.Error));
        message.setName("Xml");
        message.setIndex("xml");
        message.setDisplayName(Resources.get(strings.Message));
        message.get().colspan.set(3);
        registerDataField(ordinal);
        registerDataField(processed);
        registerDataField(error);
        registerDataField(message);
    }
    
    public static ExportMessages instance() {
        return new CLASS<ExportMessages>().get();
    }

    public void readExportMessages(String selfAddress) {
        SqlToken notProcessedNotError = new And(
                new Unary(Operation.Not, new SqlField(processed.get())),
                new Unary(Operation.Not, new SqlField(error.get())));
        SqlToken fromMeNotForMe = new And(
                new Rel(id.get(), Operation.Eq, new sql_string(selfAddress)),
                new Rel(id1.get(), Operation.NotEq, new sql_string(selfAddress)));
        read(getDataFields(), Arrays.<Field> asList(ordinal.get()), new And(notProcessedNotError, fromMeNotForMe));
    }

    public void readImportMessages(String selfAddress) {
        SqlToken notProcessedNotError = new And(
                new Unary(Operation.Not, new SqlField(processed.get())),
                new Unary(Operation.Not, new SqlField(error.get())));
        SqlToken forMe = new Rel(id1.get(), Operation.Eq, new sql_string(selfAddress));
        read(getDataFields(), Arrays.<Field> asList(ordinal.get()), new And(notProcessedNotError, forMe));
    }

}
