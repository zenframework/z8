package org.zenframework.z8.server.ie;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

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

	public static final string PROP_TYPE = new string("message.type");
	public static final string PROP_RECORD_ID = new string("message.recordId");
	public static final string PROP_FILE_PATH = new string("message.filePath");
	public static final string PROP_SEND_FILES_CONTENT = new string("message.sendFilesContent");

	public static final string TYPE_FILE_REQUEST = new string("file.request");
	public static final string TYPE_FILE_CONTENT = new string("file.content");

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

	public static Message instance() {
		return instance(UUID.randomUUID());
	}

	public static Message instance(UUID id) {
		Message message = new Message.CLASS<Message>().get();
		message.id = id;
		return message;
	}

	private UUID id;
	private String sender;
	private String address;
	private String exportProtocol;
	private ExportEntry exportEntry;
	private final RCollection<FileInfo> files = new RCollection<FileInfo>(true);

	private transient RLinkedHashMap<string, primary> properties = null;

	private Message(IObject container) {
		super(container);
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getExportProtocol() {
		return exportProtocol;
	}

	public void setExportProtocol(String exportProtocol) {
		this.exportProtocol = exportProtocol;
	}

	public ExportEntry getExportEntry() {
		if (exportEntry == null) {
			exportEntry = new ExportEntry();
			exportEntry.setRecords(new ExportEntry.Records());
			exportEntry.setFiles(new ExportEntry.Files());
			exportEntry.setProperties(new ExportEntry.Properties());
		}
		return exportEntry;
	}

	public void setExportEntry(ExportEntry exportEntry) {
		this.exportEntry = exportEntry;
	}

	public List<FileInfo> getFiles() {
		return files;
	}

	public void setFiles(List<FileInfo> files) {
		this.files.clear();
		this.files.addAll(files);
	}

	public RLinkedHashMap<string, primary> getProperties() {
		if (properties == null) {
			properties = new RLinkedHashMap<string, primary>();
			ExportEntry.Properties entryProps = getExportEntry().getProperties();
			if (entryProps != null) {
				for (ExportEntry.Properties.Property property : entryProps.getProperty()) {
					properties.put(new string(property.getKey()), primary.create(property.getType(), property.getValue()));
				}
			}
		}
		return properties;
	}

	public boolean isSendFilesContent() {
		RLinkedHashMap<string, primary> props = getProperties();
		return props.containsKey(PROP_SEND_FILES_CONTENT) && props.get(PROP_SEND_FILES_CONTENT).bool().get();
	}

	public guid z8_getId() {
		return new guid(id);
	}

	public string z8_getAddress() {
		return new string(address);
	}

	public string z8_getSender() {
		return new string(sender);
	}

	public RCollection<FileInfo.CLASS<FileInfo>> z8_getFiles() {
		RCollection<FileInfo.CLASS<FileInfo>> fileInfos = new RCollection<FileInfo.CLASS<FileInfo>>();
		for (ExportEntry.Files.File file : getExportEntry().getFiles().getFile()) {
			fileInfos.add(IeUtil.fileToFileInfoCLASS(file));
		}
		return fileInfos;
	}

	public RLinkedHashMap<string, primary> z8_getProperties() {
		return getProperties();
	}

	public string z8_getXml() {
		try {
			return new string(IeUtil.marshalExportEntry(getExportEntry()));
		} catch (JAXBException e) {
			throw new exception(e);
		}
	}

}
