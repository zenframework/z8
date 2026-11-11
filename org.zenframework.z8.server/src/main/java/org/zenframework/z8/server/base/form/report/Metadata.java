package org.zenframework.z8.server.base.form.report;

import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class Metadata extends OBJECT {
	static public class CLASS<T extends Metadata> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Metadata.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Metadata(container);
		}
	}

	public Aggregation aggregation = Aggregation.None;

	public Metadata(IObject container) {
		super(container);
	}
}
