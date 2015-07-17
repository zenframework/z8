package org.zenframework.z8.server.ie;

import org.zenframework.z8.server.types.guid;
import junit.framework.TestCase;

public class RecordsSorterTest extends TestCase {

    public RecordsSorterTest(String name) {
        super(name);
    }

    // TODO Закончить тест
    public void testComparator() throws Exception {
        RecordsSorter sorter = new RecordsSorter();
        sorter.addRecord("", guid(1));
        sorter.addRecord("", guid(2));
        sorter.addRecord("", guid(3));
        sorter.addRecord("", guid(4));
        sorter.addRecord("", guid(5));
        sorter.addRecord("", guid(6));
        sorter.addRecord("", guid(7));
        sorter.addRecord("", guid(8));
        sorter.addLink("", guid(1), "", guid(2));
        sorter.addLink("", guid(1), "", guid(5));
        sorter.addLink("", guid(5), "", guid(7));
        sorter.addLink("", guid(1), "", guid(6));
        sorter.addLink("", guid(6), "", guid(7));
        sorter.addLink("", guid(4), "", guid(3));
        sorter.addLink("", guid(3), "", guid(6));
        sorter.addLink("", guid(4), "", guid(8));
        System.out.println(sorter.getSorted());
    }
    
    private static guid guid(int c) {
        return new guid("00000000-0000-0000-0000-00000000000" + c);
    }
    
}
