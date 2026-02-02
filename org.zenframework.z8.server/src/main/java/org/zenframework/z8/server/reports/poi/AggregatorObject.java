package org.zenframework.z8.server.reports.poi;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.reports.poi.math.Vector;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class AggregatorObject {

	private final Map<String, AggregatorState> accumulated = new HashMap<>();

	private Vector currentPosition;
	private Object currentValue;

	private static class AggregatorState {
		double sum;
		int count;
		Comparable<Object> min;
		Comparable<Object> max;
	}

	public void setCurrentCell(Vector position, Object value) {
		this.currentPosition = position;
		this.currentValue = value;
	}

	private void clearCurrentCell() {
		this.currentPosition = null;
		this.currentValue = null;
	}

	private boolean hasCurrentCell() {
		return currentPosition != null && currentValue != null;
	}

	public decimal sum(string cellAddress) {
		String addr = cellAddress.get();
		String key = addr + "_Sum";
		Vector targetPos = Vector.parseAddress(addr);
		AggregatorState state = getState(key);

		if (!hasCurrentCell() || !currentPosition.equals(targetPos))
			return new decimal(state.sum);

		Double numValue = toDouble(currentValue);
		state.sum += numValue;
		state.count++;
		clearCurrentCell();

		return null;
	}

	public decimal average(string cellAddress) {
		String addr = cellAddress.get();
		String key = addr + "_Average";
		Vector targetPos = Vector.parseAddress(addr);
		AggregatorState state = getState(key);

		if (!hasCurrentCell() || !currentPosition.equals(targetPos))
			return new decimal(state.count > 0 ? state.sum / state.count : 0);

		Double numValue = toDouble(currentValue);
		state.sum += numValue;
		state.count++;
		clearCurrentCell();

		return null;
	}

	public integer count(string cellAddress) {
		String addr = cellAddress.get();
		String key = addr + "_Count";
		Vector targetPos = Vector.parseAddress(addr);
		AggregatorState state = getState(key);

		if (!hasCurrentCell() || !currentPosition.equals(targetPos))
			return new integer(state.count);

		state.count++;
		clearCurrentCell();

		return null;
	}

	public Object max(string cellAddress) {
		String addr = cellAddress.get();
		String key = addr + "_Max";
		Vector targetPos = Vector.parseAddress(addr);
		AggregatorState state = getState(key);

		if (!hasCurrentCell() || !currentPosition.equals(targetPos))
			return state.max;

		@SuppressWarnings("unchecked")
		Comparable<Object> comparable = (Comparable<Object>) currentValue;
		if (state.max == null || comparable.compareTo(state.max) > 0)
			state.max = comparable;

		clearCurrentCell();
		return null;
	}

	public Object min(string cellAddress) {
		String addr = cellAddress.get();
		String key = addr + "_Min";
		Vector targetPos = Vector.parseAddress(addr);
		AggregatorState state = getState(key);

		if (!hasCurrentCell() || !currentPosition.equals(targetPos))
			return state.min;

		@SuppressWarnings("unchecked")
		Comparable<Object> comparable = (Comparable<Object>) currentValue;
		if (state.min == null || comparable.compareTo(state.min) < 0)
			state.min = comparable;

		clearCurrentCell();
		return null;
	}

	public void reset() {
		accumulated.clear();
		currentPosition = null;
		currentValue = null;
	}

	private AggregatorState getState(String key) {
		return accumulated.computeIfAbsent(key, k -> new AggregatorState());
	}

	private Double toDouble(Object value) {
		if (value == null)
			return null;

		if (value instanceof Number)
			return ((Number) value).doubleValue();

		if (value instanceof integer)
			return (double) ((integer) value).getInt();

		if (value instanceof decimal)
			return ((decimal) value).getDouble();

		try {
			return Double.parseDouble(value.toString());
		} catch (Exception e) {
			return .0;
		}
	}
}