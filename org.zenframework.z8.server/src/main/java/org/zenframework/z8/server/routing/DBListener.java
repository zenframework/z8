package org.zenframework.z8.server.routing;

import org.zenframework.z8.server.base.table.system.RoleRequestAccess;
import org.zenframework.z8.server.db.generator.DBGenerator;
import org.zenframework.z8.server.db.sql.functions.Sql;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.string;

public class DBListener implements DBGenerator.Listener {

	@Override
	public void beforeStart() {}

	@Override
	public void afterFinish() {
		RoleRequestAccess.CLASS<? extends RoleRequestAccess> requestAccess = new RoleRequestAccess.CLASS<RoleRequestAccess>(null);
		// access to execute
		requestAccess.get().execute.get().operatorAssign(new bool(true));

		// access for org.zenframework.z8.server.routing.AliasTable
		RCollection<string> requestsCls = new RCollection<>();
		requestsCls.add(new string(AliasTable.class.getName()));

		requestAccess.get().z8_update(Sql.z8_inVector(requestAccess.get().requests.get().classId.get().sql_string(), requestsCls));
	}
}
