package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;

public class DataMessage extends Message {

	static private final long serialVersionUID = 3103056587172568570L;

	static public final string RecordId = new string("message.recordId");

	static public class CLASS<T extends DataMessage> extends Message.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(DataMessage.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new DataMessage(container);
		}
	}

	public boolean sendFiles = true;
	private String type;
	private MessageSource source = new MessageSource();

	static public DataMessage newInstance() {
		return new DataMessage.CLASS<DataMessage>(null).get();
	}

	public DataMessage(IObject container) {
		super(container);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isSendFiles() {
		return sendFiles;
	}

	public void setSendFiles(boolean sendFiles) {
		this.sendFiles = sendFiles;
	}

	public MessageSource getSource() {
		return source;
	}

	public void setSource(MessageSource source) {
		this.source = source;
	}

	@Override
	public void setBytesTransferred(long bytes) {
	}

	@Override
	protected void write(ObjectOutputStream out) throws IOException {
		RmiIO.writeLong(out, serialVersionUID);

		RmiIO.writeString(out, type);
		out.writeObject(source);
	}

	@Override
	protected void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		type = RmiIO.readString(in);
		source = (MessageSource)in.readObject();
	}

	public ExportRules getExportRules() {
		return source.exportRules();
	}

	@Override
	public void prepare() {
		beforeExport();

		source.exportData();

		if(sendFiles) {
			for(file file : getSource().files()) {
				FileMessage fileMessage = FileMessage.newInstance();
				fileMessage.setSourceId(getId());
				fileMessage.setAddress(getAddress());
				fileMessage.setSender(getSender());
				fileMessage.setFile(file);
				fileMessage.prepare();
			}
		}

		DataMessage dataMessage = (DataMessage)getCLASS().newInstance();
		dataMessage.setSourceId(getId());
		dataMessage.setAddress(getAddress());
		dataMessage.setSender(getSender());
		dataMessage.setSource(getSource());
		dataMessage.setType(getType());

		TransportQueue.newInstance().add(dataMessage);

		afterExport();
	}

	@SuppressWarnings("unchecked")
	public void onMerge(Table source, Table target, String tableName) {
		z8_onMerge((Table.CLASS<? extends Table>) source.getCLASS(), (Table.CLASS<? extends Table>) target.getCLASS());
	}

	@Override
	protected boolean transactive() {
		return true;
	}

	@Override
	protected boolean apply() {
		source.importData(this);
		return true;
	}

	public void z8_onMerge(Table.CLASS<? extends Table> source, Table.CLASS<? extends Table> target) {
		throw new UnsupportedOperationException();
	}

	public string z8_getType() {
		return new string(type);
	}

	public void z8_setType(string type) {
		setType(type.get());
	}
	
	public bool z8_isSendFiles() {
		return new bool(sendFiles);
	}

	public void z8_setSendFiles(bool sendFiles) {
		setSendFiles(sendFiles.get());
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RCollection z8_getRecords(Table.CLASS<? extends Table> table) {
		return new RCollection(source.getRecords(table.get().name()));
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
	
	public void z8_setExportAll(bool exportAll) {
		source.setExportAll(exportAll.get());
	}
	
	public bool z8_isExportAll() {
		return new bool(source.isExportAll());
	}
}
