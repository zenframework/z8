package org.zenframework.z8.server.reports.poi;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.OBJECT;

public class Range {

	public static Comparator<Range> Comparator = new Comparator<Range>() {
		@Override
		public int compare(Range o1, Range o2) {
			return Block.Comparator.compare(o1.getBoundaries(), o2.getBoundaries());
		}
	};

	private ReportOptions options;

	private DataSource source;
	private int sheetIndex = 0;
	private Block block, boundaries;
	private Block.Direction direction;
	private Range parent;

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

	public Range setSheetIndex(int sheetIndex) {
		this.sheetIndex = sheetIndex;
		return this;
	}

	public Block getBlock() {
		return block;
	}

	public Range setBlock(Block block) {
		this.block = block;
		return this;
	}

	public Range setBlock(String address) {
		return setBlock(new Block(CellRangeAddress.valueOf(address)));
	}

	public Block getBoundaries() {
		return boundaries;
	}

	public Block getBoundaries(SheetModifier sheet) {
		return boundaries != null ? boundaries : parent != null ? parent.getBlock() : sheet.getBoundaries();
	}

	public Range setBoundaries(Block boundaries) {
		this.boundaries = boundaries;
		return this;
	}

	public Range setBoundaries(String boundaries) {
		return setBoundaries(boundaries != null ? new Block(CellRangeAddress.valueOf(boundaries)) : null);
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

	public Range getParent() {
		return parent;
	}

	public Range setParent(Range parent) {
		this.parent = parent;
		return this;
	}

	public List<Range> getRanges() {
		return ranges;
	}

	public Range addRange(Range range) {
		this.ranges.add(range.setParent(this));
		return this;
	}

	protected Block apply(SheetModifier sheet, Block.Vector baseShift) {
		Block boundaries = getBoundaries(sheet);

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

		Block targetBoundaries = boundaries.shift(baseShift);
		Block target = block.shift(baseShift);
		Block filled = new Block(target.start(), new Block.Vector());
		Block.Vector shift = baseShift;

		if (!baseShift.isZero())
			sheet.copy(boundaries, targetBoundaries.start(), false);

		try {
			source.open();

			while (source.next()) {
				if (source.getIndex() > 0)
					for (Block band : targetBoundaries.bandExclusive(target, direction))
						sheet.clear(band);

				if (!shift.isZero())
					sheet.copy(block, target.start(), false);

				for (Range range : ranges)
					target = Block.boundaries(range.apply(sheet, shift), target);

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
			Block source = boundaries.bandAfter(block, direction);
			Block.Vector component = stretch.component(direction);
			if (!component.isZero())
				sheet.copy(source, baseShift.add(component), true);
		}

		targetBoundaries = targetBoundaries.stretch(stretch);

		sheet.applyMergedRegions(boundaries, block, targetBoundaries, filled, direction);

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
