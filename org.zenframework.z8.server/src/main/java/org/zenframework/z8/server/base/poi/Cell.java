package org.zenframework.z8.server.base.poi;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
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
		return new string(getCellStringValue(cell));
	}

	public decimal z8_getDecimal() {
		return new decimal(cell.getNumericCellValue());
	}

	public integer z8_getInt() {
		return new integer((long) cell.getNumericCellValue());
	}

	public date z8_getDate() {
		return new date(cell.getDateCellValue());
	}

	static public String getCellStringValue(org.apache.poi.ss.usermodel.Cell cell) {
		// TODO Use CellType with POI-16
		switch (cell.getCellType()) {
		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN:
			return Boolean.toString(cell.getBooleanCellValue());
		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA:
			return cell.getCellFormula();
		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
			double value = cell.getNumericCellValue();
			return value == (long) value ? Long.toString((long) value) : Double.toString(value);
		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue();
		default:
			return "";
		}
	}
}
