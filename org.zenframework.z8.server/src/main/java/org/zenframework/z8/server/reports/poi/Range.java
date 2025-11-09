package org.zenframework.z8.server.reports.poi;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.OBJECT;

public class Range {

	private static final String SheetSplitter = "!";

	public static java.util.Comparator<Range> Comparator = new java.util.Comparator<Range>() {
		@Override
		public int compare(Range o1, Range o2) {
			if (o1.getDirection() == Block.Direction.Horizontal && o1.getBlock().startCol() < o2.getBlock().startCol()
					|| o1.getDirection() == Block.Direction.Vertical && o1.getBlock().startRow() < o2.getBlock().startRow())
				return -1;
			if (o2.getDirection() == Block.Direction.Horizontal && o2.getBlock().startCol() < o1.getBlock().startCol()
					|| o2.getDirection() == Block.Direction.Vertical && o2.getBlock().startRow() < o1.getBlock().startRow())
				return 1;
			return 0;
		}
	};

	private ReportOptions options;

	private DataSource source;
	private int sheetIndex;
	private Block block;
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

	public Block getBlock() {
		return block;
	}

	public Range setAddress(int sheetIndex, CellRangeAddress address) {
		this.sheetIndex = sheetIndex;
		this.block = new Block(address);
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

	public Block apply(XSSFWorkbook workbook) {
		SheetModifier sheet = new SheetModifier().setWorkbook(workbook);

		try {
			return apply(sheet.open(sheetIndex).clearMergedRegion(), null, new Block.Vector());
		} finally {
			sheet.close();
		}
	}

	protected Block apply(SheetModifier sheet, Block boundaries, Block.Vector baseShift) {
		Collections.sort(ranges, Comparator);

		ApplicationServer.getMonitor().logInfo("Ranges sorted: " + ranges);

		SheetModifier.CellVisitor visitor = new SheetModifier.CellVisitor() {
			@Override
			public void visit(Row row, int colNum, Cell cell) {
				if (cell == null || cell.getCellTypeEnum() != CellType.STRING)
					return;

				Object value = source.evaluate(cell.getStringCellValue());
				cell.setCellValue(value != null ? value.toString() : "null");
			}
		};

		source.prepare(sheet);

		Block targetBoundaries = boundaries != null ? boundaries.shift(baseShift) : null;
		Block target = block.shift(baseShift);
		Block filled = new Block(target.start(), new Block.Vector());
		Block.Vector shift = baseShift;

		//sheet.clearMergedRegion(boundaries);

		try {
			source.open();

			while (source.next()) {
				if (source.getIndex() > 0) {
					if (targetBoundaries != null)
						for (Block band : targetBoundaries.bandExclusive(target, direction))
							sheet.clear(band);
					sheet.copy(block, target.start(), false);
				}

				for (Range range : ranges)
					target = Block.boundaries(range.apply(sheet, block, shift), target);

				sheet.visitCells(target, visitor);

				filled = Block.boundaries(filled, target);
				shift = shift.add(target.vector(direction, 1));
				target = block.shift(shift);
			}
		} finally {
			source.close();
		}

		Block.Vector stretch = filled.diffSize(block);

		// Spread static cells AFTER inserted cells
		for (Block.Direction direction : Block.Direction.values()) {
			Block source = boundaries != null ? boundaries.bandAfter(block, direction) : sheet.getBoundariesAfter(block, direction);
			Block.Vector component = stretch.component(direction);
			if (!component.isZero()) {
				//sheet.clearMergedRegion(source);
				shift = baseShift.add(component);
				sheet.copy(source, shift, true);
				if (boundaries != null)
					sheet.clearMergedRegion(source); //.copyMergedRegions(source, shift, true);
			}
		}

		if (targetBoundaries != null)
			targetBoundaries = targetBoundaries.stretch(stretch);

		//sheet.applyMergedRegions(boundaries, block, targetBoundaries, filled, direction);

		ApplicationServer.getMonitor().logInfo("Report '" + options.getName() + "':"
				+ "\n\t- range " + block.toAddress() + " -> " + baseShift + ", " + filled
				+ "\n\t- boundaries " + boundaries + " -> " + targetBoundaries
				+ "\n\t- stat: " + sheet.getStat());

		return targetBoundaries;
	}

	@Override
	public String toString() {
		return "Range[" + block.toAddress() + ']';
	}

	private DataSource toDataSource(OBJECT source) {
		if (source instanceof Query)
			return new QuerySource(this, (Query) source);
		if (source instanceof JsonArray)
			return new JsonSource(this, (JsonArray) source);
		return null;
	}
}
