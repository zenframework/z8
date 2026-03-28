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
import org.zenframework.z8.server.base.json.Json;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.expression.DefaultContext;
import org.zenframework.z8.server.expression.ObjectContext;
import org.zenframework.z8.server.reports.poi.math.Axis;
import org.zenframework.z8.server.reports.poi.math.Block;
import org.zenframework.z8.server.reports.poi.math.Vector;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

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
	private Vector groupStartPosition = null;
	private Vector lastResize = new Vector();
	private SheetModifier.CellVisitor customVisitor = null;

	private final List<Range> ranges = new ArrayList<Range>();
	private final Set<Block> subtotalMerges = new HashSet<Block>();
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

	private void parseMergesString(String mergeString, Set<Block> targetSet) {
		if (mergeString == null || mergeString.isEmpty())
			return;

		String[] blocks = mergeString.split(",");
		for (String block : blocks) {
			String trimmed = block.trim();
			if (!trimmed.isEmpty())
				targetSet.add(new Block(trimmed));
		}
	}

	public Range setMerges(String mergeString) {
		parseMergesString(mergeString, merges);
		return this;
	}

	public Range setSubtotalMerges(String mergeString) {
		parseMergesString(mergeString, subtotalMerges);
		return this;
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

	public Vector getLastResize() {
		return lastResize;
	}
	
	public Range setCustomVisitor(SheetModifier.CellVisitor visitor) {
		this.customVisitor = visitor;
		return this;
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
		
		if (subtotalsBy != null && !subtotalsBy.isEmpty()) {
			aggregatorObj = new AggregatorObject();

			ObjectContext context = new ObjectContext(
				DefaultContext.create()
					.setVariable("agg", aggregatorObj)
					.setVariable(Json.parameters.get(), ApplicationServer.getRequest().getParameters()),
				report.getContext()
			);

			report.getExpression().setContext(context);

			if (subtotalBlock != null)
				dataVisitor = getAccumulatingVisitor(sheet, subtotalBlock, aggregatorObj);
			else
				dataVisitor = getBaseVisitor();

		} else {
			dataVisitor = getBaseVisitor();
		}

		Block target = block.move(baseShift);
		Block filled = new Block(target.start(), block.size().component(axis.orthogonal()));
		Vector shift = baseShift;
		groupStartPosition = baseShift;

		if (!baseShift.isZero())
			sheet.copy(boundaries, boundaries.move(baseShift).start(), false);

		try {
			source.open();

			while (source.next()) {
				if (aggregatorObj != null && !firstRow) {
					Object currentValue = source.getCurrentValue(subtotalsBy);

					if (!objectsEqual(previousGroupValue, currentValue)) {
						Vector groupResize = shift.sub(Vector.unit(axis)).sub(groupStartPosition);

						sheet.applyGroupMerges(groupStartPosition, groupResize, subtotalMerges, block, axis);

						Vector subtotalPosition = filled.end(axis);

						Vector subtotalShift = insertSubtotalRow(sheet, subtotalPosition, aggregatorObj);

						shift = shift.add(subtotalShift);
						target = block.move(shift);
						groupStartPosition = shift;

						aggregatorObj.reset();
					}
				}

				if (!shift.isZero())
					sheet.copy(block, target.start(), false);

				target = target.resize(applyInnerRanges(sheet, shift));

				SheetModifier.CellVisitor visitor = customVisitor != null ? customVisitor : dataVisitor;
				sheet.applyInnerMergedRegions(block, shift, getInnerBoundaries())
					.visitSheetCells(shift, target, visitor);

				filled = Block.boundaries(filled, target);
				shift = shift.add(target.size(axis));
				target = block.move(shift);

				if (aggregatorObj != null) {
					previousGroupValue = source.getCurrentValue(subtotalsBy);
					firstRow = false;

				}
			}

			if (aggregatorObj != null && !firstRow) {
				Vector groupResize = shift.sub(Vector.unit(axis)).sub(groupStartPosition);

				sheet.applyGroupMerges(groupStartPosition, groupResize, subtotalMerges, block, axis);

				Vector subtotalPosition = filled.end(axis);
				Vector subtotalShift = insertSubtotalRow(sheet, subtotalPosition, aggregatorObj);
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

		this.lastResize = resize;
		return resize;
	}

	private boolean objectsEqual(Object a, Object b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		return a.equals(b);
	}

	private Vector insertSubtotalRow(SheetModifier sheet, Vector subtotalPosition,
			AggregatorObject aggregatorObj) {
		if (subtotalBlock == null)
			return new Vector();

		final Vector axisShift = subtotalPosition.component(axis)
			.sub(subtotalBlock.start().component(axis));

		Vector templateOffset = subtotalBlock.start().sub(block.start());

		final Range subtotalRange = new Range()
			.setReport(report)
			.setBlock(subtotalBlock)
			.setBoundaries(subtotalBlock)
			.setSource(new SimpleSource(report.getContext()))
			.setAxis(axis);

		int cumul = 0;
		for (Range child : ranges) {
			final Axis childAxis = child.getAxis();
			final int childCumul = cumul;

			Range subtotalChild = new Range()
					.setReport(report)
					.setBlock(child.getBlock().move(templateOffset))
					.setBoundaries(child.getBoundaries() != null
						? child.getBoundaries().move(templateOffset)
						: child.getBlock().move(templateOffset))
					.setSource(child.getSource())
					.setAxis(child.getAxis());

			subtotalChild.setCustomVisitor(new SheetModifier.CellVisitor() {
				@Override
				public void visit(Row row, int colNum, Cell cell, Vector shift) {
					if (cell == null || cell.getCellTypeEnum() != CellType.STRING)
						return;

					Vector ownShift = shift != null ? shift.sub(axisShift) : new Vector();
					int ownOffset = ownShift.component(childAxis).mod();
					Vector offsetForAgg = childAxis == Axis.Horizontal
							? new Vector(0, childCumul + ownOffset)
							: new Vector(childCumul + ownOffset, 0);

					aggregatorObj.setPositionOffset(offsetForAgg);

					evaluateAndSet(cell, cell.getStringCellValue());

					aggregatorObj.setPositionOffset(new Vector());
				}
			});

			subtotalRange.addRange(subtotalChild);
			cumul += child.getLastResize().component(child.getAxis()).mod();
		}

		subtotalRange.setCustomVisitor(new SheetModifier.CellVisitor() {
			@Override
			public void visit(Row row, int colNum, Cell cell, Vector shift) {
				if (cell == null)
					return;

				Vector livePos = new Vector(row.getRowNum(), colNum);
				Vector shiftedTemplatePos = livePos.sub(axisShift);
				PositionInfo info = resolvePosition(subtotalRange.getRanges(), shiftedTemplatePos);

				if (info.isChildCell)
					return;

				Vector templatePos = info.axis == Axis.Horizontal
						? new Vector(shiftedTemplatePos.row(), shiftedTemplatePos.col() - info.cumulExpansion)
						: new Vector(shiftedTemplatePos.row() - info.cumulExpansion, shiftedTemplatePos.col());
				
				Block originBlock = new Block(templatePos.row(), templatePos.col(), 1, 1);

				sheet.visitOriginCells(originBlock, new SheetModifier.CellVisitor() {
					@Override
					public void visit(Row originRow, int originCol, Cell originCell, Vector originShift) {
						if (originCell == null || originCell.getCellTypeEnum() != CellType.STRING)
							return;

						aggregatorObj.setPositionOffset(info.toOffset());
						evaluateAndSet(cell, originCell.getStringCellValue());
						aggregatorObj.setPositionOffset(new Vector());
					}
				});
			}
		});

		subtotalRange.apply(sheet, axisShift);

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

		sheet.applyOuterMergedRegions(block, boundaries, baseShift, resize, merges);
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
	
	private SheetModifier.CellVisitor getAccumulatingVisitor(SheetModifier sheet,
			Block subtotalBlock, AggregatorObject aggregatorObj) {
		return new SheetModifier.CellVisitor() {
			@Override
			public void visit(Row row, int colNum, Cell cell, Vector shift) {
				if (cell == null)
					return;

				CellType type = cell.getCellTypeEnum();
				Object value;

				if (type == CellType.STRING) {
					String cellContent = cell.getStringCellValue();
					value = evaluateAndSet(cell, cellContent);
				} else if (type == CellType.NUMERIC) {
					value = cell.getNumericCellValue();
				} else {
					return;
				}

				Vector absolutePos = new Vector(row.getRowNum(), colNum);
				Vector templatePos = absolutePos.sub(shift);

				aggregatorObj.setCurrentCell(templatePos, value);

				Vector offset = resolvePosition(ranges, templatePos).toOffset();
				aggregatorObj.setPositionOffset(offset);

				sheet.visitOriginCells(subtotalBlock, new SheetModifier.CellVisitor() {
					@Override
					public void visit(Row row, int colNum, Cell subtotalCell, Vector shift) {
						if (subtotalCell != null && subtotalCell.getCellTypeEnum() == CellType.STRING)
							source.evaluate(subtotalCell.getStringCellValue());
					}
				});

				aggregatorObj.setPositionOffset(new Vector());
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
				evaluateAndSet(cell, cellContent);
			}
		};
	}
	
	private static class PositionInfo {
		final boolean isChildCell;
		final int cumulExpansion;
		final int offset;
		final Axis axis;

		PositionInfo(boolean isChildCell, int cumulExpansion, Axis axis) {
			this(isChildCell, cumulExpansion, axis, cumulExpansion);
		}

		PositionInfo(boolean isChildCell, int cumulExpansion, Axis axis, int offset) {
			this.isChildCell = isChildCell;
			this.cumulExpansion = cumulExpansion;
			this.axis = axis;
			this.offset = offset;
		}

		Vector toOffset() {
			return axis == Axis.Horizontal ? new Vector(0, offset) : new Vector(offset, 0);
		}
	}
	
	private PositionInfo resolvePosition(List<Range> ranges, Vector templatePos) {
		int cumulativeExpansion = 0;
		Axis defaultAxis = Axis.Horizontal;

		for (Range child : ranges) {
			Axis childAxis = child.getAxis();
			defaultAxis = childAxis;
			int childOwnExpansion = child.getLastResize().component(childAxis).mod();
			int childStart = child.getBlock().start(childAxis).mod();
			int childEnd = child.getBlock().end(childAxis).mod();
			int childSize = child.getBlock().size(childAxis).mod();
			int pos = templatePos.component(childAxis).mod();

			int adjustedStart = childStart + cumulativeExpansion;
			int adjustedEnd = childEnd + cumulativeExpansion + childOwnExpansion;

			if (pos < adjustedStart)
				return new PositionInfo(false, cumulativeExpansion, childAxis);

			if (pos < adjustedEnd) {
				int copyIndex = (pos - adjustedStart) / childSize;
				return new PositionInfo(true, cumulativeExpansion, childAxis, cumulativeExpansion + copyIndex * childSize);
			}

			cumulativeExpansion += childOwnExpansion;
		}

		return new PositionInfo(false, cumulativeExpansion, defaultAxis);
	}
	
	private Object evaluateAndSet(Cell cell, String expression) {
		Object value = source.evaluate(expression);
		if (value instanceof Number)
			cell.setCellValue(((Number) value).doubleValue());
		else if (value instanceof integer)
			cell.setCellValue(((integer) value).getInt());
		else if (value instanceof decimal)
			cell.setCellValue(((decimal) value).getDouble());
		else if (value instanceof bool)
			cell.setCellValue(((bool) value).get());
		else if (value instanceof date)
			cell.setCellValue(((date) value).get());
		else if (value instanceof string)
			cell.setCellValue(((string) value).get());
		else
			cell.setCellValue(value != null ? value.toString() : "");
		return value;
	}
}
