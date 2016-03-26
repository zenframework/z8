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
import org.zenframework.z8.server.base.table.value.AttachmentField;
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
	public static final String REMOTE_PROTOCOL = "local";

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

	private final Map<String, ImportPolicy> policies = new HashMap<String, ImportPolicy>();
	private final List<RecordsetEntry> recordsetEntries = new LinkedList<RecordsetEntry>();
	private boolean exportAttachments = false;
	private String exportUrl;
	private int exportRecordsMax = Integer.parseInt(Properties.getProperty(ServerRuntime.ExportRecordsMaxProperty));

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

	public void setPolicy(Table table, ImportPolicy policy) {
		policies.put(table.classId(), policy);
	}

	public void setPolicy(String tableClass, ImportPolicy policy) {
		policies.put(tableClass, policy);
	}

	public ImportPolicy getPolicy(Table table) {
		return getPolicy(table.classId());
	}

	public ImportPolicy getPolicy(String tableClass) {
		return policies.containsKey(tableClass) ? policies.get(tableClass) : ImportPolicy.DEFAULT;
	}

	public boolean isExportAttachments() {
		return exportAttachments;
	}

	public void setExportAttachments(boolean exportAttachments) {
		this.exportAttachments = exportAttachments;
	}

	public String getExportUrl() {
		return exportUrl;
	}

	public void setExportUrl(String exportUrl) {
		this.exportUrl = exportUrl;
	}

	public void setExportRecordsMax(int exportRecordsMax) {
		this.exportRecordsMax = exportRecordsMax;
	}

	public void execute() {
		RecordsSorter recordsSorter = new RecordsSorter();
		List<ExportEntry.Records.Record> records = new LinkedList<ExportEntry.Records.Record>();
		List<ExportEntry.Files.File> files = new LinkedList<ExportEntry.Files.File>();
		List<ExportEntry.Properties.Property> props = new LinkedList<ExportEntry.Properties.Property>();

		try {
			if (!LOCAL_PROTOCOL.equals(getProtocol())) {
				// Если протокол НЕ "null", экспортировать записи БД
				for (RecordsetEntry recordsetEntry : recordsetEntries) {
					while (recordsetEntry.recordset.next()) {
						exportRecord(recordsetEntry, records, files, getPolicy(recordsetEntry.recordset.classId()),
								recordsSorter, exportRecordsMax, 0);
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
					ExportEntry.Properties.Property property = new ExportEntry.Properties.Property();
					property.setKey(entry.getKey().get());
					property.setValue(entry.getValue().toString());
					property.setType(entry.getValue().type().toString());
					props.add(property);
				}
			}
			// Запись сообщений в таблицу ExportMessages
			String sender = context.get().getProperty(TransportContext.SelfAddressProperty);
			boolean sendFilesSeparately = Boolean.parseBoolean(Properties
					.getProperty(ServerRuntime.SendFilesSeparatelyProperty));
			ExportMessages exportMessages = ExportMessages.instance();
			if (sendFilesSeparately) {
				for (ExportEntry.Files.File file : files) {
					Message message = Message.instance();
					message.setAddress(getAddress());
					message.setSender(sender);
					message.getExportEntry().getFiles().getFile().add(file);
					exportMessages.addMessage(message, null);
				}
			}
			Message message = Message.instance();
			message.setAddress(getAddress());
			message.setSender(sender);
			message.getExportEntry().getRecords().getRecord().addAll(records);
			message.getExportEntry().getProperties().getProperty().addAll(props);
			if (!sendFilesSeparately) {
				message.getExportEntry().getFiles().getFile().addAll(files);
			}
			exportMessages.addMessage(message, null);
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

	public void z8_setPolicy(Table.CLASS<? extends Table> cls, ImportPolicy policy) {
		setPolicy(cls.get(), policy);
	}

	public void z8_setExportAttachments(bool exportAttachments) {
		setExportAttachments(exportAttachments.get());
	}

	public void z8_setExportUrl(string exportUrl) {
		setExportUrl(exportUrl.get());
	}

	public void z8_setExportRecordsMax(integer exportRecordsMax) {
		setExportRecordsMax(exportRecordsMax.getInt());
	}

	public void z8_init() {}

	public void z8_execute() {
		execute();
	}

	public static string z8_getExportUrl(string protocol, string address) {
		return new string(getExportUrl(protocol.get(), address.get()));
	}

	private boolean exportRecord(RecordsetEntry recordsetEntry, List<ExportEntry.Records.Record> records,
			List<ExportEntry.Files.File> files, ImportPolicy policy, RecordsSorter recordsSorter, int exportRecordsMax,
			int level) {
		checkExportRecordsMax(records, exportRecordsMax);
		guid recordId = recordsetEntry.recordset.recordId();
		String table = recordsetEntry.recordset.classId();
		if (!recordsSorter.contains(table, recordId) && !IeUtil.isBuiltinRecord(recordsetEntry.recordset, recordId)) {
			if (policies.containsKey(recordsetEntry.recordset.classId())) {
				policy = policies.get(recordsetEntry.recordset.classId());
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug(getSpaces(level) + "Export record " + recordsetEntry.recordset.name() + '[' + recordId + ']');
			}
			ExportEntry.Records.Record record = IeUtil.tableToRecord(recordsetEntry.recordset, recordsetEntry.fields,
					policy == null ? null : policy.getSelfPolicy(), isExportAttachments());
			records.add(record);
			recordsSorter.addRecord(table, recordId);
			// Вложения
			if (isExportAttachments()) {
				for (AttachmentField attField : recordsetEntry.recordset.getAttachments()) {
					List<FileInfo> fileInfos = FileInfo.parseArray(attField.get().string().get());
					files.addAll(IeUtil.fileInfosToFiles(fileInfos, policy == null ? null : policy.getSelfPolicy()));
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
						if (exportRecord(new RecordsetEntry(refRecord, refRecord.getPrimaryFields()), records, files,
								policy == null ? null : policy.getRelationsPolicy(), recordsSorter, exportRecordsMax,
								level + 1)) {
							recordsSorter.addLink(table, recordId, refRecord.classId(), refGuid);
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
						if (exportRecord(new RecordsetEntry(parentRecord, parentRecord.getPrimaryFields()), records, files,
								policy == null ? null : policy.getRelationsPolicy(), recordsSorter, exportRecordsMax,
								level + 1)) {
							recordsSorter.addLink(table, recordId, table, parentId);
						}
					}
				}
			}
			return true;
		}
		return false;
	}

	private String getProtocol() {
		int pos = exportUrl.indexOf(':');
		return pos > 0 ? exportUrl.substring(0, pos) : LOCAL_PROTOCOL;
	}

	private String getAddress() {
		int pos = exportUrl.indexOf(':');
		return pos > 0 ? exportUrl.substring(pos + 1) : null;
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
