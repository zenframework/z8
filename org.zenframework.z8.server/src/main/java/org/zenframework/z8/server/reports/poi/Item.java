package org.zenframework.z8.server.reports.poi;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class Item extends OBJECT implements Wrapper<Object> {
	public static final String Index = "item";

	public static class CLASS<T extends Item> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Item.class);
			setIndex(Index);
		}

		@Override
		public Object newObject(IObject container) {
			return new Item(container);
		}
	}

	private Object value;

	public Item(IObject container) {
		super(container);
	}

	@Override
	public Object get() {
		return value;
	}

	public Item set(Object value) {
		this.value = value;
		return this;
	}
}