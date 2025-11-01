package org.zenframework.z8.server.reports.poi;

import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.runtime.OBJECT;

public class Range {

	public static enum Direction {
		Vertical(0), Horizontal(1);

		private final int value;

		private Direction(int value) {
			this.value = value;
		}

		public static Direction valueOf(int value) {
			return value == Horizontal.value ? Horizontal : Vertical;
		}
	}

	private static final String SheetSplitter = "!";

	private OBJECT context;
	private DataSource source;
	private int sheetIndex;
	private CellRangeAddress address;
	private Direction direction;

	private final List<Range> ranges = new LinkedList<Range>();

	public OBJECT getContext() {
		return context;
	}

	public Range setContext(OBJECT context) {
		this.context = context;
		return this;
	}

	public DataSource getSource() {
		return source;
	}

	public Range setSource(DataSource source) {
		this.source = source;
		return this;
	}

	public Range setSource(OBJECT source) {
		return setSource(toDataSource(source));
	}

	public int getSheetIndex() {
		return sheetIndex;
	}

	public CellRangeAddress getAddress() {
		return address;
	}

	public Range setAddress(int sheetIndex, CellRangeAddress address) {
		this.sheetIndex = sheetIndex;
		this.address = address;
		return this;
	}

	public Range setAddress(String address) {
		String[] parts = address.split(SheetSplitter);
		return setAddress(parts.length > 1 ? Integer.parseInt(parts[0]) : 0, CellRangeAddress.valueOf(parts[parts.length - 1]));
	}

	public Direction getDirection() {
		return direction;
	}

	public Range setDirection(Direction direction) {
		this.direction = direction;
		return this;
	}

	public Range setDirection(int direction) {
		return setDirection(Direction.valueOf(direction));
	}

	public List<Range> getRanges() {
		return ranges;
	}

	public Range setRanges(List<Range> ranges) {
		this.ranges.clear();
		this.ranges.addAll(ranges);
		return this;
	}

	public void apply(XSSFWorkbook workbook) {
		for (Range range : ranges)
			range.apply(workbook);

		source.prepare(workbook);

		int rowStart = address.getFirstRow(), colStart = address.getFirstColumn();
		int rowEnd = address.getLastRow() + 1, colEnd = address.getLastColumn() + 1;
		int height = rowEnd - rowStart, width = colEnd - colStart;
		int rowShift = 0, colShift = 0;
		int count = source.count();

		Sheet sheet = workbook.getSheetAt(sheetIndex);

		Util.multiplyBlock(sheet, address, count, direction);

		Util.CellVisitor visitor = new Util.CellVisitor() {
			@Override
			public void visit(Row row, int colNum, Cell cell) {
				if (cell == null || cell.getCellTypeEnum() != CellType.STRING)
					return;

				Object value = source.evaluate(cell.getStringCellValue());
				cell.setCellValue(value != null ? value.toString() : "null");
			}
		};

		while (source.next()) {
			Util.visitCells(sheet, rowStart + rowShift, colStart + colShift, rowEnd + rowShift, colEnd + colShift, visitor);

			if (direction == Direction.Horizontal)
				colShift += width;
			else
				rowShift += height;
		}
	}

	private DataSource toDataSource(OBJECT source) {
		if (source instanceof Query)
			return new QuerySource(this, (Query) source);
		if (source instanceof JsonArray)
			return new JsonSource(this, (JsonArray) source);
		return null;
	}
}
