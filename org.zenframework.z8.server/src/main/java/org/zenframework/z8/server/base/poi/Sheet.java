package org.zenframework.z8.server.base.poi;

import org.zenframework.z8.server.base.poi.Row;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.integer;

public class Sheet extends OBJECT {
	public static class CLASS<T extends Sheet> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			this.setJavaClass(Sheet.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Sheet(container);
		}
	}

	public org.apache.poi.ss.usermodel.Sheet sheet;

	public Sheet(IObject container) {
		super(container);
	}

	public integer z8_getRowCount() {
		return new integer((long)this.sheet.getPhysicalNumberOfRows());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Row.CLASS<? extends Row> z8_getRow(integer index) {
		Row.CLASS<Row> cls = new Row.CLASS(this);
		cls.get().row = this.sheet.getRow(index.getInt());
		return cls;
	}
}
