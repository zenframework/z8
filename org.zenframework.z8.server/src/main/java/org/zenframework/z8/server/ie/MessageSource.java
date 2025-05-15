package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.FileField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.EventsLevel;
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

	private static class Cache {
		final Map<String, Table> tables = new HashMap<String, Table>();
		final Map<String, Field> fields = new HashMap<String, Field>();
		final Set<guid> records = new HashSet<guid>();

		void clear() {
			tables.clear();
			fields.clear();
			records.clear();
		}
	}

	private boolean exportAll;
	private boolean skipFiles;
	private ExportRules exportRules = new ExportRules();
	private Collection<ExportSource> sources = new ArrayList<ExportSource>();
	private Map<String, primary> properties = new HashMap<String, primary>();

	private Collection<RecordInfo> inserts = new ArrayList<RecordInfo>();
	private Collection<RecordInfo> updates = new ArrayList<RecordInfo>();

	private Collection<file> files = new ArrayList<file>();

	public ExportRules exportRules() {
		return exportRules;
	}
	
	public Collection<guid> getRecords(String tableName) {
		Collection<guid> result = new ArrayList<guid>();
		
		for(ExportSource src : sources) {
			if(src.name().equals(tableName))
				result.addAll(src.records());
		}
		
		return result;
	}

	public void add(Table table, Collection<Field> fields, sql_bool where) {
		if(table.exportable() || exportAll)
			sources.add(new ExportSource(table, fields, exportAll, where));
	}

	public void add(Table table, Collection<Field> fields, Collection<guid> ids) {
		if(table.exportable() || exportAll)
			sources.add(new ExportSource(table, fields, exportAll, ids));
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
		Map<String, Boolean> recordStates = new HashMap<String, Boolean>();
		ApplicationServer.setEventsLevel(EventsLevel.NONE);

		for(ExportSource source : sources)
			processExportSource(source, recordStates);

		ApplicationServer.restoreEventsLevel();
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
		
		RmiIO.writeBoolean(out, exportAll);

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
		long version = RmiIO.readLong(in);

		if(version == serialVersionUID)
			exportAll = RmiIO.readBoolean(in);

		exportRules = (ExportRules)in.readObject();
		sources = (Collection<ExportSource>)in.readObject();
		properties = (Map<String, primary>)in.readObject();

		inserts = (Collection<RecordInfo>)in.readObject();
		updates = (Collection<RecordInfo>)in.readObject();

		files = (Collection<file>)in.readObject();
	}

	private void processExportSource(ExportSource source, Map<String, Boolean> recordStates) {
		Table table = source.table();
		String tableName = source.name();

		Collection<Field> fields = source.fields();

		table.saveState();

		Collection<guid> records = source.records();
		SqlToken where = records != null ? table.primaryKey().inVector(records) : null;
		table.setWhere(where); // to replace existing where
		table.read(fields);

		while(table.next()) {
			guid recordId = table.recordId();

			String id = makeUniqueId(tableName, recordId);

			if(recordStates.get(id) != null)
				continue;

			recordStates.put(id, false);

			RecordInfo record = new RecordInfo(recordId, tableName);

			for(Field field : fields) {
				if(!processLink(field, recordId, recordStates))
					continue;

				if(!skipFiles)
					processFiles(field);

				record.add(new FieldInfo(field));
			}

			inserts.add(record);

			recordStates.put(id, true);
		}

		table.restoreState();
	}

	private void processFiles(Field field) {
		boolean isFile = field instanceof FileField;

		if(!isFile)
			return;

		for(file f : file.parse(field.string().get())) {
			if(!files.contains(f))
				files.add(f);
		}
	}

	private boolean processLink(Field field, guid recordId, Map<String, Boolean> recordStates) {
		boolean isLink = field instanceof Link;
		boolean isParentKey = field.isParentKey();

		if(!isLink && !isParentKey)
			return true;

		Table table = (Table)((Link)field).getQuery();

		if(isParentKey && table == null)
			table = (Table)field.getOwner();

		String name = table.name();
		guid value = field.guid();

		if(value.equals(guid.Null))
			return true;

		if(BuiltinUsers.System.guid().equals(value) || BuiltinUsers.Administrator.guid().equals(value))
			return false;

		Boolean state = recordStates.get(makeUniqueId(name, value));
		boolean notInserted = state == null;
		boolean inserting = state != null && !state;

		if(notInserted) {
			ExportSource source = new ExportSource(table, null, exportAll, Arrays.asList(value));
			processExportSource(source, recordStates);
			return true;
		} else if(inserting) {
			processUpdate(recordId, field);
			return false;
		}

		// inserted
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

	public void importData() {
		Cache cache = new Cache();
		ApplicationServer.setEventsLevel(EventsLevel.NONE);

		for(RecordInfo record : inserts)
			insert(record, cache);

		ConnectionManager.get().flush();

		for(RecordInfo record : updates)
			update(record, cache);

		cache.clear();

		ApplicationServer.restoreEventsLevel();
	}

	private Table getTable(String name, Cache cache) {
		Table table = cache.tables.get(name);

		if(table == null) {
			table = (Table)Runtime.instance().getTableByName(name).newInstance();
			cache.tables.put(name, table);
		}

		return table;
	}

	private Field getField(String tableName, String fieldName, Cache cache) {
		String key = tableName + "." + fieldName;

		Field field = cache.fields.get(key);

		if(field == null) {
			Table table = getTable(tableName, cache);
			if (table == null)
				throw new RuntimeException("Table '" + tableName + "' not found");
			field = table.getFieldByName(fieldName);
			cache.fields.put(key, field);
		}

		return field;
	}

	private void insert(RecordInfo record, Cache cache) {
		String tableName = record.table();
		Table table = getTable(tableName, cache);
		guid recordId = record.id();

		Collection<Field> attachments = table.attachments();
		boolean exists = table.readRecord(recordId, attachments);

		for(FieldInfo fieldInfo : record.fields()) {
			String fieldName = fieldInfo.name();
			Field field = getField(tableName, fieldName, cache);

			if(field == null)
				throw new RuntimeException("Incorrect record format. Table '" + tableName + "' has no field '" + fieldName + "'");

			ImportPolicy fieldPolicy = exportRules.getPolicy(tableName, fieldName, recordId);

			if(!exists || fieldPolicy == ImportPolicy.Override) {
				primary value = fieldInfo.value();

				if(exists && field instanceof FileField)
					value = mergeAttachments(field.string(), (string)value);

				field.set(value);
			}
		}

		if(!exists) {
			table.create(recordId);
			cache.records.add(recordId);
		} else
			table.update(recordId);
	}

	private void update(RecordInfo record, Cache cache) {
		String tableName = record.table();
		Table table = getTable(tableName, cache);
		guid recordId = record.id();

		for(FieldInfo fieldInfo : record.fields()) {
			String fieldName = fieldInfo.name();

			if(!cache.records.contains(recordId)) {
				ImportPolicy fieldPolicy = exportRules.getPolicy(tableName, fieldName, recordId);
				if(fieldPolicy != ImportPolicy.Override)
					continue;
			}

			Field field = getField(tableName, fieldName, cache);

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

		for(file f : file.parse(value2.get())) {
			if(!files.contains(f))
				files.add(f);
		}

		return new string(new JsonArray(files).toString());
	}
	
	public boolean isExportAll() {
		return exportAll;
	}

	public void setExportAll(boolean exportAll) {
		this.exportAll = exportAll;
	}

	public void setSkipFiles(boolean skipFiles) {
		this.skipFiles = skipFiles;
	}
}
