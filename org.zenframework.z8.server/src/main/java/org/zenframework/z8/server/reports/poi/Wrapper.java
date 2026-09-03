package org.zenframework.z8.server.reports.poi;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class Wrapper<V> extends OBJECT {
	@SuppressWarnings("rawtypes")
	public static class CLASS<T extends Wrapper> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Wrapper.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Wrapper(container);
		}
	}

	private V value;

	public Wrapper(IObject container) {
		super(container);
	}

	public V get() {
		return value;
	}

	public Wrapper<V> set(V value) {
		this.value = value;
		return this;
	}

	@Override
	public String toString() {
		return "Wrapper[" + (value != null ? value.toString() : "null") + ']';
	}
}
