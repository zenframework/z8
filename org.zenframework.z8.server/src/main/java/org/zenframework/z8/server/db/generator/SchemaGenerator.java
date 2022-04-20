package org.zenframework.z8.server.db.generator;

import org.zenframework.z8.server.base.Executable;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.EventsLevel;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ErrorUtils;

public class SchemaGenerator extends Executable {
	final static public String Name = "Schema generator";
	
	public static class CLASS<T extends SchemaGenerator> extends Executable.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(SchemaGenerator.class);
			setName(SchemaGenerator.Name);
			setDisplayName(Resources.get("Generator.displayName"));
			setAttribute(SystemTool, Integer.toString(100000));
		}

		@Override
		public Object newObject(IObject container) {
			return new SchemaGenerator(container);
		}
	}

	public SchemaGenerator(IObject container) {
		super(container);
		useTransaction = bool.False;
	}

	@Override
	protected void execute() {
		if(!ApplicationServer.getUser().isAdministrator()) {
			error("You must be a member of Administrators security group to perform this action.");
			return;
		}
		super.execute();
	}

	public void z8_beforeStart() {
	}

	public void z8_afterFinish() {
	}

	@Override
	protected void z8_execute(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
		ILogger logger = new Logger();
		IDatabase database = ApplicationServer.getDatabase();

		try {
			Scheduler.suspend(database);
			logger.info(Resources.get("Generator.schedulerStopped"));

			ApplicationServer.setEventsLevel(EventsLevel.SYSTEM);

			z8_beforeStart();

			new Generator(logger).run();

			z8_afterFinish();
		} catch(Throwable e) {
			logger.error(e);
		} finally {
			ApplicationServer.restoreEventsLevel();

			Scheduler.resume(database);
			logger.info(Resources.get("Generator.schedulerStarted"));
		}
	}

	public bool z8_tableExists(string tableName) {
		return new bool(ConnectionManager.database().tableExists(tableName.get()));
	}

	public bool z8_fieldExists(string tableName, string fieldName) {
		return new bool(ConnectionManager.database().fieldExists(tableName.get(), fieldName.get()));
	}

	public void z8_renameTable(string tableName, string newTableName) {
		ConnectionManager.database().renameTable(tableName.get(), newTableName.get());
	}

	public void z8_renameField(string tableName, string fieldName, string newFieldName) {
		ConnectionManager.database().renameField(tableName.get(), fieldName.get(), newFieldName.get());
	}

	public void z8_dropTable(string tableName) {
		ConnectionManager.database().dropTable(tableName.get());
	}

	private class Logger implements ILogger {
		@Override
		public void error(Throwable exception, String message) {
			SchemaGenerator.this.error(message);
		}

		@Override
		public void error(Throwable exception) {
			error(exception, ErrorUtils.getMessage(exception));
		}

		@Override
		public void info(String message) {
			SchemaGenerator.this.info(message);
		}

		@Override
		public void warning(String message) {
			SchemaGenerator.this.warning(message);
		}

		@Override
		public void progress(int percentDone) {
			reportProgress(percentDone);
		}
	}
}
