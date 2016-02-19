package org.zenframework.z8.server.ie;

import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.server.types.guid;

import junit.framework.TestCase;

public class RecordsSorterTest extends TestCase {

    public RecordsSorterTest(String name) {
        super(name);
    }

    // TODO Закончить тест
    public void testComparator() throws Exception {
        RecordsSorter sorter = new RecordsSorter();
        for (int i = 1; i <= 29; i++)
            sorter.addRecord("", guid(i));
        addLinks(sorter, 1, 2, 3, 4);
        addLinks(sorter, 2, 4, 6, 19);
        addLinks(sorter, 3, 13, 16, 18);
        addLinks(sorter, 6, 3, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        addLinks(sorter, 9, 17);
        addLinks(sorter, 13, 9);
        addLinks(sorter, 19, 25);
        addLinks(sorter, 21, 3, 6, 22, 23);
        addLinks(sorter, 24, 3, 6, 7, 23);
        addLinks(sorter, 25, 17);
        addLinks(sorter, 26, 3, 6, 23);
        addLinks(sorter, 27, 3, 4, 6, 22);
        addLinks(sorter, 28, 6, 20, 29);
        addLinks(sorter, 29, 4, 6, 19);
        List<RecordsSorter.Record> sorted = sorter.getSorted();
        System.out.println("Sorted in " + sorter.getCount() + " steps: " + sorted);
        List<RecordsSorter.Record> checked = new ArrayList<RecordsSorter.Record>(sorted.size());
        for (RecordsSorter.Record record : sorted) {
            for (RecordsSorter.Record link : sorter.getLinks(record)) {
                assertTrue(checked.contains(link));
            }
            checked.add(record);
        }
    }
    
    private static guid guid(int c) {
        return new guid("00000000-0000-0000-0000-00000000000" + c);
    }
    
    private static void addLinks(RecordsSorter sorter, int rec, int... links) {
        for (int i = 0; i < links.length; i++) {
            sorter.addLink("", guid(rec), "", guid(links[i]));
        }
    }

}
