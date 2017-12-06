package org.zenframework.z8.server.base.table.value;

import java.util.Arrays;

import org.zenframework.z8.server.base.query.ReadLock;
import org.zenframework.z8.server.base.table.system.Sequences;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
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

	private String key = null;

	private integer defaultValue = new integer(1);
	private integer increment = new integer(1);

	public Sequencer(IObject container) {
		super(container);
	}

	static public void reset(String key) {
		Sequences sequences = new Sequences.CLASS<Sequences>().get();

		SqlToken where = new Equ(sequences.key.get(), key.toLowerCase());
		sequences.destroy(where);
	}

	static public long next(String key) {
		return next(key, 1L);
	}

	static public long next(String key, long defaultValue) {
		return next(key, defaultValue, 1L);
	}

	static public long next(String key, long defaultValue, long increment) {
		Sequences sequences = new Sequences.CLASS<Sequences>().get();
		sequences.setReadLock(ReadLock.Update);

		StringField keyField = sequences.key.get();
		IntegerField valueField = sequences.value.get();

		SqlToken where = new Equ(keyField, key);

		long result = defaultValue;

		if(sequences.readFirst(Arrays.<Field>asList(valueField), where)) {
			result = valueField.integer().get() + Math.max(increment, 1);
			valueField.set(new integer(result));
			sequences.update(sequences.recordId());
		} else {
			keyField.set(new string(key));
			valueField.set(new integer(result));
			sequences.create();
		}

		ConnectionManager.get().flush();

		return result;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public long next() {
		return next(key, defaultValue.get(), increment.get());
	}

	public void reset() {
		reset(key);
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

	public void z8_reset() {
		reset(key);
	}

	static public void z8_reset(string key) {
		reset(key.get());
	}
}
