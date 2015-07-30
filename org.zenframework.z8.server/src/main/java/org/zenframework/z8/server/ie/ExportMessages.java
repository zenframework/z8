package org.zenframework.z8.server.ie;

import javax.xml.bind.JAXBException;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

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
        public final static String Address = "ExportMessages.address";
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

    public void addMessage(Message message, String protocol) {
        String xml = "<xml marshalling error>";
        try {
            xml = IeUtil.marshalExportEntry(message.getExportEntry());
            this.id.get().set(new string(message.getAddress()));
            this.name.get().set(new string(protocol));
            this.message.get().set(xml);
            this.ordinal.get().set(new integer(this.ordinal.get().getSequencer().next()));
            create(new guid(message.getId()));
        } catch (Exception e) {
            throw new RuntimeException("Can't add export message '" + message.getId() + "' to '" + message.getAddress()
                    + "':\n" + xml, e);
        }
    }
    
    public void setError(guid messageId, String description) {
        this.error.get().set(new bool(true));
        this.description.get().set(new string(description));
        update(messageId);
    }

    public String getAddress() {
        return id.get().get().toString();
    }

    public String getProtocol() {
        return name.get().get().toString();
    }

    public String getUrl() {
        return getProtocol() + "://" + getAddress();
    }

    public Message.CLASS<Message> getMessage() throws JAXBException {
        Message.CLASS<Message> message = new Message.CLASS<Message>();
        message.get().setId(recordId().toUUID());
        message.get().setAddress(getAddress());
        message.get().setExportEntry(IeUtil.unmarshalExportEntry(this.message.get().get().toString()));
        return message;
    }

    @Override
    public void constructor2() {
        super.constructor2();
        id.setDisplayName(Resources.get(strings.Address));
        id1.get().hidden.set(true);
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

}
