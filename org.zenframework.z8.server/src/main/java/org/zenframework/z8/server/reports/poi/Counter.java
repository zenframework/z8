package org.zenframework.z8.server.reports.poi;

import java.util.Collection;
import java.util.LinkedList;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.integer;

public class Counter extends OBJECT implements Wrapper<integer> {
	public static final String Index = "counter";

	private static final Collection<Counter> Counters = new LinkedList<Counter>();

	public static class CLASS<T extends Counter> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Counter.class);
			setIndex(Index);
		}

		@Override
		public Object newObject(IObject container) {
			return new Counter(container);
		}
	}

	private int value = -1;

	public Counter(IObject container) {
		super(container);
		Counters.add(this);
	}

	public Collection<Counter> getCounters() {
		return Counters;
	}

	@Override
	public integer get() {
		return new integer(value);
	}

	public int getInt() {
		return value;
	}

	public int inc() {
		return ++value;
	}

	public Counter set(int value) {
		this.value = value;
		return this;
	}

	public Counter reset() {
		return set(-1);
	}
}
