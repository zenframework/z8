package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;

public class MessageSource implements RmiSerializable, Serializable {
	private static final long serialVersionUID = -145929531248527279L;

	private ExportRules exportRules = new ExportRules();
	private Collection<ExportSource> sources = new ArrayList<ExportSource>();
	private Map<String, primary> properties = new HashMap<String, primary>();

	private Collection<RecordInfo> inserts = new ArrayList<RecordInfo>();
	private Collection<RecordInfo> updates = new ArrayList<RecordInfo>();

	private Collection<file> files = new ArrayList<file>();

	public ExportRules exportRules() {
		return exportRules;
	}

	public void add(Table table, Collection<Field> fields, sql_bool where) {
		if (table.exportable())
			sources.add(new ExportSource(table, fields, where));
	}
	
	public void addRule(ImportPolicy importPolicy) {
		exportRules.add(importPolicy);
	}
	
	public void addRule(Table table, ImportPolicy policy) {
		exportRules.add(table.name(), policy);
	}

	public void addRule(Table table, guid recordId, ImportPolicy policy) {
		exportRules.add(table.name(), recordId, policy);
	}

	public void addRule(Table table, Field field, ImportPolicy policy) {
		exportRules.add(table.name(), field.name(), policy);
	}

	public void addRule(Table table, guid recordId, Field field, ImportPolicy policy) {
		exportRules.add(table.name(), recordId, field.name(), policy);
	}

	public Map<String, primary> getProperties() {
		return properties;
	}

	public primary getProperty(String key) {
		return properties.get(key);
	}

	public void setProperty(String key, primary value) {
		properties.put(key, value);
	}

	public Collection<file> files() {
		return files;
	}
	
	public void exportData() {
		for(ExportSource source : sources)
			processExportSource(source);
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

		out.writeObject(exportRules);
		out.writeObject(sources);
		out.writeObject(properties);

		out.writeObject(inserts);
		out.writeObject(updates);

		out.writeObject(files);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		exportRules = (ExportRules)in.readObject();
		sources = (Collection<ExportSource>)in.readObject();
		properties = (Map<String, primary>)in.readObject();

		inserts = (Collection<RecordInfo>)in.readObject();
		updates = (Collection<RecordInfo>)in.readObject();

		files = (Collection<file>)in.readObject();
	}

	private Map<String, Boolean> addedRecords = new HashMap<String, Boolean>();
	
	private void processExportSource(ExportSource source) {
		Table table = source.table();
		String tableName = source.name();

		Collection<Field> fields = source.fields();
		
		table.saveState();
		
		Collection<guid> records = source.records();
		SqlToken where = records != null ? new InVector(table.primaryKey(), records) : null;
		table.read(fields, where);

		while(table.next()) {
			guid recordId = table.recordId();
		
			String id = makeUniqueId(tableName, recordId);
			
			if(addedRecords.get(id) != null)
				continue;
			
			addedRecords.put(id, false);
			
			RecordInfo record = new RecordInfo(recordId, tableName);
			
			for(Field field : fields) {
				if(!processLink(field, recordId))
					continue;

				processAttachments(field);
				
				record.add(new FieldInfo(field));
			}
			
			inserts.add(record);

			addedRecords.put(id, true);
		}
		
		table.restoreState();
	}
	
	private void processAttachments(Field field) {
		boolean isAttachment = field instanceof AttachmentField;
		
		if(!isAttachment)
			return;
		
		for(file file : file.parse(field.string().get())) {
			if(!files.contains(file))
				files.add(file);
		}
	}

	private boolean processLink(Field field, guid recordId) {
		boolean isLink = field instanceof Link;
		boolean isParentKey = field.isParentKey();
		
		if(!isLink && !isParentKey)
			return true;
		
		Table table = (Table)(isParentKey ? field.getOwner() : ((Link)field).getQuery());
		
		String name = table.name();
		guid value = field.guid();
		
		if(value.equals(guid.NULL) || 
				BuiltinUsers.System.guid().equals(value) || 
				BuiltinUsers.Administrator.guid().equals(value))
			return false;

		Boolean recordState = addedRecords.get(makeUniqueId(name, value));
		boolean notProcessed = recordState == null;
		boolean inProcess = recordState != null && !recordState;
		
		if(notProcessed) {
			ExportSource source = new ExportSource(table, null, Arrays.asList(value));
			processExportSource(source);
			return true;
		} else if(inProcess) {
			processUpdate(recordId, field);
			return false;
		} else
			return true;
	}

	private void processUpdate(guid recordId, Field field) {
		RecordInfo record = new RecordInfo(recordId, field.getOwner().name());
		record.add(new FieldInfo(field));
		updates.add(record);
	}
	
	private String makeUniqueId(String name, guid id) {
		return name + "/" + id;
	}

	public void apply() {
		for(RecordInfo record : inserts)
			insert(record);

		for(RecordInfo record : updates)
			update(record);
		
		fieldCache.clear();
		tableCache.clear();
	}

	private Map<String, Table> tableCache = new HashMap<String, Table>();
	private Map<String, Field> fieldCache = new HashMap<String, Field>();

	private Table getTable(String name) {
		Table table = tableCache.get(name);
		
		if(table == null) {
			table = (Table)Runtime.instance().getTableByName(name).newInstance();
			tableCache.put(name, table);
		}
		
		return table;
	}
	
	private Field getField(String tableName, String fieldName) {
		String key = tableName + "." + fieldName;
		
		Field field = fieldCache.get(key);
		
		if(field == null) {
			field = getTable(tableName).getFieldByName(fieldName);
			fieldCache.put(key, field);
		}
		
		return field;
	}

	private void insert(RecordInfo record) {
		String tableName = record.table();
		Table table = getTable(tableName);
		guid recordId = record.id();

		Collection<Field> attachments = table.getAttachments();
		boolean exists = table.readRecord(recordId, attachments);

		for(FieldInfo fieldInfo : record.fields()) {
			String fieldName = fieldInfo.name();
			Field field = getField(tableName, fieldName);
			
			if(field == null)
				throw new RuntimeException("Incorrect record format. Table '" + tableName + "' has no field '" + fieldName + "'");

			ImportPolicy fieldPolicy = exportRules.getPolicy(tableName, fieldName, recordId);

			if(!exists || fieldPolicy == ImportPolicy.OVERRIDE) {
				primary value = fieldInfo.value();
			
				if(exists && field instanceof AttachmentField)
					value = mergeAttachments(field.string(), (string)value);
				
				field.set(value);
			}
		}

		if(!exists)
			table.create(recordId);
		else
			table.update(recordId);
	}

	private void update(RecordInfo record) {
		String tableName = record.table();
		Table table = getTable(tableName);

		for(FieldInfo fieldInfo : record.fields()) {
			String fieldName = fieldInfo.name();
			Field field = getField(tableName, fieldName);

			if(field == null)
				throw new RuntimeException("Incorrect record format. Table '" + tableName + "' has no field '" + fieldName + "'");
			
			field.set(fieldInfo.value());
		}
		
		table.update(record.id());
	}
	
	static private string mergeAttachments(string value1, string value2) {
		if(value1.equals(value2) || value2.isEmpty())
			return value1;
		
		if(value1.isEmpty())
			return value2;

		Collection<file> files = file.parse(value1.get());
		
		for(file file : file.parse(value2.get())) {
			if(!files.contains(file))
				files.add(file);
		}
		
		return new string(new JsonArray(files).toString());
	}
}
