package org.zenframework.z8.server.reports.poi.math;

public enum Direction {
	Vertical, Horizontal;

	public Direction orthogonal() {
		return this == Vertical ? Horizontal : Vertical;
	}

	public static Direction valueOf(int value) {
		return values()[value];
	}
}
