package org.zenframework.z8.server.base.table.value;

import java.util.Arrays;

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

public class Sequencer extends OBJECT {
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

	static private final ThreadLocal<Listener> currentListener = new ThreadLocal<Listener>();

	private String key = null;
	private integer defaultValue = new integer(1);
	private integer increment = new integer(1);

	public Sequencer(IObject container) {
		super(container);
	}

	static class Listener implements Connection.Listener {
		private Sequences sequences;
		private String key;
		private long value;
		private long increment;
		private guid id = null;

		public Listener(String key, long defaultValue, long increment) {
			this.sequences = new Sequences.CLASS<Sequences>().get();

			this.key = key;
			this.value = defaultValue;
			this.increment = Math.max(increment, 1);
			initialize();
		}

		private void initialize() {
			sequences.setReadLock(ReadLock.Update);

			StringField keyField = sequences.key.get();
			IntegerField valueField = sequences.value.get();

			if(sequences.readFirst(Arrays.<Field>asList(valueField), new Equ(keyField, key))) {
				id = sequences.recordId();
				value = valueField.integer().get();
			} else
				createSequencerRecord();
		}

		private void createSequencerRecord() {
			Connection connection = null;

			try {
				connection = ConnectionManager.get().inTransaction() ? Connection.connect(ConnectionManager.database()) : null;

				Sequences sequences = new Sequences.CLASS<Sequences>().get();
				sequences.setConnection(connection);
				sequences.key.get().set(key);
				sequences.value.get().set(value - increment);
				sequences.create();
			} finally {
				if(connection != null)
					connection.close();
			}

			initialize();
		}

		public long next() {
			value += increment;
			return value;
		}

		public long flush() {
			sequences.value.get().set(value);
			sequences.update(id);
			return value;
		}

		public void on(Connection connection, ConnectionEvent event) {
			if(event == ConnectionEvent.Flush || event == ConnectionEvent.Commit)
				flush();

			if(event == ConnectionEvent.Commit || event == ConnectionEvent.Rollback)
				currentListener.remove();
		}
	}

	static public long next(String key) {
		return next(key, 1L);
	}

	static public long next(String key, long defaultValue) {
		return next(key, defaultValue, 1L);
	}

	static public long next(String key, long defaultValue, long increment) {
		Connection connection = ConnectionManager.get();

		boolean inTransaction = connection.inTransaction();
		Listener listener = inTransaction ? currentListener.get() : new Listener(key, defaultValue, increment);

		if(inTransaction) {
			listener = currentListener.get();
			if(listener == null) {
				listener = new Listener(key, defaultValue, increment);
				currentListener.set(listener);
				connection.addListener(listener);
			}
			return listener.next();
		} else {
			listener = new Listener(key, defaultValue, increment);
			listener.next();
			return listener.flush();
		}
	}

	public void setKey(String key) {
		this.key = key;
	}

	public long next() {
		return next(key, defaultValue.get(), increment.get());
	}

	public integer z8_next() {
		return z8_next(new string(key), new integer(1), new integer(1));
	}

	public integer z8_next(integer defaultValue) {
		return z8_next(new string(key), defaultValue, new integer(1));
	}

	public integer z8_next(integer defaultValue, integer increment) {
		return z8_next(new string(key), defaultValue, increment);
	}

	static public integer z8_next(string key) {
		return z8_next(key, new integer(1));
	}

	static public integer z8_next(string key, integer defaultValue) {
		return z8_next(key, new integer(defaultValue.getInt()), new integer(1));
	}

	static public integer z8_next(string key, integer defaultValue, integer increment) {
		return new integer(next(key.get(), defaultValue.get(), increment.get()));
	}
}
