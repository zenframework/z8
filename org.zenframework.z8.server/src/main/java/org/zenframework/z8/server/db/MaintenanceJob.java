package org.zenframework.z8.server.db;

import java.sql.SQLException;

import org.zenframework.z8.server.base.Procedure;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;

public class MaintenanceJob extends Procedure {
	public static class CLASS<T extends MaintenanceJob> extends Procedure.CLASS<T> {
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
			Statement.executeUpdate(connection, "vacuum analyze");
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
