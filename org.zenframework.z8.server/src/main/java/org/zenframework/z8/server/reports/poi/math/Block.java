package org.zenframework.z8.server.reports.poi.math;

import java.util.ArrayList;
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

	public Vector size() {
		return size;
	}

	public Vector end() {
		return start.add(size);
	}

	public Vector size(Direction direction) {
		return size.component(direction);
	}

	public int startRow() {
		return start.row();
	}

	public int startCol() {
		return start.col();
	}

	public int endRow() {
		return start.row() + size.row();
	}

	public int endCol() {
		return start.col() + size.col();
	}

	public int height() {
		return size.row();
	}

	public int width() {
		return size.col();
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

	public Block band(Block block, Direction direction) {
		return direction == Direction.Horizontal ? new Block(startRow(), block.startCol(), height(), block.width())
				: new Block(block.startRow(), startCol(), block.height(), width());
	}

	public Block bandBefore(Block block, Direction direction) {
		return direction == Direction.Horizontal ? new Block(startRow(), startCol(), height(), block.startCol() - startCol())
				: new Block(startRow(), startCol(), block.startRow() - startRow(), width());
	}

	public Block bandAfter(Block block, Direction direction) {
		return direction == Direction.Horizontal ? new Block(startRow(), block.endCol(), height(), endCol() - block.endCol())
				: new Block(block.endRow(), startCol(), endRow() - block.endRow(), width());
	}

	public List<Block> bandExclusive(Block block, Direction direction) {
		List<Block> blocks = new ArrayList<Block>(2);

		if (direction == Direction.Horizontal) {
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

	public Block part(int size, Direction direction) {
		return size >= 0 ? new Block(start(), size().component(direction.orthogonal()).add(new Vector(size, direction)))
				: new Block(start().add(size(direction)).add(new Vector(size, direction)), size().component(direction.orthogonal()).add(new Vector(-size, direction)));
	}

	public Vector diffStart(Block block) {
		return start().sub(block.start());
	}

	public Vector diffSize(Block block) {
		return size().sub(block.size());
	}

	public static Block boundaries(Block... blocks) {
		if (blocks.length == 0)
			return null;

		int startRow = Integer.MAX_VALUE, startCol = Integer.MAX_VALUE, endRow = 0, endCol = 0;

		for (Block block : blocks) {
			startRow = Math.min(startRow, block.startRow());
			startCol = Math.min(startCol, block.startCol());
			endRow = Math.max(endRow, block.endRow());
			endCol = Math.max(endCol, block.endCol());
		}

		return new Block(startRow, startCol, endRow - startRow, endCol - startCol);
	}
}
