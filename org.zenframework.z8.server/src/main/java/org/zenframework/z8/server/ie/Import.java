package org.zenframework.z8.server.ie;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.file.InputOnlyFileItem;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.SystemDomains;
import org.zenframework.z8.server.base.table.system.SystemFiles;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Import {

	private static JsonObject STRUCTURE = null;

	public static boolean importObject(Object object) {
		if(object instanceof Message) {
			return Import.importMessage((Message)object);
		} else if(object instanceof file) {
			return Import.importFile((file)object);
		}
		throw new RuntimeException("Unsupported object type '" + (object != null ? object.getClass().getCanonicalName() : "null"));
	}
	
	public static boolean importFile(file file) {
		File target = FileUtils.getFile(Folders.Base, Folders.Temp, file.path.get());

		long offset = file.offset();

		if(offset == 0)
			target.delete();
		else if(!target.exists() || target.length() > offset)
			return false;
		
		try {
			if(!file.addPartTo(target))
				return true;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		try {
			SystemFiles files = SystemFiles.newInstance();
			file.set(new InputOnlyFileItem(target, file.name.get()));

			if(!files.hasRecord(file.id))
				files.addFile(file);
			else
				files.updateFile(file);

			return true;
		} finally {
			target.delete();
		}
	}
	
	public static boolean importMessage(Message message) {
		IUser user = SystemDomains.newInstance().getDomain(message.getAddress()).getSystemUser();

		IRequest request = new Request(new Session("", user));

		ApplicationServer.setRequest(request);

		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			message.processed();

			message.beforeImport();
			ApplicationServer.disableEvents();
			
			try {
				importRecords(message);
				importFiles(message);
			} finally {
				ApplicationServer.enableEvents();
			}
			
			message.afterImport();
			
			connection.commit();
			
			return true;
		} catch (Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		} finally {
			ConnectionManager.release();
			ApplicationServer.setRequest(null);
		}
	}

	public static void importRecords(Message message) throws ImportException {

		// Обработка специальных свойств
		Map<string, primary> properties = message.getProperties();
		string type = properties.containsKey(Message.PROP_TYPE) ? properties.get(Message.PROP_TYPE).string() : null;
		if (Message.TYPE_FILE_REQUEST.equals(type)) {
			guid id = properties.get(Message.PROP_RECORD_ID).guid();
			String path = properties.get(Message.PROP_FILE_PATH).string().get();
			SystemFiles.sendFile(new file(id, null, null, path), message.getSender());
		}

		// Сортировка записей
		if (RecordsSorter.getSortingMode().onImport) {
			RecordsSorter sorter = new RecordsSorter();
			for (ExportEntry.Records.Record record : message.getExportEntry().getRecords().getRecord()) {
				guid recordId = new guid(record.getRecordId());
				sorter.addRecord(record.getTable(), recordId);
				for (ExportEntry.Records.Record.Field field : record.getField()) {
					JsonObject tableInfo = getStructure().getJsonObject(record.getTable());
					if (tableInfo == null)
						throw new ImportException("No structure for '" + record.getTable() + "'");
					if (tableInfo.has(field.getId())) {
						String linkTableName = tableInfo.getString(field.getId());
						Table linkTable = (Table) Loader.getInstance(record.getTable());
						guid linkId = new guid(field.getValue());
						if (!IeUtil.isBuiltinRecord(linkTable, linkId)) {
							sorter.addLink(record.getTable(), recordId, linkTableName, linkId);
						}
					}
				}
			}
			try {
				Collections.sort(message.getExportEntry().getRecords().getRecord(), sorter.getComparator());
			} catch (SorterException e) {
				throw new ImportException(e.getMessage(), e);
			}
		}

		// Импорт записей
		for (ExportEntry.Records.Record record : message.getExportEntry().getRecords().getRecord())
			updateTableRecord((Table) Loader.getInstance(record.getTable()), record);

	}

	public static void importFiles(Message message) throws IOException {
		SystemFiles files = SystemFiles.newInstance();

		for (file file : message.getFiles()) {
			if (file.get() == null)
				file = files.getFile(file);
			files.addFile(file);
		}
	}

	private static void updateTableRecord(Table table, ExportEntry.Records.Record record) throws ImportException {

		guid recordId = new guid(record.getRecordId());

		if (IeUtil.isBuiltinRecord(table, recordId))
			return;

		String policy = record.getPolicy();
		ImportPolicy recordPolicy = ImportPolicy.getPolicy(policy);

		Collection<Field> aggregatedFields = new LinkedList<Field>();
		for (ExportEntry.Records.Record.Field xmlField : record.getField()) {
			Field field = table.getFieldById(xmlField.getId());
			if (field != null) {
				policy = xmlField.getPolicy();
				if (policy == null || policy.isEmpty())
					policy = field.importPolicy();
				if (ImportPolicy.getPolicy(policy, recordPolicy) == ImportPolicy.AGGREGATE)
					aggregatedFields.add(field);
			}
		}

		boolean exists = table.readRecord(recordId, aggregatedFields);

		boolean hasUpdatedFields = false;
		for (ExportEntry.Records.Record.Field xmlField : record.getField()) {
			Field field = table.getFieldById(xmlField.getId());
			if (field != null) {
				policy = xmlField.getPolicy();
				if (policy == null || policy.isEmpty())
					policy = field.importPolicy();
				ImportPolicy fieldPolicy = ImportPolicy.getPolicy(policy, recordPolicy);
				if (!exists || fieldPolicy == ImportPolicy.OVERRIDE) {
					field.set(primary.create(field.type(), xmlField.getValue()));
					hasUpdatedFields = true;
				} else if (exists && fieldPolicy == ImportPolicy.AGGREGATE) {
					if (field.aggregator == null)
						throw new ImportException("Can't aggregate " + field.id()
								+ " values. Field aggregator is not defined");
					field.set(field.aggregator.get().aggregate(field.get(),
							primary.create(field.type(), xmlField.getValue())));
					hasUpdatedFields = true;
				}
			} else {
				Trace.logEvent("WARNING: Incorrect record format. Table '" + table.classId() + "' has no field '"
						+ xmlField.getId() + "'");
			}
		}

		if (!exists) {
			// Если запись не существует, создать
			table.create(recordId);
		} else if (hasUpdatedFields) {
			// Если запись должна быть обновлена согласно политике, обновить
			table.update(recordId);
		}
	}

	private static JsonObject getStructure() {
		if (STRUCTURE == null) {
			STRUCTURE = getTablesStructure();
		}
		return STRUCTURE;
	}

	public static JsonObject getTablesStructure() {
		JsonObject structure = new JsonObject();
		for (Table.CLASS<? extends Table> tableClass : Runtime.instance().tables()) {
			fillStructure(structure, tableClass);
		}
		return structure;
	}

	@SuppressWarnings("unchecked")
	private static void fillStructure(JsonObject structure, Table.CLASS<? extends Table> tableClass) {
		if (!structure.has(tableClass.classId())) {
			JsonObject jsonTable = new JsonObject();
			structure.put(tableClass.classId(), jsonTable);
			Table table = tableClass.newInstance();
			for (IForeignKey fkey : table.getForeignKeys()) {
				String fieldIndex = table.getFieldByName(fkey.getFieldDescriptor().name()).getIndex();
				jsonTable.put(fieldIndex, ((Table) fkey.getReferencedTable()).classId());
				fillStructure(structure,
						(Table.CLASS<? extends Table>) Loader.loadClass(((Table) fkey.getReferencedTable()).classId()));
			}
			Field parentKey = table.parentKey();
			if (parentKey != null) {
				jsonTable.put(parentKey.getIndex(), tableClass.classId());
			}
		}
	}

}
