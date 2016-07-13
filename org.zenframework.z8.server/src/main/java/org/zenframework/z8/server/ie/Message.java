package org.zenframework.z8.server.ie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.table.system.MessagesQueue;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

public class Message extends OBJECT implements RmiSerializable, Serializable {

	private static final long serialVersionUID = 3103056587172568570L;

	public static final string PROP_TYPE = new string("message.type");
	public static final string PROP_GROUP = new string("message.group");
	public static final string PROP_RECORD_ID = new string("message.recordId");
	public static final string PROP_FILE_PATH = new string("message.filePath");
	public static final string PROP_SEND_FILES_CONTENT = new string("message.sendFilesContent");

	public static final string TYPE_FILE_REQUEST = new string("file.request");
	public static final string TYPE_FILE_CONTENT = new string("file.content");
	public static final string TYPE_FILE_REFERENCE = new string("file.reference");

	public static class CLASS<T extends Message> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Message.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Message(container);
		}
	}

	private guid id = guid.create();
	private datetime time = new datetime();
	private String sender;
	private String address;
	private long bytesTransferred;

	private String xml;
	private ExportEntry exportEntry;
	private RCollection<file> files = new RCollection<file>(true);

	private RLinkedHashMap<string, primary> properties = null;

	public Message(IObject container) {
		super(container);
	}

	public guid getId() {
		return id;
	}

	public void setId(guid id) {
		this.id = id;
	}

	public datetime getTime() {
		return time;
	}

	public void setTime(datetime time) {
		this.time = time;
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

	public void setBytesTransferred(long bytesTransferred) {
		this.bytesTransferred = bytesTransferred;
	}

	public String getXml() {
		if(xml == null && exportEntry != null) {
			xml = IeUtil.marshalExportEntry(exportEntry);
		}
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
		this.exportEntry = null;
	}

	public ExportEntry getExportEntry() {
		if(exportEntry == null && xml != null) {
			exportEntry = IeUtil.unmarshalExportEntry(xml);
		}
		if(exportEntry == null) {
			exportEntry = new ExportEntry();
			exportEntry.setRecords(new ExportEntry.Records());
			exportEntry.setFiles(new ExportEntry.Files());
			exportEntry.setProperties(new ExportEntry.Properties());
		}
		return exportEntry;
	}

	public void setExportEntry(ExportEntry exportEntry) {
		this.exportEntry = exportEntry;
		this.xml = null;
	}

	public List<file> getFiles() {
		return files;
	}

	public void setFiles(List<file> files) {
		this.files.clear();
		this.files.addAll(files);
	}

	public RLinkedHashMap<string, primary> getProperties() {
		if(properties == null) {
			properties = new RLinkedHashMap<string, primary>();
			ExportEntry.Properties entryProps = getExportEntry().getProperties();
			if(entryProps != null) {
				for(ExportEntry.Properties.Property property : entryProps.getProperty()) {
					properties.put(new string(property.getKey()), primary.create(property.getType(), property.getValue()));
				}
			}
		}
		return properties;
	}

	public String getInfo() {
		primary type = getProperties().get(Message.PROP_TYPE);
		primary group = getProperties().get(Message.PROP_GROUP);
		StringBuilder str = new StringBuilder();
		if(type != null)
			str.append(type);
		if(group != null) {
			if(str.length() > 0)
				str.append(" : ");
			str.append(group);
		}
		return str.toString();
	}

	public boolean isSendFilesContent() {
		RLinkedHashMap<string, primary> props = getProperties();
		return props.containsKey(PROP_SEND_FILES_CONTENT) && props.get(PROP_SEND_FILES_CONTENT).bool().get();
	}

	@Override
	public String toString() {
		return new StringBuilder().append(id).append(':').append(sender).append("->").append(address).toString();
	}

	protected void beforeImport() {
		z8_beforeImport();
	}

	public void z8_beforeImport() {
	}

	protected void afterImport() {
		z8_afterImport();
	}

	public void z8_afterImport() {
	}

	protected void beforeExport() {
		z8_beforeExport();
	}

	public void z8_beforeExport() {
	}

	protected void afterExport() {
		z8_afterExport();
	}

	public void z8_afterExport() {
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	public void info(String info) {
		MessagesQueue.newInstance().info(getId(), info);
	}

	public void processed() {
		processed(null);
	}
	
	public void processed(String info) {
		MessagesQueue.newInstance().processed(getId(), info);
	}
	
	public void transferred(long bytes) {
		MessagesQueue.newInstance().transferred(getId(), bytes);
	}

	public boolean isFile() {
		List<ExportEntry.Files.File> files = getExportEntry().getFiles().getFile();

		if(files.size() > 1)
			throw new RuntimeException("Message must contain one or zero files");

		return files.size() == 1;
	}

	public file getFile() {
		ExportEntry.Files.File desc = getExportEntry().getFiles().getFile().get(0);
		file file = new file(new guid(desc.getId()), desc.getId(), desc.getInstanceId(), desc.getPath(), 0, new datetime(desc.getTime()));
		file.setOffset(bytesTransferred);
		return file;
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		RmiIO.writeLong(out, serialVersionUID);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream(256 * NumericUtils.Kilobyte);
		ObjectOutputStream objects = new ObjectOutputStream(bytes);

		RmiIO.writeGuid(objects, id);
		RmiIO.writeDatetime(objects, time);
		RmiIO.writeString(objects, sender);
		RmiIO.writeString(objects, address);
		RmiIO.writeString(objects, getXml());

		objects.close();

		RmiIO.writeBytes(out, IOUtils.zip(bytes.toByteArray()));

		// out.writeObject(files);
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		ByteArrayInputStream bytes = new ByteArrayInputStream(IOUtils.unzip(RmiIO.readBytes(in)));
		ObjectInputStream objects = new ObjectInputStream(bytes);

		id = RmiIO.readGuid(objects);
		time = RmiIO.readDatetime(objects);
		sender = RmiIO.readString(objects);
		address = RmiIO.readString(objects);
		xml = RmiIO.readString(objects);

		objects.close();

		// files = (RCollection<file>) in.readObject();
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

	public RCollection<file> z8_getFiles() {
		RCollection<file> files = new RCollection<file>();
		for(ExportEntry.Files.File file : getExportEntry().getFiles().getFile()) {
			files.add(IeUtil.fileToFileInfo(file));
		}
		return files;
	}

	public RLinkedHashMap<string, primary> z8_getProperties() {
		return getProperties();
	}

	public string z8_getXml() {
		try {
			return new string(getXml());
		} catch(Throwable e) {
			throw new exception(e);
		}
	}

}
