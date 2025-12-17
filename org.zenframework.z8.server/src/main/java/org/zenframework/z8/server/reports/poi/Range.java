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
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.reports.poi.math.Block;
import org.zenframework.z8.server.expression.DefaultContext;
import org.zenframework.z8.server.expression.ObjectContext;
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

	private static final boolean Trace = ServerConfig.reportPoiTrace();

	private PoiReport report;
	private String name;
	private DataSource source;
	private Block block, boundaries;
	private Axis axis = Axis.Vertical;
	private boolean aggregation;
	private Range parent;
	private String subtotalsBy;
	private Block subtotalBlock = null;

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
		if (block == null) {
			if (parent != null)
				throw new IllegalStateException(this + " block is not set");
			block = boundaries;
		}

		return block;
	}

	public Range setBlock(Block block) {
		this.block = block;
		return this;
	}

	public Range setBlock(String address) {
		return setBlock(address != null ? new Block(address) : null);
	}

	public Block getBoundaries() {
		return boundaries != null ? boundaries
				: parent == null ? null : parent.getRanges().size() == 1 ? parent.getBlock() : block;
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

	public Range setSubtotalBlock(Block subtotalBlock) {
		this.subtotalBlock = subtotalBlock;
		return this;
	}

	public Range setSubtotalBlock(String subtotalAddress) {
		return setSubtotalBlock(subtotalAddress != null ? new Block(subtotalAddress) : null);
	}

	public Block getSubtotalBlock() {
		return subtotalBlock;
	}

	public Range setSubtotalsBy(String subtotalsBy) {
		this.subtotalsBy = subtotalsBy;
		return this;
	}

	public String getSubtotalsBy() {
		return subtotalsBy;
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

		block = getBlock();
		Block originalBlock = block;

		if (subtotalBlock != null) {
			block = block.bandBefore(subtotalBlock, axis);
		}

		if (!block.in(boundaries))
			throw new IllegalStateException(this + " address " + block + " is out of boundaries " + boundaries);

		Collections.sort(ranges, Comparator);

		if (Trace && ranges.size() > 1)
			ApplicationServer.getMonitor().logInfo("Report '" + report.getOptions().getName() + "': " + ranges);

		source.prepare(sheet);

		Object previousGroupValue = null;
		AggregatorObject aggregatorObj = null;
		boolean firstRow = true;

		SheetModifier.CellVisitor dataVisitor;
		SheetModifier.CellVisitor subtotalVisitor = null;

		if (subtotalsBy != null && !subtotalsBy.isEmpty() && subtotalBlock != null) {
			aggregatorObj = new AggregatorObject();

			ObjectContext context = new ObjectContext(DefaultContext.create().setVariable("agg", aggregatorObj),
					report.getContext());

			report.getExpression().setContext(context);

			dataVisitor = getAccumulatingVisitor(sheet, subtotalBlock, aggregatorObj);
			subtotalVisitor = getBaseVisitor();

		} else {
			dataVisitor = getBaseVisitor();
		}

		Block target = block.move(baseShift);
		Block filled = new Block(target.start(), block.size().component(axis.orthogonal()));
		Vector shift = baseShift;

		if (!baseShift.isZero())
			sheet.copy(boundaries, boundaries.move(baseShift).start(), false);

		try {
			source.open();

			while (source.next()) {

				if (aggregatorObj != null && !firstRow) {
					Object currentValue = source.getCurrentValue(subtotalsBy);

					if (!previousGroupValue.equals(currentValue)) {
						Vector subtotalPosition = filled.end(axis);
						Vector subtotalShift = insertSubtotalRow(sheet, subtotalPosition, aggregatorObj,
								subtotalVisitor);
						shift = shift.add(subtotalShift);
						target = block.move(shift);

						aggregatorObj.reset();
					}
				}

				if (!shift.isZero())
					sheet.copy(block, target.start(), false);

				Block oldTarget = target;
				target = target.resize(applyInnerRanges(sheet, shift));

				Vector currentShift = target.start().sub(block.start());

				sheet.applyInnerMergedRegions(block, shift, getInnerBoundaries()).visitSheetCells(currentShift,
						oldTarget, dataVisitor);

				filled = Block.boundaries(filled, target);
				shift = shift.add(target.size(axis));
				target = block.move(shift);

				if (aggregatorObj != null) {
					previousGroupValue = source.getCurrentValue(subtotalsBy);
					firstRow = false;
				}
			}

			if (aggregatorObj != null && !firstRow) {
				Vector subtotalPosition = filled.end(axis);
				Vector subtotalShift = insertSubtotalRow(sheet, subtotalPosition, aggregatorObj, subtotalVisitor);
				filled = filled.resize(subtotalShift);

				aggregatorObj.reset();
			}

		} finally {
			source.close();
		}

		block = originalBlock;

		Vector resize = filled.diffSize(block);

		afterApply(sheet, baseShift, resize, filled);

		if (Trace)
			ApplicationServer.getMonitor()
					.logInfo("Report '" + report.getOptions().getName() + "':" + "\n\t- range " + block.toAddress()
							+ " -> " + baseShift + ", " + filled + "\n\t- boundaries " + boundaries + " -> "
							+ boundaries.move(baseShift).resize(resize) + "\n\t- stat: " + sheet.getStat());

		return resize;
	}

	private Vector insertSubtotalRow(SheetModifier sheet, Vector subtotalPosition, AggregatorObject aggregatorObj,
			SheetModifier.CellVisitor subtotalVisitor) {
		if (subtotalBlock == null)
			return new Vector(0, 0);

		Block subtotalTarget = subtotalBlock.move(subtotalPosition.sub(subtotalBlock.start()));

		sheet.copy(subtotalBlock, subtotalTarget.start(), false);

		sheet.visitSheetCells(null, subtotalTarget, subtotalVisitor);

		return subtotalBlock.size(axis);
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
				for (Block band : targetBoundaries.resize(resize)
						.bandExclusive(filled.bandAfter(block.move(baseShift), axis), axis)) {
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
			 * if (stretchDir.mod() > 0) for (Block merge : merges)
			 * sheet.addMergedRegion(merge.move(baseShift).resize(stretchDir));
			 */
		}

		sheet.applyOuterMergedRegions(block, boundaries, baseShift, resize);
	}

	@Override
	public String toString() {
		return new StringBuilder(30).append("Range[").append(name != null ? name : "?").append(": ")
				.append(block != null ? block.toAddress() : '-').append(" / ")
				.append(boundaries != null ? boundaries : '-').append(']').toString();
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

	private SheetModifier.CellVisitor getAccumulatingVisitor(SheetModifier sheet, Block subtotalBlock,
			AggregatorObject aggregatorObj) {
		return new SheetModifier.CellVisitor() {
			@Override
			public void visit(Row row, int colNum, Cell cell, Vector shift) {
				if (cell == null || cell.getCellTypeEnum() != CellType.STRING)
					return;

				String cellContent = cell.getStringCellValue();
				Object value = source.evaluate(cellContent);
				cell.setCellValue(value != null ? value.toString() : "null");

				Vector absolutePos = new Vector(row.getRowNum(), colNum);
				Vector templatePos = absolutePos.sub(shift);

				aggregatorObj.setCurrentCell(templatePos, value);

				sheet.visitOriginCells(subtotalBlock, new SheetModifier.CellVisitor() {
					@Override
					public void visit(Row row, int colNum, Cell subtotalCell, Vector shift) {
						if (subtotalCell != null && subtotalCell.getCellTypeEnum() == CellType.STRING)
							source.evaluate(subtotalCell.getStringCellValue());
					}
				});

			}
		};

	}

	private SheetModifier.CellVisitor getBaseVisitor() {
		return new SheetModifier.CellVisitor() {
			@Override
			public void visit(Row row, int colNum, Cell cell, Vector shift) {
				if (cell == null || cell.getCellTypeEnum() != CellType.STRING)
					return;

				String cellContent = cell.getStringCellValue();
				Object value = source.evaluate(cellContent);
				cell.setCellValue(value != null ? value.toString() : "null");
			}
		};
	}

}
