package org.zenframework.z8.server.ie;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.IOUtils;

public class RecordsSorterTest extends TestCase {

	private static Collection<guid> BUILTIN_RECORDS = Arrays.asList(new guid("00000000-0000-0000-0000-000000000000"),
			new guid("00000000-0000-0000-0000-000000000001"), new guid("00000000-0000-0000-0000-000000000002"));

	public RecordsSorterTest(String name) {
		super(name);
	}

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
		assertTrue(!isSorted(sorter, sorter.getUnsorted()));
		assertTrue(isSorted(sorter, sorter.getSorted()));
	}

	public void testRealMessage() throws Exception {
		testRealMessage("message.xml");
		testRealMessage("message2.xml");
	}

	private void testRealMessage(String message) throws Exception {
		JsonObject structure = new JsonObject(IOUtils.readText(getClass().getClassLoader().getResourceAsStream(
				"structure.json")));
		RecordsSorter sorter = new RecordsSorter();
		ExportEntry entry = IeUtil.unmarshalExportEntry(new InputStreamReader(getClass().getClassLoader()
				.getResourceAsStream(message)));
		for (ExportEntry.Records.Record record : entry.getRecords().getRecord()) {
			guid recordId = new guid(record.getRecordId());
			sorter.addRecord(record.getTable(), recordId);
			for (ExportEntry.Records.Record.Field field : record.getField()) {
				JsonObject tableInfo = structure.getJsonObject(record.getTable());
				if (tableInfo == null)
					throw new Exception("No structure for '" + record.getTable() + "'");
				if (tableInfo.has(field.getId())) {
					String linkTable = tableInfo.getString(field.getId());
					guid linkId = new guid(field.getValue());
					if (!BUILTIN_RECORDS.contains(linkId)) {
						sorter.addLink(record.getTable(), recordId, linkTable, linkId);
					}
				}
			}
		}
		assertTrue(!isSorted(sorter, sorter.getUnsorted()));
		assertTrue(isSorted(sorter, sorter.getSorted()));
	}

	private static guid guid(int c) {
		return new guid("00000000-0000-0000-0000-00000000000" + c);
	}

	private static void addLinks(RecordsSorter sorter, int rec, int... links) {
		for (int i = 0; i < links.length; i++) {
			sorter.addLink("", guid(rec), "", guid(links[i]));
		}
	}

	private static boolean isSorted(RecordsSorter sorter, List<RecordsSorter.Record> sorted) {
		System.out.println(sorted.size() + " sorted in " + sorter.getCount() + " steps:");
		for (RecordsSorter.Record record : sorted) {
			System.out.println(record);
		}
		List<RecordsSorter.Record> checked = new ArrayList<RecordsSorter.Record>(sorted.size());
		for (RecordsSorter.Record record : sorted) {
			for (RecordsSorter.Record link : sorter.getLinks(record)) {
				if (!checked.contains(link))
					return false;
			}
			checked.add(record);
		}
		return true;
	}

}
