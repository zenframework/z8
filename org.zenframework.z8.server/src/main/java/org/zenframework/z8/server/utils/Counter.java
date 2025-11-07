package org.zenframework.z8.server.utils;

public class Counter {

	private final int step, count;
	private int value;
	int index = 0;

	public Counter(int value, int step, int count) {
		this.value = value - step;
		this.step = step;
		this.count = count;
	}

	public boolean next() {
		value += step;
		return index++ < count;
	}

	public int get() {
		return value;
	}

	public boolean isFinished() {
		return index >= count;
	}
}
