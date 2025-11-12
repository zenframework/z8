package org.zenframework.z8.server.reports.poi.math;

public enum Axis {
	Vertical, Horizontal;

	public Axis orthogonal() {
		return this == Vertical ? Horizontal : Vertical;
	}

	public static Axis valueOf(int value) {
		return values()[value];
	}
}
