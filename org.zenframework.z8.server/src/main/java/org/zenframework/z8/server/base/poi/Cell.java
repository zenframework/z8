package org.zenframework.z8.server.base.poi;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Cell extends OBJECT {
	public static class CLASS<T extends Cell> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			this.setJavaClass(Cell.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Cell(container);
		}
	}

	public org.apache.poi.ss.usermodel.Cell cell;

	public Cell(IObject container) {
		super(container);
	}

	public string z8_getString() {
		switch(cell.getCellTypeEnum()) {
		case BOOLEAN:
			return new bool(cell.getBooleanCellValue()).string();
		case FORMULA:
			return new string(cell.getCellFormula());
		case NUMERIC:
			double value = cell.getNumericCellValue();
			return value == (long)value ? new integer((long)value).string() : new decimal(value).string();
		case STRING:
			return new string(cell.getStringCellValue());
		default:
			return new string();
		}
	}

	public decimal z8_getDecimal() {
		return new decimal(cell.getNumericCellValue());
	}

	public integer z8_getInt() {
		return new integer((long)cell.getNumericCellValue());
	}
	
	public date z8_getDate() {
		return new date(cell.getDateCellValue());
	}
}
