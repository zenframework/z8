package org.zenframework.z8.server.reports.poi;

import java.util.Collection;
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

	private static final String SheetSplitter = "!";

	private PoiReport report;

	private DataSource source;
	private int sheetIndex;
	private Block templateBlock, targetBlock;
	private Block.Direction direction;

	private final List<Range> ranges = new LinkedList<Range>();

	public Range setReport(PoiReport report) {
		this.report = report;

		for (Range range : ranges)
			range.setReport(report);

		return this;
	}

	public PoiReport getReport() {
		return report;
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

	public Block getTemplateBlock() {
		return templateBlock;
	}

	public Block getTargetBlock() {
		return targetBlock;
	}

	public Range setAddress(int sheetIndex, CellRangeAddress address) {
		this.sheetIndex = sheetIndex;
		this.templateBlock = new Block(address);
		return this;
	}

	public Range setAddress(String address) {
		String[] parts = address.split(SheetSplitter);
		return setAddress(parts.length > 1 ? Integer.parseInt(parts[0]) : 0, CellRangeAddress.valueOf(parts[parts.length - 1]));
	}

	public Block.Direction getDirection() {
		return direction;
	}

	public Range setDirection(Block.Direction direction) {
		this.direction = direction;
		return this;
	}

	public Range setDirection(int direction) {
		return setDirection(Block.Direction.valueOf(direction));
	}

	public List<Range> getRanges() {
		return ranges;
	}

	public Range setRanges(List<Range> ranges) {
		this.ranges.clear();
		this.ranges.addAll(ranges);
		return this;
	}

	public Range apply(XSSFWorkbook workbook) {
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		prepare(sheet);
		fill(sheet, new Block.Diff());
		return this;
	}

	protected Block prepare(Sheet sheet) {
		int count = source.count();

		source.prepare(sheet);

		// TODO Check inner ranges intersections, update ranges
		for (Range range : ranges) {
			if (!range.getTemplateBlock().in(templateBlock))
				throw new RuntimeException("Incorrect ranges: " + range.getTemplateBlock() + " is not in " + templateBlock);

			templateBlock = templateBlock.stretch(range.prepare(sheet).diffSize(range.getTemplateBlock()));
		}

		return targetBlock = Util.multiplyBlock(sheet, templateBlock, count, direction);
	}

	protected void fill(Sheet sheet, Block.Diff shift) {
		Util.CellVisitor visitor = new Util.CellVisitor() {
			@Override
			public void visit(Row row, int colNum, Cell cell) {
				if (inOneOf(row.getRowNum(), colNum, shift, ranges) || cell == null || cell.getCellTypeEnum() != CellType.STRING)
					return;

				Object value = source.evaluate(cell.getStringCellValue());
				cell.setCellValue(value != null ? value.toString() : "null");
			}
		};

		try {
			source.open();

			while (source.next()) {
				for (Range range : ranges)
					range.fill(sheet, shift.copy());

				Util.visitCells(sheet, templateBlock.shift(shift), visitor);

				shift.add(direction, templateBlock);
			}
		} finally {
			source.close();
		}
	}

	private DataSource toDataSource(OBJECT source) {
		if (source instanceof Query)
			return new QuerySource(this, (Query) source);
		if (source instanceof JsonArray)
			return new JsonSource(this, (JsonArray) source);
		return null;
	}

	private static boolean inOneOf(int row, int col, Block.Diff shift, Collection<Range> ranges) {
		for (Range range : ranges)
			if (range.getTargetBlock().shift(shift).has(row, col))
				return true;
		return false;
	}
}
