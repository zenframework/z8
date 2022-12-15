package org.zenframework.z8.server.base.table.value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.ReadLock;
import org.zenframework.z8.server.base.table.system.Sequences;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionEvent;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Sequencer extends OBJECT implements Connection.Listener {
	public static class CLASS<T extends Sequencer> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Sequencer.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Sequencer(container);
		}
	}

	static private final ThreadLocal<Map<String, Sequencer>> sequencers = new ThreadLocal<Map<String, Sequencer>>();

	private String key;
	private long defaultValue = 1;
	private long increment = 1;

	private guid id = null;
	private Sequences sequences = new Sequences.CLASS<Sequences>().get();

	private long nextValue;

	public Sequencer(IObject container) {
		super(container);
	}

	private void initialize() {
		if(read())
			return;

		createSequencerRecord();
		read();
	}

	private boolean read() {
		StringField keyField = sequences.key.get();
		IntegerField valueField = sequences.value.get();

		sequences.setReadLock(ReadLock.Update);
		if(!sequences.readFirst(Arrays.<Field>asList(valueField), new Equ(keyField, key)))
			return false;

		id = sequences.recordId();
		nextValue = valueField.integer().get();
		return true;
	}

	private void createSequencerRecord() {
		Connection connection = null;

		try {
			connection = ConnectionManager.get().inTransaction() ? Connection.connect(ConnectionManager.database()) : null;

			Sequences sequences = new Sequences.CLASS<Sequences>().get();
			sequences.setConnection(connection);
			sequences.key.get().set(key);
			sequences.value.get().set(defaultValue);
			sequences.create(guid.create(key));
		} finally {
			if(connection != null)
				connection.close();
		}
	}

	private long getNextValue() {
		nextValue += increment;
		return nextValue;
	}

	private void flush() {
		sequences.value.get().set(nextValue);
		sequences.update(id);
	}

	public long next() {
		Connection connection = ConnectionManager.get();

		long nextValue;
		Sequencer sequencer = this;

		if(connection.inTransaction()) {
			Map<String, Sequencer> map = sequencers.get();

			if(map == null) {
				map = new HashMap<String, Sequencer>();
				sequencers.set(map);
			}

			sequencer = map.get(key);

			if(sequencer == null) {
				sequencer = this;
				map.put(key, sequencer);
				connection.addListener(sequencer);
				sequencer.initialize();
			} else
				sequencer.increment = this.increment;

			nextValue = sequencer.getNextValue();
		} else {
			initialize();
			nextValue = getNextValue();
			flush();
		}

		return nextValue;
	}

	@Override
	public void on(Connection connection, ConnectionEvent event) {
		if(event == ConnectionEvent.Flush || event == ConnectionEvent.Commit)
			flush();

		if(event == ConnectionEvent.Commit || event == ConnectionEvent.Rollback) {
			Map<String, Sequencer> map = sequencers.get();
			map.remove(key);
			if(map.isEmpty())
				sequencers.remove();
		}
	}

	static public long next(String key) {
		return next(key, 1L, 1L);
	}

	static public long next(String key, long increment) {
		return next(key, increment, 1L);
	}

	static public long next(String key, long increment, long defaultValue) {
		Sequencer sequencer = new Sequencer.CLASS<Sequencer>(null).get();
		sequencer.key = key;
		sequencer.increment = increment;
		sequencer.defaultValue = defaultValue;
		return sequencer.next();
	}

	public void setKey(String key) {
		this.key = key;
	}

	public integer z8_next() {
		return new integer(next());
	}

	public integer z8_next(integer increment) {
		this.increment = increment.get();
		return z8_next();
	}

	public integer z8_next(integer increment, integer defaultValue) {
		this.increment = increment.get();
		this.defaultValue = defaultValue.get();
		return z8_next();
	}

	static public integer z8_next(string key) {
		return z8_next(key, new integer(1));
	}

	static public integer z8_next(string key, integer increment) {
		return z8_next(key, increment, new integer(1));
	}

	static public integer z8_next(string key, integer increment, integer defaultValue) {
		return new integer(next(key.get(), increment.get(), defaultValue.get()));
	}
}
