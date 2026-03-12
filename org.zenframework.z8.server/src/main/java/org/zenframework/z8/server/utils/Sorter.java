package org.zenframework.z8.server.utils;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.integer;

public class Sorter extends OBJECT implements java.util.Comparator<OBJECT.CLASS<? extends OBJECT>> {
	public static class CLASS<T extends OBJECT> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Sorter.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Sorter(container);
		}
	}

	public Sorter(IObject container) {
		super(container);
	}

	@Override
	public int compare(OBJECT.CLASS<? extends OBJECT> o1, OBJECT.CLASS<? extends OBJECT> o2) {
		return z8_compare(o1, o2).getInt();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_sort(RCollection array) {
		array.sort(this);
	}

	public integer z8_compare(OBJECT.CLASS<? extends OBJECT> o1, OBJECT.CLASS<? extends OBJECT> o2) {
		return new integer(0);
	}
}
