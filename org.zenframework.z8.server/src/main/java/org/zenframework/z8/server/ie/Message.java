package org.zenframework.z8.server.ie;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Message extends OBJECT implements Serializable {

    private static final long serialVersionUID = 3103056587172568570L;

    public static class CLASS<T extends Message> extends OBJECT.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(Message.class);
            setAttribute(Native, Message.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Message(container);
        }
        
    }

    private UUID id;
    private String address;
    private ExportEntry exportEntry;
    private RCollection<FileInfo> files = new RCollection<FileInfo>();

    public Message(IObject container) {
        super(container);
        id = UUID.randomUUID();
    }

    public Message() {
        id = UUID.randomUUID();
    }

    public Message(UUID id) {
        this.id = id;
    }

    public ExportEntry getExportEntry() {
        if (exportEntry == null) {
            exportEntry = new ExportEntry();
            exportEntry.setRecords(new ExportEntry.Records());
            exportEntry.setFiles(new ExportEntry.Files());
        }
        return exportEntry;
    }

    public void setExportEntry(ExportEntry exportEntry) {
        this.exportEntry = exportEntry;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public RLinkedHashMap<string, primary> getProperties() {
        RLinkedHashMap<string, primary> properties = new RLinkedHashMap<string, primary>();
        if (exportEntry.getProperties() != null) {
            for (ExportEntry.Properties.Property property : exportEntry.getProperties().getProperty()) {
                properties.put(new string(property.getKey()), primary.create(property.getType(), property.getValue()));
            }
        }
        return properties;
    }

    public void setFiles(List<FileInfo> files) {
        this.files.clear();
        this.files.addAll(files);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    
    public CLASS<Message> getMessageClass() {
        Message.CLASS<Message> messageClass = new Message.CLASS<Message>();
        messageClass.get().address = address;
        messageClass.get().exportEntry = exportEntry;
        messageClass.get().files = files;
        messageClass.get().id = id;
        return messageClass;
    }

    public guid z8_getId() {
        return new guid(id);
    }

    public string z8_getAddress() {
        return new string(address);
    }

    public RCollection<FileInfo> z8_getFiles() {
        return files;
    }
    
    public RLinkedHashMap<string, primary> z8_getProperties() {
        return getProperties();
    }

    public string z8_getXml() {
        try {
            return new string(IeUtil.marshalExportEntry(exportEntry));
        } catch (Exception e) {
            throw new exception(e);
        }
    }

}
