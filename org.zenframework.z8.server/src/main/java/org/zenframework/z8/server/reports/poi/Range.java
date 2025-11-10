package org.zenframework.z8.server.reports.poi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.reports.poi.math.Block;
import org.zenframework.z8.server.reports.poi.math.Direction;
import org.zenframework.z8.server.reports.poi.math.Vector;
import org.zenframework.z8.server.runtime.OBJECT;

public class Range {

	public static Comparator<Range> Comparator = new Comparator<Range>() {
		@Override
		public int compare(Range o1, Range o2) {
			return Block.Comparator.compare(o1.getBoundaries(), o2.getBoundaries());
		}
	};

	private PoiReport report;
	private DataSource source;
	private Block block, boundaries;
	private Direction direction = Direction.Vertical;
	private Range parent;

	private final List<Range> ranges = new ArrayList<Range>();
	private final Set<Block> merges = new HashSet<Block>();

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
		this.source = source.setRange(this);
		return this;
	}

	public Range setSource(OBJECT source) {
		return setSource(DataSource.toDataSource(source));
	}

	public Block getBlock() {
		return block;
	}

	public Range setBlock(Block block) {
		this.block = block;
		return this;
	}

	public Range setBlock(String address) {
		return setBlock(new Block(address));
	}

	public Block getBoundaries() {
		return boundaries != null ? boundaries : parent == null ? null : parent.getRanges().size() == 1 ? parent.getBlock() : block;
	}

	public Range setBoundaries(Block boundaries) {
		this.boundaries = boundaries;
		return this;
	}

	public Range setBoundaries(String boundaries) {
		return setBoundaries(boundaries != null ? new Block(boundaries) : null);
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
		ranges.add(range.setParent(this).setReport(report));

		for (Range r : ranges)
			if (r != range && range.getBoundaries().intersects(r.getBoundaries()))
				throw new IllegalStateException(range + "  intersect " + r + " by boundaries");

		return this;
	}

	public Collection<Block> getInnerBoundaries() {
		Set<Block> boundaries = new HashSet<Block>();

		for (Range range : ranges)
			boundaries.add(range.getBoundaries());

		return boundaries;
	}

	public Range setMerges(Collection<Block> merges) {
		this.merges.clear();
		this.merges.addAll(merges);
		return this;
	}

	public Range setMergesAddress(Collection<String> merges) {
		this.merges.clear();

		for (String merge : merges)
			addMerge(merge);

		return this;
	}

	public Range addMerge(Block merge) {
		merges.add(merge);
		return this;
	}

	public Range addMerge(String merge) {
		return addMerge(new Block(merge));
	}

	public void apply(SheetModifier sheet) {
		apply(sheet, new Vector());
	}

	protected Block apply(SheetModifier sheet, Vector baseShift) {
		Block boundaries = getBoundaries();

		if (boundaries == null)
			boundaries = sheet.getBoundaries();

		if (block == null) {
			if (parent != null)
				throw new IllegalStateException("Range block is not set");
			block = boundaries;
		}

		if (!block.in(boundaries))
			throw new IllegalStateException("Range address " + block + " is out of boundaries " + boundaries);

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

		Block targetBoundaries = boundaries.move(baseShift);
		Block target = block.move(baseShift);
		Block filled = new Block(target.start(), target.size().component(direction.orthogonal()));
		Vector shift = baseShift;

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
					target = Block.boundaries(range.apply(sheet, shift), target); // TODO No data, stretch -1

				sheet.applyInnerMergedRegions(block, shift, getInnerBoundaries());

				sheet.visitCells(target, visitor);

				filled = Block.boundaries(filled, target);
				shift = shift.add(target.size(direction));
				target = block.move(shift);
			}
		} finally {
			source.close();
		}

		Vector stretch = filled.diffSize(block), stretchDir = stretch.component(direction);

		// Spread static cells AFTER inserted cells
		for (Direction direction : Direction.values()) {
			Block source = boundaries.bandAfter(block, direction);
			Vector component = stretch.component(direction);
			int mod = component.mod();
			if (mod != 0)
				sheet.copy(source, baseShift.add(component), true);
			if (mod < 0)
				sheet.clear(targetBoundaries.part(mod, direction));
		}

		targetBoundaries = targetBoundaries.resize(stretch);

		sheet.applyOuterMergedRegions(block, boundaries, baseShift, stretch);

		if (stretchDir.mod() > 0)
			for (Block merge : merges)
				sheet.addMergedRegion(merge.move(baseShift).resize(stretchDir));

		ApplicationServer.getMonitor().logInfo("Report '" + report.getOptions().getName() + "':"
				+ "\n\t- range " + block.toAddress() + " -> " + baseShift + ", " + filled
				+ "\n\t- boundaries " + boundaries + " -> " + targetBoundaries
				+ "\n\t- stat: " + sheet.getStat());

		return targetBoundaries;
	}

	@Override
	public String toString() {
		return "Range[" + block.toAddress() + " / " + getBoundaries() + ']';
	}
}
