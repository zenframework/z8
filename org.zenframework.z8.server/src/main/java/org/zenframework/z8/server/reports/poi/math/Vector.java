package org.zenframework.z8.server.reports.poi.math;

import org.apache.poi.ss.usermodel.Cell;
import org.zenframework.z8.server.reports.poi.Util;

public class Vector {
	private final int row, col;

	public Vector() {
		this(0, 0);
	}

	public Vector(Cell cell) {
		this(cell.getRowIndex(), cell.getColumnIndex());
	}

	public Vector(int value, Direction direction) {
		this(direction == Direction.Horizontal ? 0 : value, direction == Direction.Horizontal ? value : 0);
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

	public int mod() {
		return row + col;
	}

	public Vector mul(int n) {
		return new Vector(row * n, col * n);
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

	public Vector inverse() {
		return new Vector(-row, -col);
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