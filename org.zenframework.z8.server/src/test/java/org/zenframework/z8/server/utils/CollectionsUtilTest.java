package org.zenframework.z8.server.utils;

import junit.framework.TestCase;

import org.zenframework.z8.server.json.parser.JsonObject;

public class CollectionsUtilTest extends TestCase {

    public CollectionsUtilTest(String name) {
        super(name);
    }

    public void testEqualsMaps() throws Exception {
        assertTrue(CollectionsUtil.equals(new JsonObject("{ a: 'b', c: [1, 2, { a: 'e', f: 3 }], d: true }"),
                new JsonObject("{ d: true, c: [2, { f: 3, a: 'e' }, 1], a: 'b' }"), "a"));
    }

}
