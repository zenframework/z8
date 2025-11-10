package org.zenframework.z8.server.reports.poi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;

public class Block {

	public static enum Direction {
		Vertical, Horizontal;

		public static Direction valueOf(int value) {
			return values()[value];
		}
	}

	public static class Vector {
		private final int row, col;

		public Vector() {
			this(0, 0);
		}

		public Vector(Cell cell) {
			this(cell.getRowIndex(), cell.getColumnIndex());
		}

		public Vector(int row, int col) {
			this.row = row;
			this.col = col;
		}

		public int row() {
			return row;
		}

		public int col() {
			return col;
		}

		public Vector copy() {
			return new Vector(row, col);
		}

		public Vector add(Vector vector) {
			return vector != null ? new Vector(row + vector.row, col + vector.col) : this;
		}

		public Vector sub(Vector vector) {
			return vector != null ? new Vector(row - vector.row, col - vector.col) : this;
		}

		public Vector component(Direction direction) {
			return direction == Direction.Horizontal ? new Vector(0, col) : new Vector(row, 0);
		}

		public boolean isZero() {
			return row == 0 && col == 0;
		}

		public Vector checkPositive() {
			if (row < 0 || col < 0)
				throw new IllegalStateException();
			return this;
		}

		public String toAddress() {
			return new StringBuilder(10).append(Util.columnToString(col)).append(row + 1).toString();
		}

		@Override
		public int hashCode() {
			return row ^ col;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof Vector))
				return false;
			Vector v = (Vector) obj;
			return row == v.row() && col == v.col();
		}

		@Override
		public String toString() {
			return new StringBuilder(20).append("{r:").append(row).append(",c:").append(col).append('}').toString();
		}
	}

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

	public Block(Block block) {
		this(block.startRow(), block.startCol(), block.height(), block.width());
	}

	public Block(Vector start, Vector size) {
		this.start = start;
		this.size = size;
	}

	public Block(int startRow, int startCol, int height, int width) {
		this(new Vector(startRow, startCol).checkPositive(), new Vector(height, width).checkPositive());
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

	public int startRow() {
		return start.row;
	}

	public int startCol() {
		return start.col;
	}

	public int endRow() {
		return start.row + size.row;
	}

	public int endCol() {
		return start.col + size.col;
	}

	public int height() {
		return size.row;
	}

	public int width() {
		return size.col;
	}

	public boolean in(Block block) {
		return startRow() >= block.startRow() && startCol() >= block.startCol()
				&& endRow() <= block.endRow() && endCol() <= block.endCol();
	}

	public boolean out(Block block) {
		return block.startCol() >= endCol() || startCol() >= block.endCol()
				|| block.startRow() >= endRow() || startRow() >= block.endRow();
	}

	public boolean intersects(Block block) {
		return !out(block);
	}

	public boolean has(int row, int col) {
		return row >= startRow() && row < endRow() && col >= startCol() && col < endCol();
	}

	public String toAddress() {
		return new StringBuilder(100).append(Util.columnToString(startCol())).append(startRow() + 1)
				.append(':').append(Util.columnToString(endCol() - 1)).append(endRow()).toString();
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

	public Vector vector(Direction direction, int count) {
		return new Vector(direction == Direction.Horizontal ? 0 : height() * count,
				direction == Direction.Horizontal ? width() * count : 0);
	}

	public Block shift(Vector shift) {
		return new Block(start.add(shift), size);
	}

	public Block shift(Direction direction, int shift) {
		return shift(new Vector(direction == Direction.Horizontal ? 0 : shift,
				direction == Direction.Horizontal ? shift : 0));
	}

	public Block shiftMe(Direction direction, int count) {
		return shift(vector(direction, count));
	}

	public Block stretch(Vector vector) {
		return new Block(start, size.add(vector));
	}

	public Block stretchMe(Direction direction, int count) {
		return new Block(start, new Vector(height() * (direction == Direction.Horizontal ? 1 : count),
				width() * (direction == Direction.Horizontal ? count : 1)));
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

	public Vector diffStart(Block block) {
		return start().sub(block.start());
	}

	public Vector diffSize(Block block) {
		return size().sub(block.size());
	}

	public Block copy() {
		return new Block(this);
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
