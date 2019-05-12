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

		public Object newObject(IObject container) {
			return new Cell(container);
		}
	}

	public org.apache.poi.ss.usermodel.Cell cell;

	public static final int Numeric = 0;
	public static final int String = 1;
	public static final int Formula = 2;
	public static final int Blank = 3;
	public static final int Boolean = 4;
	public static final int Error = 5;

	public Cell(IObject container) {
		super(container);
	}

	public string z8_getString() {
		switch(cell.getCellType()) {
		case String: 
			return new string(cell.getStringCellValue());
		case Numeric:
			double value = cell.getNumericCellValue();
			return value == (long)value ? new integer((long)value).string() : new decimal(value).string();
		case Boolean:
			return new bool(cell.getBooleanCellValue()).string();
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
