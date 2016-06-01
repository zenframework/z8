package org.zenframework.z8.server.ie;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Export extends OBJECT {

	private static final Log LOG = LogFactory.getLog(Export.class);

	public static final String LOCAL_PROTOCOL = "local";
	public static final String REMOTE_PROTOCOL = "remote";

	public static class CLASS<T extends Export> extends OBJECT.CLASS<T> {

		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Export.class);
			setName(Export.class.getName());
			setDisplayName(Export.class.getName());
		}

		@Override
		public Object newObject(IObject container) {
			return new Export(container);
		}

	}

	public final RLinkedHashMap<string, primary> properties = new RLinkedHashMap<string, primary>();

	protected final TransportContext.CLASS<TransportContext> context = new TransportContext.CLASS<TransportContext>();

	private final Map<String, RecordsetExportRules> exportRules = new HashMap<String, RecordsetExportRules>();
	private final List<RecordsetEntry> recordsetEntries = new LinkedList<RecordsetEntry>();
	private final RecordsetExportRules defaultExportRules = new RecordsetExportRules.CLASS<RecordsetExportRules>().get();

	private final List<ExportEntry.Records.Record> records = new LinkedList<ExportEntry.Records.Record>();
	private final List<ExportEntry.Files.File> files = new LinkedList<ExportEntry.Files.File>();
	private final List<ExportEntry.Properties.Property> props = new LinkedList<ExportEntry.Properties.Property>();

	private String exportUrl;
	private int exportRecordsMax = Integer.parseInt(Properties.getProperty(ServerRuntime.ExportRecordsMaxProperty));
	private boolean sendFilesSeparately = Boolean.parseBoolean(Properties
			.getProperty(ServerRuntime.SendFilesSeparatelyProperty));
	private boolean sendFilesContent = !Boolean.parseBoolean(Properties.getProperty(ServerRuntime.LazyFilesProperty));

	public Export(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();
		z8_init();
	}

	public void addRecordset(Table table) {
		table.read(table.getPrimaryFields());
		recordsetEntries.add(new RecordsetEntry(table, table.getPrimaryFields()));
	}

	public void addRecordset(Table table, Collection<Field> fields) {
		table.read(fields);
		recordsetEntries.add(new RecordsetEntry(table, fields));
	}

	public void addRecordset(Table table, sql_bool where) {
		table.read(table.getPrimaryFields(), where);
		recordsetEntries.add(new RecordsetEntry(table, table.getPrimaryFields()));
	}

	public void addRecordset(Table table, Collection<Field> fields, sql_bool where) {
		table.read(fields, where);
		recordsetEntries.add(new RecordsetEntry(table, fields));
	}

	public void addFile(FileInfo fileInfo) {
		addFile(fileInfo, ImportPolicy.DEFAULT);
	}

	public void addFile(FileInfo fileInfo, ImportPolicy importPolicy) {
		files.add(IeUtil.fileInfoToFile(fileInfo, importPolicy,
				context.get().getProperty(TransportContext.SelfAddressProperty)));
	}

	public void setDefaults(ImportPolicy importPolicy, boolean exportAttachments) {
		defaultExportRules.setImportPolicy(importPolicy);
		defaultExportRules.setExportAttachments(exportAttachments);
	}

	public void setExportRules(Table table, RecordsetExportRules exportRules) {
		this.exportRules.put(getTableClassId(table), exportRules);
	}

	public String getExportUrl() {
		return exportUrl;
	}

	public String getProtocol() {
		int pos = exportUrl.indexOf(':');
		return pos > 0 ? exportUrl.substring(0, pos) : LOCAL_PROTOCOL;
	}

	public String getAddress() {
		int pos = exportUrl.indexOf(':');
		return pos > 0 ? exportUrl.substring(pos + 1) : null;
	}

	public void setExportUrl(String exportUrl) {
		this.exportUrl = exportUrl;
	}

	public void setExportRecordsMax(int exportRecordsMax) {
		this.exportRecordsMax = exportRecordsMax;
	}

	public void setSendFilesSeparately(boolean sendFilesSeparately) {
		this.sendFilesSeparately = sendFilesSeparately;
	}

	public void setSendFilesContent(boolean sendFilesContent) {
		this.sendFilesContent = sendFilesContent;
	}

	public void execute() {
		RecordsSorter recordsSorter = new RecordsSorter();
		try {
			String exportProtocol = getProtocol();
			boolean local = LOCAL_PROTOCOL.equals(exportProtocol);
			String sender = context.get().getProperty(TransportContext.SelfAddressProperty);
			if (!local) {
				// Если протокол НЕ "local", экспортировать записи БД
				for (RecordsetEntry recordsetEntry : recordsetEntries) {
					while (recordsetEntry.recordset.next()) {
						exportRecord(recordsSorter, recordsetEntry, sender, 0);
					}
				}
				if (RecordsSorter.getSortingMode().onExport) {
					// Сортировка записей в соответствии со ссылками по foreign keys и parentId
					Collections.sort(records, recordsSorter.getComparator());
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug(records.size() + " records sorted in " + recordsSorter.getCount() + " steps:");
					for (ExportEntry.Records.Record record : records) {
						LOG.debug(record.getTable() + '[' + record.getRecordId() + ']');
					}
				}
			}
			// Свойства экспортируются для любого протокола
			if (!properties.isEmpty()) {
				for (Map.Entry<string, primary> entry : this.properties.entrySet()) {
					props.add(getProperty(entry.getKey(), entry.getValue()));
				}
			}
			// Запись сообщений в таблицу ExportMessages
			ExportMessages exportMessages = new ExportMessages.CLASS<ExportMessages>().get();
			if (sendFilesSeparately && !local) {
				for (ExportEntry.Files.File file : files) {
					Message message = Message.instance();
					message.setAddress(getAddress());
					message.setSender(sender);
					message.setExportProtocol(exportProtocol);
					message.getExportEntry().getFiles().getFile().add(file);
					if (sendFilesContent)
						message.getExportEntry().getProperties().getProperty()
								.add(getProperty(Message.PROP_SEND_FILES_CONTENT, new bool(true)));
					exportMessages.addMessage(message, null, ExportMessages.Direction.OUT);
				}
			}
			Message message = Message.instance();
			message.setAddress(getAddress());
			message.setSender(sender);
			message.setExportProtocol(exportProtocol);
			message.getExportEntry().getRecords().getRecord().addAll(records);
			message.getExportEntry().getProperties().getProperty().addAll(props);
			if (!sendFilesSeparately && !local) {
				message.getExportEntry().getFiles().getFile().addAll(files);
				message.getExportEntry().getProperties().getProperty()
						.add(getProperty(Message.PROP_SEND_FILES_CONTENT, new bool(true)));
			}
			exportMessages.addMessage(message, null, ExportMessages.Direction.OUT);
		} catch (JAXBException e) {
			throw new exception("Can't marshal records", e);
		}
	}

	public static String getExportUrl(String protocol, String address) {
		return protocol + ":" + address;
	}

	public void z8_addRecordset(Table.CLASS<? extends Table> cls) {
		addRecordset(cls.get());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_addRecordset(Table.CLASS<? extends Table> cls, RCollection fields) {
		addRecordset(cls.get(), CLASS.asList(fields));
	}

	public void z8_addRecordset(Table.CLASS<? extends Table> cls, sql_bool where) {
		addRecordset(cls.get(), where);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_addRecordset(Table.CLASS<? extends Table> cls, RCollection fields, sql_bool where) {
		addRecordset(cls.get(), CLASS.asList(fields), where);
	}

	public void z8_addFile(FileInfo.CLASS<? extends FileInfo> fileInfo) {
		addFile(fileInfo.get());
	}

	public void z8_addFile(FileInfo.CLASS<? extends FileInfo> fileInfo, ImportPolicy importPolicy) {
		addFile(fileInfo.get(), importPolicy);
	}

	public void z8_setDefaults(ImportPolicy importPolicy, bool exportAttachments) {
		setDefaults(importPolicy, exportAttachments.get());
	}

	public void z8_setExportRules(Table.CLASS<? extends Table> cls,
			RecordsetExportRules.CLASS<? extends RecordsetExportRules> exportRules) {
		setExportRules(cls.get(), exportRules.get());
	}

	public void z8_setExportUrl(string exportUrl) {
		setExportUrl(exportUrl.get());
	}

	public void z8_setExportRecordsMax(integer exportRecordsMax) {
		setExportRecordsMax(exportRecordsMax.getInt());
	}

	public void z8_setSendFilesSeparately(bool sendFilesSeparately) {
		setSendFilesSeparately(sendFilesSeparately.get());
	}

	public void z8_setSendFilesContent(bool sendFilesContent) {
		setSendFilesContent(sendFilesContent.get());
	}

	public void z8_init() {}

	public void z8_execute() {
		execute();
	}

	public static string z8_getExportUrl(string protocol, string address) {
		return new string(getExportUrl(protocol.get(), address.get()));
	}

	private boolean exportRecord(RecordsSorter recordsSorter, RecordsetEntry recordsetEntry, String sender, int level) {
		checkExportRecordsMax(records, exportRecordsMax);
		guid recordId = recordsetEntry.recordset.recordId();
		String table = getTableClassId(recordsetEntry.recordset);
		RecordsetExportRules recordsetExportRules = exportRules.containsKey(table) ? exportRules.get(table)
				: defaultExportRules;
		if (!recordsSorter.contains(table, recordId) && !IeUtil.isBuiltinRecord(recordsetEntry.recordset, recordId)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(getSpaces(level) + "Export record " + recordsetEntry.recordset.name() + '[' + recordId + ']');
			}
			ExportEntry.Records.Record record = IeUtil.getRecord(table, recordId, recordsetEntry.fields,
					recordsetExportRules, sender);
			records.add(record);
			recordsSorter.addRecord(table, recordId);
			// Вложения
			for (Field attField : recordsetEntry.recordset.getAttachments()) {
				if (recordsetExportRules.isExportAttachments(recordId, attField)) {
					List<FileInfo> fileInfos = FileInfo.parseArray(attField.get().string().get());
					files.addAll(IeUtil.fileInfosToFiles(fileInfos,
							recordsetExportRules.getImportPolicy(recordId, attField), sender));
				}
			}
			// Ссылки на другие таблицы
			// TODO Переделать через links, проверять атрибут exportable
			for (IForeignKey fkey : recordsetEntry.recordset.getForeignKeys()) {
				if (fkey.getReferencedTable() instanceof Table) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(getSpaces(level + 1)
								+ fkey.getFieldDescriptor().name()
								+ " --> "
								+ fkey.getReferencedTable().name()
								+ '['
								+ recordsetEntry.recordset.getFieldByName(fkey.getFieldDescriptor().name()).guid()
										.toString() + ']');
					}
					Table refRecord = (Table) Loader.getInstance(((Table) fkey.getReferencedTable()).classId());
					guid refGuid = recordsetEntry.recordset.getFieldByName(fkey.getFieldDescriptor().name()).guid();
					if (refRecord.readRecord(refGuid, refRecord.getPrimaryFields())) {
						if (exportRecord(recordsSorter, new RecordsetEntry(refRecord, refRecord.getPrimaryFields()), sender,
								level + 1)) {
							recordsSorter.addLink(table, recordId, getTableClassId(refRecord), refGuid);
						}
					}
				}
			}
			if (recordsetEntry.recordset instanceof TreeTable) {
				TreeTable treeRecordSet = (TreeTable) recordsetEntry.recordset;
				guid parentId = treeRecordSet.parentId.get().guid();
				if (!guid.NULL.equals(parentId)) {
					TreeTable parentRecord = (TreeTable) Loader.getInstance(recordsetEntry.recordset.classId());
					if (parentRecord.readRecord(parentId, parentRecord.getPrimaryFields())) {
						if (exportRecord(recordsSorter, new RecordsetEntry(parentRecord, parentRecord.getPrimaryFields()),
								sender, level + 1)) {
							recordsSorter.addLink(table, recordId, table, parentId);
						}
					}
				}
			}
			return true;
		}
		return false;
	}

	private static String getSpaces(int level) {
		char buf[] = new char[level * 4];
		Arrays.fill(buf, ' ');
		return new String(buf);
	}

	private static void checkExportRecordsMax(List<ExportEntry.Records.Record> records, int exportRecordsMax) {
		if (records.size() > exportRecordsMax) {
			File file = null;
			try {
				ExportEntry exportEntry = new ExportEntry();
				exportEntry.setRecords(new ExportEntry.Records());
				exportEntry.getRecords().getRecord().addAll(records);
				file = File.createTempFile("z8-export-", ".xml");
				IeUtil.marshalExportEntry(exportEntry, new FileWriter(file));
			} catch (Throwable e) {
				LOG.error("Can't write export entry to file", e);
			}
			throw new RuntimeException("Export of " + records.size() + " records (max " + exportRecordsMax + ") failed."
					+ (file != null ? " Export entry is written to '" + file + "'" : ""));
		}
	}

	private static String getTableClassId(Table table) {
		// TODO Find [generatable] class 
		//Class<?> cls = table.getClass();
		//while (cls != null && cls.getSuperclass() != Table.class)
		//	cls = cls.getSuperclass();
		//return (cls == null ? table.getClass() : cls).getCanonicalName();
		return table.classId();
	}

	private static ExportEntry.Properties.Property getProperty(string key, primary value) {
		ExportEntry.Properties.Property property = new ExportEntry.Properties.Property();
		property.setKey(key.get());
		property.setValue(value.toString());
		property.setType(value.type().toString());
		return property;
	}

	private static class RecordsetEntry {

		final Table recordset;
		final Collection<Field> fields;

		RecordsetEntry(Table recordset, Collection<Field> fields) {
			this.recordset = recordset;
			this.fields = fields;
		}

		@Override
		public String toString() {
			return recordset.name();
		}

	}

}
