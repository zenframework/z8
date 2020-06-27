package org.zenframework.z8.server.base.poi;

import org.zenframework.z8.server.base.poi.Cell;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.integer;

public class Row extends OBJECT {
	public static class CLASS<T extends Row> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			this.setJavaClass(Row.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Row(container);
		}
	}

	public org.apache.poi.ss.usermodel.Row row;

	public Row(IObject container) {
		super(container);
	}

	public integer z8_getCellCount() {
		return new integer(this.row.getPhysicalNumberOfCells());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Cell.CLASS<? extends Cell> z8_getCell(integer index) {
		org.apache.poi.ss.usermodel.Cell cell = row.getCell(index.getInt());

		if(cell == null)
			return null;

		Cell.CLASS<Cell> cls = new Cell.CLASS(this);
		cls.get().cell = cell;
		return cls;
	}
}
