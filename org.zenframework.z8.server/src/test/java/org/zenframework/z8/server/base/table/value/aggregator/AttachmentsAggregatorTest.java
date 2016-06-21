package org.zenframework.z8.server.base.table.value.aggregator;

import junit.framework.TestCase;

import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.CollectionUtils;

public class AttachmentsAggregatorTest extends TestCase {

	public AttachmentsAggregatorTest(String name) {
		super(name);
	}

	public void testAggregate() throws Exception {
		String o1 = "{\"path\":\"table/org.zenframework.z8.server.base.table.system.SystemFiles/95903074-371D-4F4C-91C1-A6FEA90C9B8B/File/Текст документа.txt\",\"size\":0,\"name\":\"Текст документа.txt\",\"time\":\"20/06/2016 19:20:17\"}";
		String o2 = "{\"path\":\"table/org.zenframework.z8.server.base.table.system.SystemFiles/B53E9D35-22B2-45FF-871F-B75191E1CF0A/File/message.eml\",\"size\":0,\"name\":\"message.eml\",\"time\":\"20/06/2016 19:20:17\"}";
		String o3 = "{\"path\":\"table/org.zenframework.z8.server.base.table.system.SystemFiles/AFA9A437-C253-4BBA-AF9A-9B37EE4CE540/File/Текст документа.txt\",\"size\":0,\"name\":\"Текст документа.txt\",\"time\":\"20/06/2016 19:20:17\"}";
		String a1 = '[' + o1 + ", " + o2 + ']';
		String a2 = '[' + o1 + ", " + o3 + ']';
		String a3 = '[' + o1 + ", " + o2 + ", " + o3 + ']';
		AttachmentsAggregator aggregator = new AttachmentsAggregator.CLASS<AttachmentsAggregator>().get();
		String result = aggregator.aggregate(new string(a1), new string(a2)).string().get();
		System.out.println(result);
		JsonArray arr = new JsonArray(result);
		assertTrue(CollectionUtils.equals(arr, new JsonArray(a3), "path"));
	}

}
