package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.functions.Sql;
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

	private String type;
	private MessageSource source = new MessageSource();
	private StringBuilder description = new StringBuilder();

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

		for(file file : getSource().files()) {
			FileMessage fileMessage = FileMessage.newInstance();
			fileMessage.setName(file.name.get());
			fileMessage.setDescription(file.json.toString());
			fileMessage.setSourceId(getId());
			fileMessage.setAddress(getAddress());
			fileMessage.setSender(getSender());
			fileMessage.setFile(file);
			fileMessage.prepare();
		}

		DataMessage dataMessage = (DataMessage)getCLASS().newInstance();
		dataMessage.setName(getName());
		dataMessage.setDescription(getDescription());
		dataMessage.setSourceId(getId());
		dataMessage.setAddress(getAddress());
		dataMessage.setSender(getSender());
		dataMessage.setSource(getSource());
		dataMessage.setType(getType());

		if (dataMessage.isExportToFile())
			exportToFile(this);
		else
			TransportQueue.newInstance().add(dataMessage);

		afterExport();
	}
	
	@SuppressWarnings("unchecked")
	public void onMerge(Table source, Table target) {
		z8_onMerge((Table.CLASS<? extends Table>) source.getCLASS(), (Table.CLASS<? extends Table>) target.getCLASS());
	}

	@Override
	protected boolean transactive() {
		return true;
	}

	@Override
	protected boolean apply() {
		source.importData();
		return true;
	}

	@Override
	protected void initDescription() {
		setDescription(description.toString());
	}
	
	public void z8_onMerge(Table.CLASS<? extends Table> source, Table.CLASS<? extends Table> target) {
		throw new UnsupportedOperationException();
	}

	public void z8_setName(string name) {
		setName(name.get());
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
		source.add(table.get(), null, (sql_bool) null);
		addDescription(table, bool.True.sql_bool());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_add(Table.CLASS<? extends Table> table, RCollection fields) {
		source.add(table.get(), CLASS.asList(fields), (sql_bool) null);
		addDescription(table, bool.True.sql_bool());
	}

	public void z8_add(Table.CLASS<? extends Table> table, sql_bool where) {
		source.add(table.get(), null, where);
		addDescription(table, where);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_add(Table.CLASS<? extends Table> table, RCollection fields, sql_bool where) {
		source.add(table.get(), CLASS.asList(fields), where);
		addDescription(table, where);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_addRecords(Table.CLASS<? extends Table> table, RCollection ids) {
		source.add(table.get(), null, ids);
		addDescription(table, Sql.z8_inVector(table.get().recordId.get().sql_guid(), ids));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_addRecords(Table.CLASS<? extends Table> table, RCollection fields, RCollection ids) {
		source.add(table.get(), CLASS.asList(fields), ids);
		addDescription(table, Sql.z8_inVector(table.get().recordId.get().sql_guid(), ids));
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
	
	private void addDescription(Table.CLASS<? extends Table> table, sql_bool where) {
		if (description.length() > 0)
			description.append(", ");
		description.append(table.name() + ": " + table.get().count(where));
	}
}
