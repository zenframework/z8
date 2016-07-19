package org.zenframework.z8.server.ie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.base.table.system.MessageQueue;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

public class Message extends OBJECT implements RmiSerializable, Serializable {

	static private final long serialVersionUID = 3103056587172568570L;

	static public final string RecordId = new string("message.recordId");

	static public class CLASS<T extends Message> extends OBJECT.CLASS<T> {
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

	private guid id = guid.NULL;
	private guid sourceId = guid.NULL;
	private String sender;
	private String address;
	private String type;

	private file file;

	private MessageSource source = new MessageSource();
	
	static public Message newInstance() {
		return new Message.CLASS<Message>(null).get();
	}
	
	public Message(IObject container) {
		super(container);
	}

	public guid getId() {
		return id;
	}

	public void setId(guid id) {
		this.id = id;
	}

	public guid getSourceId() {
		return sourceId;
	}

	public void setSourceId(guid sourceId) {
		this.sourceId = sourceId;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public MessageSource getSource() {
		return source;
	}

	public void setSource(MessageSource source) {
		this.source = source;
	}

	public boolean isFileMessage() {
		return file != null;
	}

	public file getFile() {
		return file;
	}

	public void setFile(file file) {
		this.file = file;
	}
	
	public void setBytesTransferred(long bytesTransferred) {
		if(file != null)
			file.setOffset(bytesTransferred);
	}

	protected void beforeImport() {
		z8_beforeImport();
	}

	protected void afterImport() {
		z8_afterImport();
	}

	public void beforeExport() {
		z8_beforeExport();
	}

	public void afterExport() {
		z8_afterExport();
	}

	public binary toBinary() {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bytes);
			
			serialize(out);
			
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(bytes);
			
			return new binary(bytes.toByteArray());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void fromBinary(binary binary) {
		try {
			InputStream binaryIn = binary.get();
			ObjectInputStream in = new ObjectInputStream(binaryIn);
		
			deserialize(in);
			
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(binaryIn);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		RmiIO.writeLong(out, serialVersionUID);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream(256 * NumericUtils.Kilobyte);
		ObjectOutputStream objects = new ObjectOutputStream(bytes);

		RmiIO.writeGuid(objects, id);
		RmiIO.writeGuid(objects, sourceId);
		RmiIO.writeString(objects, sender);
		RmiIO.writeString(objects, address);
		RmiIO.writeString(objects, type);

		objects.writeObject(source);
		objects.writeObject(file);

		objects.close();

		RmiIO.writeBytes(out, IOUtils.zip(bytes.toByteArray()));
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		ByteArrayInputStream bytes = new ByteArrayInputStream(IOUtils.unzip(RmiIO.readBytes(in)));
		ObjectInputStream objects = new ObjectInputStream(bytes);

		id = RmiIO.readGuid(objects);
		sourceId = RmiIO.readGuid(objects);
		sender = RmiIO.readString(objects);
		address = RmiIO.readString(objects);
		type = RmiIO.readString(objects);

		source = (MessageSource)objects.readObject();
		file = (file)objects.readObject();
		
		objects.close();
	}

	public ExportRules getExportRules() {
		return source.exportRules();
	}

	public void exportData() {
		beforeExport();
		source.exportData();
		afterExport();
	}

	public void importData() {
		beforeImport();

		try {
			ApplicationServer.disableEvents();
			source.importData();
		} finally {
			ApplicationServer.enableEvents();
		}

		afterImport();
	}
	
	public string z8_getAddress() {
		return new string(address);
	}

	public void z8_setAddress(string address) {
		setAddress(address.get());
	}

	public string z8_getSender() {
		return new string(sender);
	}

	public void z8_setSender(string sender) {
		setSender(sender.get());
	}
	
	public string z8_getType() {
		return new string(type);
	}

	public void z8_setType(string type) {
		setType(type.get());
	}

	public RLinkedHashMap<string, primary> z8_getProperties() {
		RLinkedHashMap<string, primary> properties = new RLinkedHashMap<string, primary>();

		for(Map.Entry<String, primary> entry : source.getProperties().entrySet())
			properties.put(new string(entry.getKey()), entry.getValue());
		
		return properties;
	}

	public primary z8_getProperty(string key) {
		return source.getProperty(key.get());
	}

	public void z8_setProperty(string key, primary value) {
		source.setProperty(key.get(), value);
	}

	public void z8_add(Table.CLASS<? extends Table> table) {
		source.add(table.get(), null, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_add(Table.CLASS<? extends Table> table, RCollection fields) {
		source.add(table.get(), CLASS.asList(fields), null);
	}

	public void z8_add(Table.CLASS<? extends Table> table, sql_bool where) {
		source.add(table.get(), null, where);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_add(Table.CLASS<? extends Table> table, RCollection fields, sql_bool where) {
		source.add(table.get(), CLASS.asList(fields), where);
	}

	public void z8_addRule(ImportPolicy policy) {
		source.addRule(policy);
	}

	public void z8_addRule(Table.CLASS<? extends Table> table, ImportPolicy policy) {
		source.addRule(table.get(), policy);
	}
	
	public void z8_addRule(Table.CLASS<? extends Table> table, guid recordId, ImportPolicy policy) {
		source.addRule(table.get(), recordId, policy);
	}

	public void z8_addRule(Field.CLASS<? extends Field> field, ImportPolicy policy) {
		source.addRule((Table)field.get().getOwner(), field.get(), policy);
	}

	public void z8_addRule(RCollection<Field.CLASS<? extends Field>> fields, ImportPolicy policy) {
		for(Field.CLASS<? extends Field> field : fields)
			z8_addRule(field, policy);
	}

	public void z8_addRule(guid recordId, Field.CLASS<? extends Field> field, ImportPolicy policy) {
		source.addRule((Table)field.get().getOwner(), recordId, field.get(), policy);
	}

	public void z8_addRule(guid recordId, RCollection<Field.CLASS<? extends Field>> fields, ImportPolicy policy) {
		for(Field.CLASS<? extends Field> field : fields)
			z8_addRule(recordId, field, policy);
	}

	public void z8_send() {
		if(address == null || address.isEmpty())
			throw new RuntimeException("Export address is not set");

		if(sender == null || sender.isEmpty())
			throw new RuntimeException("Sender is not set");
		
		if(Domains.newInstance().isOwner(address))
			callEvents();
		else
			MessageQueue.newInstance().add(this);
	}

	private void callEvents() {
		Connection connection = ConnectionManager.get();
		
		connection.beginTransaction();
		
		try {
			beforeExport();
			afterExport();
	
			beforeImport();
			afterImport();
			
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}
	
	public void z8_beforeImport() {
	}

	public void z8_afterImport() {
	}

	public void z8_beforeExport() {
	}

	public void z8_afterExport() {
	}
}
