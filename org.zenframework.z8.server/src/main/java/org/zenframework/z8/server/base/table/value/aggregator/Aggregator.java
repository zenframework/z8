package org.zenframework.z8.server.base.table.value.aggregator;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.primary;

public class Aggregator extends OBJECT {

	public static class CLASS<T extends Aggregator> extends OBJECT.CLASS<T> {

		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Aggregator.class);
			setName(Aggregator.class.getName());
			setDisplayName(Aggregator.class.getName());
		}

		@Override
		public Object newObject(IObject container) {
			return new Aggregator(container);
		}

	}

	public Aggregator(IObject container) {
		super(container);
	}

	public primary aggregate(primary oldValue, primary newValue) {
		return z8_aggregate(oldValue, newValue);
	}
	
	public primary z8_aggregate(primary oldValue, primary newValue) {
		return newValue;
	}

}
