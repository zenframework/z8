package org.zenframework.z8.server.reports.poi;

import org.zenframework.z8.server.runtime.OBJECT;

public class SimpleSource extends DataSource {

	private final OBJECT object;

	public SimpleSource(OBJECT object) {
		this.object = object;
	}

	@Override
	protected boolean internalNext() {
		return getIndex() == 0;
	}

	@Override
	public OBJECT getObject() {
		return object;
	}

	@Override
	public int count() {
		return 1;
	}
}
