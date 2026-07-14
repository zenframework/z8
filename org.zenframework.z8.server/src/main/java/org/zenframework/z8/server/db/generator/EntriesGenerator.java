package org.zenframework.z8.server.db.generator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.system.Entries;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ErrorUtils;

public class EntriesGenerator {
	private ILogger logger;

	private Entries entries = new Entries.CLASS<Entries>().get();
	private UserEntries userEntries = new UserEntries.CLASS<UserEntries>().get();

	private Collection<guid> entryKeys = new HashSet<guid>();

	public EntriesGenerator(ILogger logger) {
		this.logger = logger;
		entryKeys.addAll(Runtime.instance().entryKeys());
	}

	public void run() {
		Connection connection = null;

		try {
			connection = ConnectionManager.get();
			connection.beginTransaction();
			dropEntries();
			createEntries();
			connection.commit();
		} catch(Throwable e) {
			if (connection != null)
				connection.rollback();
			logger.error(e, ErrorUtils.getMessage(e));
		} finally {
			if (connection != null)
				connection.release();
		}
	}

	private void dropEntries() {
		entries.read(Arrays.asList(entries.primaryKey()), entries.primaryKey().notInVector(entryKeys));

		while (entries.next()) {
			guid entry = entries.recordId();
			userEntries.destroy(new Equ(userEntries.entry.get(), entry));
			entries.destroy(entry);
		}
	}

	private void createEntries() {
		entries.read(Arrays.asList(entries.primaryKey()), entries.primaryKey().inVector(entryKeys));

		while (entries.next()) {
			guid entry = entries.recordId();
			setEntryProperties(Runtime.instance().getEntryByKey(entry).newInstance());
			entries.update(entry);
			entryKeys.remove(entry);
		}

		for (guid key : entryKeys) {
			setEntryProperties(Runtime.instance().getEntryByKey(key).newInstance());
			entries.create(key);
		}
	}

	private void setEntryProperties(OBJECT entry) {
		entries.classId.get().set(entry.classId());
		entries.name.get().set(new string(entry.displayName()));
		entries.lock.get().set(RecordLock.Destroy);
	}
}
