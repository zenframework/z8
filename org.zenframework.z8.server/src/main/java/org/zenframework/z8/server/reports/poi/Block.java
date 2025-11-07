package org.zenframework.z8.server.reports.poi;

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

		public Vector copy() {
			return new Vector(row, col);
		}

		public Vector add(Vector vector) {
			return new Vector(row + vector.row, col + vector.col);
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
		public String toString() {
			return new StringBuilder(20).append("{r:").append(row).append(",c:").append(col).append('}').toString();
		}
	}

	private final Vector start, size;

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

	public Block(CellRangeAddress address) {
		this(address.getFirstRow(), address.getFirstColumn(), address.getLastRow() - address.getFirstRow() + 1, address.getLastColumn() - address.getFirstColumn() + 1);
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

	public boolean has(int row, int col) {
		return row >= startRow() && row < endRow() && col >= startCol() && col < endCol();
	}

	public String toAddress() {
		return new StringBuilder(100).append(Util.columnToString(startCol())).append(startRow() + 1)
				.append(':').append(Util.columnToString(endCol() - 1)).append(endRow()).toString();
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

	public Block band(Direction direction, Block block) {
		return direction == Direction.Horizontal ? new Block(startRow(), block.startCol(), height(), block.width())
				: new Block(block.startRow(), startCol(), block.height(), width());
	}

	public Vector diffSize(Block block) {
		return new Vector(height() - block.height(), width() - block.width());
	}

	public Block copy() {
		return new Block(this);
	}
}
