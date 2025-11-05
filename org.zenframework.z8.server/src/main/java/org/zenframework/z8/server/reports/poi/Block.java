package org.zenframework.z8.server.reports.poi;

import org.apache.poi.ss.util.CellRangeAddress;

public class Block {

	public static enum Direction {
		Vertical(0), Horizontal(1);

		private final int value;

		private Direction(int value) {
			this.value = value;
		}

		public static Direction valueOf(int value) {
			return value == Horizontal.value ? Horizontal : Vertical;
		}
	}

	public static class Diff {
		private int rows, cols;

		public Diff() {
			this(0, 0);
		}

		public Diff(int rows, int cols) {
			this.rows = rows;
			this.cols = cols;
		}

		public Diff copy() {
			return new Diff(rows, cols);
		}

		public Diff add(Diff diff) {
			return new Diff(rows + diff.rows, cols + diff.cols);
		}

		@Override
		public String toString() {
			return new StringBuilder(20).append("{r:").append(rows).append(",c:").append(cols).append('}').toString();
		}
	}

	private int startRow, startCol, height, width;

	public Block() {}

	public Block(Block block) {
		this(block.startRow(), block.startCol(), block.height(), block.width());
	}

	public Block(int startRow, int startCol, int height, int width) {
		checkPositive(startRow, startCol, height, width);
		this.startRow = startRow;
		this.startCol = startCol;
		this.height = height;
		this.width = width;
	}

	public Block(CellRangeAddress address) {
		this(address.getFirstRow(), address.getFirstColumn(), address.getLastRow() - address.getFirstRow() + 1, address.getLastColumn() - address.getFirstColumn() + 1);
	}

	public Block setStart(int startRow, int startCol) {
		this.startRow = startRow;
		this.startCol = startCol;
		return this;
	}

	public Block setSize(int height, int width) {
		this.height = height;
		this.width = width;
		return this;
	}

	public int startRow() {
		return startRow;
	}

	public int startCol() {
		return startCol;
	}

	public int endRow() {
		return startRow + height;
	}

	public int endCol() {
		return startCol + width;
	}

	public int height() {
		return height;
	}

	public int width() {
		return width;
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
		return new StringBuilder(100).append(toLetter(startCol())).append(startRow() + 1)
				.append(':').append(toLetter(endCol() - 1)).append(endRow()).toString();
	}

	@Override
	public String toString() {
		return toAddress();
	}

	public CellRangeAddress toCellRangeAddress() {
		return new CellRangeAddress(startRow(), endRow() - 1, startCol(), endCol() - 1);
	}

	public Diff vector(Direction direction, int count) {
		return new Diff(direction == Direction.Horizontal ? 0 : height * count,
				direction == Direction.Horizontal ? width * count : 0);
	}

	public Block shift(Diff shift) {
		return shift != null ? copy().setStart(startRow + shift.rows, startCol + shift.cols) : this;
	}

	public Block shift(Direction direction, int count) {
		return shift(vector(direction, count));
	}

	public Block stretch(Diff diff) {
		return copy().setSize(height + diff.rows, width + diff.cols);
	}

	public Block stretch(Direction direction, int count) {
		return copy().setSize(height * (direction == Direction.Horizontal ? 1 : count),
				width * (direction == Direction.Horizontal ? count : 1));
	}

	public Block band(Direction direction, Block block) {
		return direction == Direction.Horizontal ? new Block(startRow(), block.startCol(), height(), block.width())
				: new Block(block.startRow(), startCol(), block.height(), width());
	}

	public Diff diffSize(Block block) {
		return new Diff(height() - block.height(), width() - block.width());
	}

	public Block copy() {
		return new Block(this);
	}

	private static String toLetter(int n) {
		if (n == 0)
			return "A";

		StringBuilder str = new StringBuilder(10);

		while (n >= 0) {
			str.insert(0, (char) ('A' + n % 26));
			n = n / 26 - 1;
		}

		return str.toString();
	}

	private static void checkPositive(int... values) {
		for (int value : values)
			if (value < 0)
				throw new IllegalArgumentException();
	}
}
