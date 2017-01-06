package org.zenframework.z8.server.db.generator;

import org.zenframework.z8.server.base.Procedure;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.utils.ErrorUtils;

public class DBGenerateProcedure extends Procedure {
	public static class CLASS<T extends DBGenerateProcedure> extends Procedure.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(DBGenerateProcedure.class);
			setDisplayName(Resources.get("Generator.displayName"));
		}

		@Override
		public Object newObject(IObject container) {
			return new DBGenerateProcedure(container);
		}
	}

	public DBGenerateProcedure(IObject container) {
		super(container);
		useTransaction = bool.False;
	}

	@Override
	protected void z8_execute() {
		if(!ApplicationServer.getUser().isAdministrator()) {
			error("You must be a member of Administrators security group to perform this action.");
			return;
		}

		new DBGenerator(new Logger()).run();
	}

	private class Logger implements ILogger {
		@Override
		public void error(Throwable exception, String message) {
			DBGenerateProcedure.this.error(message);
		}

		@Override
		public void error(Throwable exception) {
			error(exception, ErrorUtils.getMessage(exception));
		}

		@Override
		public void info(String message) {
			DBGenerateProcedure.this.info(message);
		}

		@Override
		public void warning(String message) {
			DBGenerateProcedure.this.warning(message);
		}

		@Override
		public void progress(int percentDone) {
			reportProgress(percentDone);
		}
	}
}
