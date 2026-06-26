package org.zenframework.z8.server.reports.poi;

import org.zenframework.z8.server.base.form.report.CustomData;
import org.zenframework.z8.server.runtime.OBJECT;

public class CustomSource extends DataSource {

	public CustomSource(CustomData customData) {
		this.customData = customData;
	}

	private final CustomData customData;

	public int count() {
		return customData.z8_count().getInt();
	}

	public void open() {
		super.open();
		customData.z8_open();
	}

	public void close() {
		super.close();
		customData.z8_close();
	}

	@Override
	public boolean next() {
		super.next();

		return customData.z8_getIndex().getInt() < customData.z8_count().getInt();
	}

	@Override
	public OBJECT getObject() {
		return customData;
	}
}
