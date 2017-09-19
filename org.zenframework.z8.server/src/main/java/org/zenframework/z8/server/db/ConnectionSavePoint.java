package org.zenframework.z8.server.db;

import java.util.Locale;

import org.zenframework.z8.server.engine.Database;

public class ConnectionSavePoint {
	private Locale locale;

	static public ConnectionSavePoint create(Database database) {
		ConnectionSavePoint savePoint = new ConnectionSavePoint();

		if(database.vendor() == DatabaseVendor.Oracle) {
			savePoint.locale = Locale.getDefault();
			Locale.setDefault(Locale.US);
		}

		return savePoint;
	}

	public void restore() {
		if(locale != null)
			Locale.setDefault(locale);
	}
}
