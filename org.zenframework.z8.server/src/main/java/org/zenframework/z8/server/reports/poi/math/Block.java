package org.zenframework.z8.server.reports.poi.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;
import org.zenframework.z8.server.reports.poi.Util;

public class Block {

	public static final Comparator<Block> Comparator = new Comparator<Block>() {
		@Override
		public int compare(Block o1, Block o2) {
			if (o1 == o2)
				return 0;
			if (o1 == null)
				return -1;
			if (o2 == null)
				return 1;
			if (o1.startRow() < o2.endRow() && o2.startRow() < o1.endRow())
				return Integer.compare(o1.startCol(), o2.startCol());
			if (o1.startCol() < o2.endCol() && o2.startCol() < o1.endCol())
				return Integer.compare(o1.startRow(), o2.startRow());
			return Integer.compare(o1.startCol(), o2.startCol()) + Integer.compare(o1.startRow(), o2.startRow());
		}
	};

	private final Vector start, size;

	public Block() {
		this(0, 0, 0, 0);
	}

	public Block(Vector start, Vector size) {
		this.start = start.checkPositive();
		this.size = size.checkPositive();
	}

	public Block(int startRow, int startCol, int height, int width) {
		this(new Vector(startRow, startCol), new Vector(height, width));
	}

	public Block(String address) {
		this(CellRangeAddress.valueOf(address));
	}

	public Block(CellRangeAddress address) {
		this(address.getFirstRow(), address.getFirstColumn(), address.getLastRow() - address.getFirstRow() + 1, address.getLastColumn() - address.getFirstColumn() + 1);
	}

	public Vector start() {
		return start;
	}

	public Vector start(Axis axis) {
		return start.component(axis);
	}

	public int startRow() {
		return start.row();
	}

	public int startCol() {
		return start.col();
	}

	public Vector size() {
		return size;
	}

	public Vector size(Axis axis) {
		return size.component(axis);
	}

	public int height() {
		return size.row();
	}

	public int width() {
		return size.col();
	}

	public Vector end() {
		return start.add(size);
	}

	public Vector end(Axis axis) {
		return end().component(axis);
	}

	public int endRow() {
		return start.row() + size.row();
	}

	public int endCol() {
		return start.col() + size.col();
	}

	public int square() {
		return size.scalar();
	}

	public boolean in(Block block) {
		return startRow() >= block.startRow() && startCol() >= block.startCol()
				&& endRow() <= block.endRow() && endCol() <= block.endCol();
	}

	public boolean inOneOf(Collection<Block> blocks) {
		for (Block block : blocks)
			if (in(block))
				return true;
		return false;
	}

	public boolean out(Block block) {
		return block.startCol() >= endCol() || startCol() >= block.endCol()
				|| block.startRow() >= endRow() || startRow() >= block.endRow();
	}

	public boolean intersects(Block block) {
		return !out(block);
	}

	public String toAddress() {
		return new StringBuilder(100).append(Util.columnToString(startCol())).append(startRow() + 1)
				.append(':').append(width() > 0 ? Util.columnToString(endCol() - 1) : "#")
						.append(height() > 0 ? endRow() : "#").toString();
	}

	@Override
	public int hashCode() {
		return start.hashCode() ^ size.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Block))
			return false;
		Block b = (Block) obj;
		return start.equals(b.start()) && size.equals(b.size());
	}

	@Override
	public String toString() {
		return toAddress();
	}

	public CellRangeAddress toCellRangeAddress() {
		return new CellRangeAddress(startRow(), endRow() - 1, startCol(), endCol() - 1);
	}

	public Block move(Vector shift) {
		return new Block(start.add(shift), size);
	}

	public Block resize(Vector vector) {
		return new Block(start, size.add(vector));
	}

	public Vector diffStart(Block block) {
		return start().sub(block.start());
	}

	public Vector diffSize(Block block) {
		return size().sub(block.size());
	}

	public Block band(Block block, Axis axis) {
		return axis == Axis.Horizontal ? new Block(startRow(), block.startCol(), height(), block.width())
				: new Block(block.startRow(), startCol(), block.height(), width());
	}

	public Block bandBefore(Block block, Axis axis) {
		return axis == Axis.Horizontal ? new Block(startRow(), startCol(), height(), block.startCol() - startCol())
				: new Block(startRow(), startCol(), block.startRow() - startRow(), width());
	}

	public Block bandAfter(Block block, Axis axis) {
		return axis == Axis.Horizontal ? new Block(startRow(), block.endCol(), height(), endCol() - block.endCol())
				: new Block(block.endRow(), startCol(), endRow() - block.endRow(), width());
	}

	public List<Block> bandExclusive(Block block, Axis axis) {
		List<Block> blocks = new ArrayList<Block>(2);

		if (axis == Axis.Horizontal) {
			if (block.startRow() > startRow())
				blocks.add(new Block(startRow(), block.startCol(), block.startRow() - startRow(), block.width()));
			if (block.endRow() < endRow())
				blocks.add(new Block(block.endRow(), block.startCol(), endRow() - block.endRow(), block.width()));
		} else {
			if (block.startCol() > startCol())
				blocks.add(new Block(block.startRow(), startCol(), block.height(), block.startCol() - startCol()));
			if (block.endCol() < endCol())
				blocks.add(new Block(block.startRow(), block.endCol(), block.height(), endCol() - block.endCol()));
		}

		return blocks;
	}

	public Block part(int size, Axis axis) {
		return size >= 0 ? new Block(start(), size().component(axis.orthogonal()).add(new Vector(size, axis)))
				: new Block(start().add(size(axis)).add(new Vector(size, axis)), size().component(axis.orthogonal()).add(new Vector(-size, axis)));
	}

	public List<Block> grid(Block block) {
		List<Block> grid = new ArrayList<Block>(8);
		return grid;
	}

	public static Block boundaries(Block... blocks) {
		return boundaries(Arrays.asList(blocks));
	}

	public static Block boundaries(Iterable<Block> blocks) {
		int startRow = Integer.MAX_VALUE, startCol = Integer.MAX_VALUE, endRow = 0, endCol = 0;
		boolean empty = true;

		for (Block block : blocks) {
			startRow = Math.min(startRow, block.startRow());
			startCol = Math.min(startCol, block.startCol());
			endRow = Math.max(endRow, block.endRow());
			endCol = Math.max(endCol, block.endCol());
			empty = false;
		}

		return empty ? null : new Block(startRow, startCol, endRow - startRow, endCol - startCol);
	}
}
