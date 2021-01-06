package org.zenframework.z8.server.db;

import java.sql.SQLException;

import org.zenframework.z8.server.base.Executable;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;

public class MaintenanceJob extends Executable {
	public static class CLASS<T extends MaintenanceJob> extends Executable.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(MaintenanceJob.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new MaintenanceJob(container);
		}
	}

	public MaintenanceJob(IObject container) {
		super(container);
		useTransaction = bool.False;
	}

	@Override
	protected void z8_execute(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
		Connection connection = ConnectionManager.get();
		if(connection.vendor() != DatabaseVendor.Postgres)
			return;

		try {
			Trace.logEvent("VACUUM ANALYZE started.");
			Statement.executeUpdate("vacuum analyze");
			Trace.logEvent("VACUUM ANALYZE finished successfully.");
		} catch(SQLException e) {
			Trace.logError("VACUUM ANALYZE finished with error.", e);
		}
	}
}
