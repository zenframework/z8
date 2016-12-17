package org.zenframework.z8.server.base.query;

import org.zenframework.z8.server.types.integer;

public class RecordLock {
	static public integer None = new integer(0);
	static public integer Full = new integer(1);
	static public integer Edit = new integer(2);
	static public integer Destroy = new integer(3);
}
