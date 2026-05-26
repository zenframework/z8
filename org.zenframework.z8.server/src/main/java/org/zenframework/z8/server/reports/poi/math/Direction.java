package org.zenframework.z8.server.reports.poi.math;

public enum Direction {

	None(0, 0),
	TopLeft(-1, -1),
	Top(-1, 0),
	TopRight(-1, 1),
	Right(0, 1),
	BottomRight(1, 1),
	Bottom(1, 0),
	BottomLeft(1, -1),
	Left(0, -1);

	private final Vector vector;

	private Direction(int row, int col) {
		vector = new Vector(row, col);
	}

	public boolean is(Axis axis) {
		return vector.component(axis).mod() != 0;
	}
}
