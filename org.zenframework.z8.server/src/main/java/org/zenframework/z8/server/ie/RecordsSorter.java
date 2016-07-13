package org.zenframework.z8.server.ie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;

public class RecordsSorter {

	private final Map<Record, Collection<Record>> recordsRelations = new HashMap<Record, Collection<Record>>();
	private int count = 0;

	public void addLink(String sourceTable, guid sourceRecordId, String targetTable, guid targetRecordId) {
		Record source = new Record(sourceTable, sourceRecordId);
		Record target = new Record(targetTable, targetRecordId);
		Collection<Record> rels = recordsRelations.get(source);
		if (rels == null) {
			rels = new LinkedList<Record>();
			recordsRelations.put(source, rels);
		}
		if (!rels.contains(target)) {
			rels.add(target);
		}
	}

	public Collection<Record> getLinks(Record record) {
		Collection<Record> links = recordsRelations.get(record);
		if (links != null) {
			return links;
		} else {
			return Collections.emptyList();
		}
	}

	public boolean contains(String table, guid recordId) {
		return recordsRelations.containsKey(new Record(table, recordId));
	}

	public void addRecord(String table, guid recordId) {
		Record record = new Record(table, recordId);
		if (!recordsRelations.containsKey(record)) {
			recordsRelations.put(record, new LinkedList<Record>());
		}
	}

	public List<Record> getUnsorted() {
		return new ArrayList<RecordsSorter.Record>(recordsRelations.keySet());
	}

	public List<Record> getSorted() throws SorterException {
		count = 0;
		Trace.logEvent("Export tree: " + recordsRelations);
		// Последовательно ищем вершины ориентированного графа, не имеющие исходящих ребёр (связей с другими записями),
		// исключаем их из исходного списка relations и добавляем в sorted.
		// Если не удалётся найти подходящую вершину - ошибка - такой ситуации происходить не должно!
		Map<Record, Collection<Record>> recordsRelations = new HashMap<Record, Collection<Record>>(this.recordsRelations);
		List<Record> sorted = new LinkedList<Record>();
		while (!recordsRelations.isEmpty()) {
			// Ищем конечную запись и помещаем её в конец отсортированного списка
			sorted.addAll(findOutside(recordsRelations));
		}
		return sorted;
	}

	public Comparator<ExportEntry.Records.Record> getComparator() throws SorterException {
		return new RecordsComparator(getSorted());
	}

	public int getCount() {
		return count;
	}

	private Collection<Record> findOutside(Map<Record, Collection<Record>> recordsRelations) throws SorterException {
		Collection<Record> outsideRecords = new LinkedList<Record>();
		Iterator<Map.Entry<Record, Collection<Record>>> it = recordsRelations.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Record, Collection<Record>> entry = it.next();
			if (entry.getValue().isEmpty()) {
				outsideRecords.add(entry.getKey());
				it.remove();
			}
			count++;
		}
		if (outsideRecords.isEmpty()) {
			throw new SorterException(
					"Ошибка экспорта: не удалось найти конечную запись. В базе данных есть циклическая зависимость. Обратитесь к системному администратору.\n"
							+ recordsToString(recordsRelations));
		}
		for (Collection<Record> relations : recordsRelations.values()) {
			relations.removeAll(outsideRecords);
			count++;
		}
		return outsideRecords;
	}

	public void printRecords() {
		System.out.println(recordsToString(recordsRelations));
	}

	public static SortingMode getSortingMode() {
		return SortingMode.valueOf(ServerConfig.get("z8.transport.recordsSortingMode", SortingMode.ALWAYS.toString())/*Properties.getProperty(ServerRuntime.RecordsSortingModeProperty).toUpperCase()*/);
	}

	private String recordsToString(Map<Record, Collection<Record>> recordsRelations) {
		StringBuilder str = new StringBuilder();
		for (Map.Entry<Record, Collection<Record>> entry : recordsRelations.entrySet()) {
			str.append(entry.getKey()).append(" --> ").append(entry.getValue()).append('\n');
		}
		return str.toString();
	}

	public static class Record {

		final String table;
		final guid recordId;

		Record(String table, guid recordId) {
			this.table = table;
			this.recordId = recordId;
		}

		@Override
		public int hashCode() {
			return table.hashCode() + recordId.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Record) {
				Record r = (Record) obj;
				return table.equals(r.table) && recordId.equals(r.recordId);
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return table + '[' + recordId + ']';
		}

	}

	public static enum SortingMode {

		ON_EXPORT(true, false), ON_IMPORT(false, true), ALWAYS(true, true);

		public final boolean onExport;
		public final boolean onImport;

		private SortingMode(boolean onExport, boolean onImport) {
			this.onExport = onExport;
			this.onImport = onImport;
		}

	}

	private static class RecordsComparator implements Comparator<ExportEntry.Records.Record> {

		private final List<Record> sorted;

		private RecordsComparator(List<Record> sorted) {
			this.sorted = sorted;
		}

		@Override
		public int compare(ExportEntry.Records.Record r1, ExportEntry.Records.Record r2) {
			int n1 = sorted.indexOf(new Record(r1.getTable(), new guid(r1.getRecordId())));
			int n2 = sorted.indexOf(new Record(r2.getTable(), new guid(r2.getRecordId())));
			return (n1 > n2) ? 1 : (n1 == n2 ? 0 : -1);
		}

	}

}
