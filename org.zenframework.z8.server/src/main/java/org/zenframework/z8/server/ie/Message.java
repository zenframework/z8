package org.zenframework.z8.server.ie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.exception;
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

	private UUID id = UUID.randomUUID();
	private String sender;
	private String address;

	private ExportEntry exportEntry;
	private RCollection<FileInfo> files = new RCollection<FileInfo>(true);

	private RLinkedHashMap<string, primary> properties = null;

	public Message(IObject container) {
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

	public String getInfo() {
		primary type = getProperties().get(Message.PROP_TYPE);
		primary group = getProperties().get(Message.PROP_GROUP);
		StringBuilder str = new StringBuilder();
		if (type != null)
			str.append(type);
		if (group != null) {
			if (str.length() > 0)
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

	public void z8_beforeImport() {}

	protected void afterImport() {
		z8_afterImport();
	}

	public void z8_afterImport() {}

	protected void beforeExport() {
		z8_beforeExport();
	}

	public void z8_beforeExport() {}

	protected void afterExport() {
		z8_afterExport();
	}

	public void z8_afterExport() {}

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		out.writeLong(serialVersionUID);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream(256 * NumericUtils.Kilobyte);
		ObjectOutputStream objects = new ObjectOutputStream(bytes);

		RmiIO.writeUUID(objects, id);
		RmiIO.writeString(objects, sender);
		RmiIO.writeString(objects, address);
		RmiIO.writeString(objects, IeUtil.marshalExportEntry(getExportEntry()));

		objects.close();

		RmiIO.writeBytes(out, IOUtils.zip(bytes.toByteArray()));

		out.writeObject(files);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = in.readLong();

		ByteArrayInputStream bytes = new ByteArrayInputStream(IOUtils.unzip(RmiIO.readBytes(in)));
		ObjectInputStream objects = new ObjectInputStream(bytes);

		id = RmiIO.readUUID(objects);
		sender = RmiIO.readString(objects);
		address = RmiIO.readString(objects);
		exportEntry = IeUtil.unmarshalExportEntry(RmiIO.readString(objects));

		objects.close();

		files = (RCollection<FileInfo>) in.readObject();
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
		} catch (Throwable e) {
			throw new exception(e);
		}
	}

}
