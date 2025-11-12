package org.zenframework.z8.server.reports.poi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.reports.poi.math.Block;
import org.zenframework.z8.server.reports.poi.math.Axis;
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
	private String name;
	private DataSource source;
	private Block block, boundaries;
	private Axis axis = Axis.Vertical;
	private boolean aggregation;
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

	public Range setName(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return name;
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

	public Axis getAxis() {
		return axis;
	}

	public Range setAxis(Axis axis) {
		this.axis = axis;
		return this;
	}

	public Range setAxis(int axis) {
		return setAxis(Axis.valueOf(axis));
	}

	public Range setAggregation(boolean aggregation) {
		this.aggregation = aggregation;
		return this;
	}

	public boolean isAggregation() {
		return aggregation;
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

	protected Vector apply(SheetModifier sheet, Vector baseShift) {
		boundaries = getBoundaries();

		if (boundaries == null)
			boundaries = boundariesFromChildren(sheet);

		if (block == null) {
			if (parent != null)
				throw new IllegalStateException(this + " block is not set");
			block = boundaries;
		}

		if (!block.in(boundaries))
			throw new IllegalStateException(this + " address " + block + " is out of boundaries " + boundaries);

		Collections.sort(ranges, Comparator);

		ApplicationServer.getMonitor().logInfo("Ranges sorted: " + ranges);

		SheetModifier.CellVisitor visitor = getCellVisitor();

		source.prepare(sheet);

		Block target = block.move(baseShift);
		Block filled = new Block(target.start(), block.size().component(axis.orthogonal()));
		Vector shift = baseShift;

		if (!baseShift.isZero())
			sheet.copy(boundaries, boundaries.move(baseShift).start(), false);

		try {
			source.open();

			while (source.next()) {
				if (!shift.isZero())
					sheet.copy(block, target.start(), false);

				target = target.resize(applyInnerRanges(sheet, shift));

				sheet.applyInnerMergedRegions(block, shift, getInnerBoundaries())
						.visitCells(target, visitor);

				filled = Block.boundaries(filled, target);
				shift = shift.add(target.size(axis));
				target = block.move(shift);
			}
		} finally {
			source.close();
		}

		Vector resize = filled.diffSize(block);

		afterApply(sheet, baseShift, resize, filled);
/*
		ApplicationServer.getMonitor().logInfo("Report '" + report.getOptions().getName() + "':"
				+ "\n\t- range " + block.toAddress() + " -> " + baseShift + ", " + filled
				+ "\n\t- boundaries " + boundaries + " -> " + boundaries.move(baseShift).resize(resize)
				+ "\n\t- stat: " + sheet.getStat());
*/
		return resize;
	}

	private Vector applyInnerRanges(SheetModifier sheet, Vector shift) {
		if (ranges.isEmpty())
			return new Vector();

		List<Vector> resizes = new ArrayList<Vector>(ranges.size());
		Block target = new Block(block.move(shift).start(), new Vector());

		for (Range range : ranges) {
			Vector rangeShift = shift;

			for (int i = 0; i < resizes.size(); i++) {
				for (Axis axis : Axis.values())
					if (range.getBlock().start(axis).mod() >= ranges.get(i).getBlock().end(axis).mod())
						rangeShift = rangeShift.add(resizes.get(i).component(axis));
			}

			Vector rangeResize = range.apply(sheet, rangeShift);
			resizes.add(rangeResize);
			target = Block.boundaries(range.getBoundaries().move(rangeShift).resize(rangeResize), target);
		}

		return target.diffSize(block);
	}

	private void afterApply(SheetModifier sheet, Vector baseShift, Vector resize, Block filled) {
		Block targetBoundaries = boundaries.move(baseShift);

		for (Axis axis : Axis.values()) {
			// Clear cells around new filled cells
			if (resize.component(axis).mod() > 0) {
				for (Block band : targetBoundaries.resize(resize).bandExclusive(filled.bandAfter(block.move(baseShift), axis), axis)) {
					if (band.square() > 0)
						sheet.clear(band);
				}
			}

			// Spread static cells AFTER inserted cells
			Block source = boundaries.bandAfter(block, axis);
			Vector component = resize.component(axis);
			int mod = component.mod();
			if (mod != 0)
				sheet.copy(source, baseShift.add(component), true);
			if (mod < 0)
				sheet.clear(targetBoundaries.part(mod, axis));

			// Apply additional merged regions
/*
			if (stretchDir.mod() > 0)
				for (Block merge : merges)
					sheet.addMergedRegion(merge.move(baseShift).resize(stretchDir));
*/
		}

		sheet.applyOuterMergedRegions(block, boundaries, baseShift, resize);
	}

	@Override
	public String toString() {
		return new StringBuilder(30).append("Range[").append(name != null ? name : "?").append(": ")
				.append(block != null ? block.toAddress() : '-').append(" / ").append(boundaries != null ? boundaries : '-').append(']').toString();
	}

	private Block boundariesFromChildren(SheetModifier sheet) {
		Collection<Block> children = new LinkedList<Block>();

		for (Range range : ranges)
			if (range.boundaries != null)
				children.add(range.boundaries);

		Block boundaries = Block.boundaries(children);

		if (boundaries != null)
			return boundaries;

		if (ranges.size() > 1)
			throw new IllegalStateException(this + ": multiple ranges must have defined boundaries");

		return sheet.getBoundaries();
	}

	private SheetModifier.CellVisitor getCellVisitor() {
		return new SheetModifier.CellVisitor() {
			@Override
			public void visit(Row row, int colNum, Cell cell) {
				if (cell == null || cell.getCellTypeEnum() != CellType.STRING)
					return;

				Object value = source.evaluate(cell.getStringCellValue());
				cell.setCellValue(value != null ? value.toString() : "null");
			}
		};
	}
}
