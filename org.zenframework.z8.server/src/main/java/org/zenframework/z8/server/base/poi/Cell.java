package org.zenframework.z8.server.base.poi;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
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

	public Cell(IObject container) {
		super(container);
	}

	public string z8_getString() {
		return new string(this.cell.getStringCellValue());
	}

	public decimal z8_getDecimal() {
		return new decimal(this.cell.getNumericCellValue());
	}

	public integer z8_getInt() {
		return new integer((long)this.cell.getNumericCellValue());
	}
}
