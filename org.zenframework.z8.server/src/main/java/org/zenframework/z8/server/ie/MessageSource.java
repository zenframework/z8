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
import org.zenframework.z8.server.base.table.Table.CLASS;
import org.zenframework.z8.server.base.table.system.Users;
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
	// MARK - в новой версии serialVersionUID кончается на 9, а не 8
	private static final long serialVersionUID = -145929531248527278L;

	private static class Cache {
		final Map<String, Table> tables = new HashMap<String, Table>();
		final Map<String, Field> fields = new HashMap<String, Field>();
		final Map<guid, Map<String, string>> attachments = new HashMap<>();

		void clear() {
			tables.clear();
			fields.clear();
			attachments.clear();
		}

		boolean exists(guid id) {
			return attachments.containsKey(id);
		}
	}

	protected boolean exportAll;
	protected ExportRules exportRules = new ExportRules();
	protected Collection<ExportSource> sources = new ArrayList<ExportSource>();
	protected Map<String, primary> properties = new HashMap<String, primary>();

	protected Collection<RecordInfo> inserts = new ArrayList<RecordInfo>();
	protected Collection<RecordInfo> updates = new ArrayList<RecordInfo>();

	protected Collection<file> files = new ArrayList<file>();

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

	public Collection<ExportSource> getSources() {
		return sources;
	}

	public Collection<RecordInfo> getInserts() {
		return inserts;
	}

	public Collection<RecordInfo> getUpdates() {
		return updates;
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
			readExisting(record, cache);

		for(RecordInfo record : updates)
			readExisting(record, cache);

		for(RecordInfo record : inserts)
			insert(record, cache);

		ConnectionManager.get().flush();

		for(RecordInfo record : inserts)
			update(record, cache);

		for(RecordInfo record : updates)
			update(record, cache);

		cache.clear();

		ApplicationServer.restoreEventsLevel();
	}

	private Table getTable(String name, Cache cache) {
		Table table = cache.tables.get(name);

		if(table == null) {
			CLASS<? extends Table> cls = Runtime.instance().getTableByName(name);
			if (cls == null)
				return null;
			table = (Table)cls.newInstance();
			cache.tables.put(name, table);
		}

		return table;
	}

	private Field getField(String tableName, String fieldName, Cache cache) {
		String key = tableName + "." + fieldName;

		Field field = cache.fields.get(key);

		if(field == null) {
			field = getTable(tableName, cache).getFieldByName(fieldName);
			cache.fields.put(key, field);
		}

		return field;
	}

	private void readExisting(RecordInfo record, Cache cache) {
		guid recordId = record.id();
		if (cache.exists(recordId))
			return;

		String tableName = record.table();
		Table table = getTable(tableName, cache);
		if (table == null)
			return;

		Collection<Field> attachments = table.attachments();
		boolean exists = table.readRecord(recordId, attachments);

		if (exists) {
			Map<String, string> recAtts = new HashMap<>();
			for (Field f : attachments)
				recAtts.put(f.name(), f.string());
			cache.attachments.put(recordId, recAtts);
		}
	}

	private void insert(RecordInfo record, Cache cache) {
		guid recordId = record.id();
		if (cache.exists(recordId))
			return;

		String tableName = record.table();
		Table table = getTable(tableName, cache);
		if (table == null)
			return;

		for(FieldInfo fieldInfo : record.fields()) {
			String fieldName = fieldInfo.name();

			Field field = getField(tableName, fieldName, cache);

			if(field == null)
				throw new RuntimeException("Incorrect record format. Table '" + tableName + "' has no field '" + fieldName + "'");

			if (field.unique())
				field.set(fieldInfo.value());
		}

		table.create(recordId);
	}

	private void update(RecordInfo record, Cache cache) {
		String tableName = record.table();
		Table table = getTable(tableName, cache);
		guid recordId = record.id();

		boolean exists = cache.exists(recordId);

		for(FieldInfo fieldInfo : record.fields()) {
			String fieldName = fieldInfo.name();

			if(exists) {
				ImportPolicy fieldPolicy = exportRules.getPolicy(tableName, fieldName, recordId);
				if(fieldPolicy != ImportPolicy.OVERRIDE)
					continue;
			}

			Field field = getField(tableName, fieldName, cache);

			if(field == null)
				throw new RuntimeException("Incorrect record format. Table '" + tableName + "' has no field '" + fieldName + "'");

			if(exists && field instanceof FileField) {
				string wasValue = cache.attachments.get(recordId).get(field.name());
				field.set(mergeAttachments(wasValue, (string)fieldInfo.value()));
			} else
				field.set(fieldInfo.value());
		}

		if (table instanceof Users)
			((Users)table).setNotifyBlock(true);
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

	public void setExportRules(ExportRules exportRules) {
		this.exportRules = exportRules;
	}

	public void setSources(Collection<ExportSource> sources) {
		this.sources = sources;
	}

	public void setProperties(Map<String, primary> properties) {
		this.properties = properties;
	}

	public void setInserts(Collection<RecordInfo> inserts) {
		this.inserts = inserts;
	}

	public void setUpdates(Collection<RecordInfo> updates) {
		this.updates = updates;
	}

	public void setFiles(Collection<file> files) {
		this.files = files;
	}
}
