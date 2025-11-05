package org.zenframework.z8.server.reports.poi;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.runtime.OBJECT;

public class Range {

	private static final String SheetSplitter = "!";

	private ReportOptions options;

	private DataSource source;
	private int sheetIndex;
	private Block templateBlock, targetBlock;
	private Block.Direction direction;

	private final List<Range> ranges = new LinkedList<Range>();

	public Range setReport(ReportOptions options) {
		this.options = options;

		for (Range range : ranges)
			range.setReport(options);

		return this;
	}

	public ReportOptions getOptions() {
		return options;
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
		SheetModifier sheet = new SheetModifier().setSheet(workbook.getSheetAt(sheetIndex));
		prepare(sheet, null);
		fill(sheet, new Block.Diff());
		return this;
	}

	// Returns modified boundaries
	protected Block prepare(SheetModifier sheet, Block boundaries) {
		int count = source.count();

		source.prepare(sheet);

		// TODO Check inner ranges intersections, update ranges
		for (Range range : ranges) {
			//if (!range.getTemplateBlock().in(templateBlock))
			//	throw new RuntimeException("Incorrect ranges: " + range.getTemplateBlock() + " is not in " + templateBlock);

			templateBlock = range.prepare(sheet, templateBlock);
		}

		if (count < 1) {
			// TODO Delete cells
		}

		Block multiplied = /*boundaries != null ? boundaries.band(direction, templateBlock) :*/ templateBlock;

		sheet.insertBlock(multiplied.shift(direction, 1).stretch(direction, count - 1), direction);

		for (int i = 1; i < count; i++)
			sheet.copy(templateBlock, templateBlock.shift(direction, i));

		targetBlock = templateBlock.stretch(direction, count);

		sheet.updateMergedRegions(boundaries, multiplied, direction, count);

		return boundaries != null ? boundaries.stretch(targetBlock.diffSize(templateBlock)) : null;
	}

	protected void fill(SheetModifier sheet, Block.Diff baseShift) {
		SheetModifier.CellVisitor visitor = new SheetModifier.CellVisitor() {
			@Override
			public void visit(Row row, int colNum, Cell cell) {
				if (inOneOf(row.getRowNum(), colNum, baseShift, ranges) || cell == null || cell.getCellTypeEnum() != CellType.STRING)
					return;

				Object value = source.evaluate(cell.getStringCellValue());
				cell.setCellValue(value != null ? value.toString() : "null");
			}
		};

		try {
			source.open();

			while (source.next()) {
				Block.Diff shift = baseShift.add(templateBlock.vector(direction, source.getIndex()));

				for (Range range : ranges)
					range.fill(sheet, shift);

				sheet.visitCells(templateBlock.shift(shift), visitor);
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
