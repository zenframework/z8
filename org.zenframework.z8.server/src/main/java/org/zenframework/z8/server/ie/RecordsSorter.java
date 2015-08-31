package org.zenframework.z8.server.ie;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;

public class RecordsSorter {

    private final Map<Record, Collection<Record>> recordsRelations = new HashMap<Record, Collection<Record>>();

    public void addLink(String sourceTable, guid sourceRecordId, String targetTable, guid targetRecordId) {
        Record source = new Record(sourceTable, sourceRecordId);
        Record target = new Record(targetTable, targetRecordId);
        Collection<Record> rels = recordsRelations.get(target);
        if (rels == null) {
            rels = new LinkedList<Record>();
            recordsRelations.put(target, rels);
        }
        if (!rels.contains(source)) {
            rels.add(source);
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

    public List<Record> getSorted() {
        Trace.logEvent("Export tree: " + recordsRelations);
        // Последовательно ищем вершины ориентированного графа, не имеющие исходящих ребёр (связей с другими записями),
        // исключаем их из исходного списка relations и добавляем в sorted.
        // Если не удалётся найти подходящую вершину - ошибка - такой ситуации происходить не должно!
        Map<Record, Collection<Record>> recordsRelations = new HashMap<Record, Collection<Record>>(this.recordsRelations);
        List<Record> sorted = new LinkedList<Record>();
        while (!recordsRelations.isEmpty()) {
            // Ищем конечную запись
            Record outside = findOutside(recordsRelations);
            // Удаляем её из исходного списка и помещаем в конец отсортированного списка
            recordsRelations.remove(outside);
            sorted.add(outside);
        }
        return sorted;
    }

    public Comparator<ExportEntry.Records.Record> getComparator() {
        return new RecordsComparator(getSorted());
    }

    private static Record findOutside(Map<Record, Collection<Record>> recordsRelations) {
        for (Record record : recordsRelations.keySet()) {
            Record relation = null;
            for (Map.Entry<Record, Collection<Record>> recordRelations : recordsRelations.entrySet()) {
                for (Record rel : recordRelations.getValue()) {
                    if (record.equals(rel)) {
                        relation = rel;
                        break;
                    }
                }
                if (relation != null) {
                    break;
                }
            }
            if (relation == null) {
                return record;
            }
        }
        throw new exception(
                "Ошибка экспорта: не удалось найти конечную запись. В базе данных есть циклическая зависимость. Обратитесь к системному администратору.");
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
