package org.zenframework.z8.server.ie;

import javax.xml.bind.JAXBException;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
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
        public final static String Sent = "ExportMessages.sent";
    }

    public IntegerField.CLASS<IntegerField> ordinal = new IntegerField.CLASS<IntegerField>(this);
    public TextField.CLASS<TextField> message = new TextField.CLASS<TextField>(this);
    public BoolField.CLASS<BoolField> sent = new BoolField.CLASS<BoolField>(this);

    public ExportMessages() {
        this(null);
    }

    public ExportMessages(IObject container) {
        super(container);
    }

    public static void addMessage(Message message, String protocol) {
        String xml = "<xml marshalling error>";
        try {
            xml = IeUtil.marshalExportEntry(message.getExportEntry());
            ExportMessages messages = new CLASS<ExportMessages>().get();
            messages.name.get().set(new string(message.getAddress()));
            messages.description.get().set(new string(protocol));
            messages.message.get().set(xml);
            messages.ordinal.get().set(new integer(messages.id.get().getSequencer().next()));
            messages.create(new guid(message.getId()));
        } catch (Exception e) {
            throw new RuntimeException("Can't add export message '" + message.getId() + "' to '" + message.getAddress()
                    + "':\n" + xml, e);
        }
    }

    public String getProtocol() {
        return description.get().get().toString();
    }

    public Message.CLASS<Message> getMessage() throws JAXBException {
        Message.CLASS<Message> message = new Message.CLASS<Message>();
        message.get().setId(recordId().toUUID());
        message.get().setAddress(name.get().get().toString());
        message.get().setExportEntry(IeUtil.unmarshalExportEntry(this.message.get().get().toString()));
        return message;
    }

    @Override
    public void constructor2() {
        super.constructor2();
        id.get().hidden.set(true);
        id1.get().hidden.set(true);
        name.setDisplayName(Resources.get(strings.Address));
        description.setDisplayName(Resources.get(strings.Protocol));
        ordinal.setName("Ordinal");
        ordinal.setIndex("ordinal");
        ordinal.setDisplayName(Resources.get(strings.Ordinal));
        ordinal.get().unique.set(true);
        sent.setName("Sent");
        sent.setIndex("sent");
        sent.setDisplayName(Resources.get(strings.Sent));
        message.setName("Xml");
        message.setIndex("xml");
        message.setDisplayName(Resources.get(strings.Message));
        message.get().colspan.set(3);
        registerDataField(ordinal);
        registerDataField(sent);
        registerDataField(message);
    }

}
