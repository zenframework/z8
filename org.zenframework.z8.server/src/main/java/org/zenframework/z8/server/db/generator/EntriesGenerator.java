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
import org.zenframework.z8.server.db.sql.expressions.UnaryNot;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class EntriesGenerator {
	@SuppressWarnings("unused")
	private ILogger logger;

	private Entries entries = new Entries.CLASS<Entries>().get();
	private UserEntries userEntries = new UserEntries.CLASS<UserEntries>().get();

	private Collection<guid> entryKeys = new HashSet<guid>();

	public EntriesGenerator(ILogger logger) {
		this.logger = logger;
		entryKeys.addAll(Runtime.instance().entryKeys());
	}

	public void run() {
		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			writeEntries();
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		} finally {
			connection.release();
		}
	}

	private void writeEntries() {
		entries.read(Arrays.asList(entries.primaryKey()), new UnaryNot(new InVector(entries.primaryKey(), entryKeys)));

		while(entries.next()) {
			guid entry = entries.recordId();
			userEntries.destroy(new Equ(userEntries.entry.get(), entry));
			entries.destroy(entry);
		}

		createEntries();
	}

	private void createEntries() {
		entries.read(Arrays.asList(entries.primaryKey()), new InVector(entries.primaryKey(), entryKeys));
		while(entries.next()) {
			guid entry = entries.recordId();
			setEntryProperties(Runtime.instance().getEntryByKey(entry).newInstance());
			entries.update(entry);
			entryKeys.remove(entry);
		}

		for(guid key : entryKeys) {
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
