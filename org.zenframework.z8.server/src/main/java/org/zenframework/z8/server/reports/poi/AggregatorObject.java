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
	private Vector positionOffset = new Vector();

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

	public void setPositionOffset(Vector offset) {
		this.positionOffset = offset != null ? offset : new Vector();
	}

	private void clearCurrentCell() {
		this.currentPosition = null;
		this.currentValue = null;
	}

	private boolean hasCurrentCell() {
		return currentPosition != null && currentValue != null;
	}

	private Vector targetPos(String addr) {
		return Vector.parseAddress(addr).add(positionOffset);
	}

	private String key(String addr, String suffix) {
		Vector t = targetPos(addr);
		return t.row() + "_" + t.col() + "_" + suffix;
	}

	public decimal sum(string cellAddress) {
		String addr = cellAddress.get();
		AggregatorState state = getState(key(addr, "Sum"));

		if (!hasCurrentCell() || !currentPosition.equals(targetPos(addr)))
			return new decimal(state.sum);

		Double numValue = toDouble(currentValue);
		state.sum += numValue;
		state.count++;
		clearCurrentCell();

		return null;
	}

	public decimal average(string cellAddress) {
		String addr = cellAddress.get();
		AggregatorState state = getState(key(addr, "Average"));

		if (!hasCurrentCell() || !currentPosition.equals(targetPos(addr)))
			return new decimal(state.count > 0 ? state.sum / state.count : 0);

		Double numValue = toDouble(currentValue);
		state.sum += numValue;
		state.count++;
		clearCurrentCell();

		return null;
	}

	public integer count(string cellAddress) {
		String addr = cellAddress.get();
		AggregatorState state = getState(key(addr, "Count"));

		if (!hasCurrentCell() || !currentPosition.equals(targetPos(addr)))
			return new integer(state.count);

		state.count++;
		clearCurrentCell();

		return null;
	}

	public Object max(string cellAddress) {
		String addr = cellAddress.get();
		AggregatorState state = getState(key(addr, "Max"));

		if (!hasCurrentCell() || !currentPosition.equals(targetPos(addr)))
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
		AggregatorState state = getState(key(addr, "Min"));

		if (!hasCurrentCell() || !currentPosition.equals(targetPos(addr)))
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
		positionOffset = new Vector();
	}

	private AggregatorState getState(String key) {
		return accumulated.computeIfAbsent(key, k -> new AggregatorState());
	}

	private Double toDouble(Object value) {
		if (value == null)
			return .0;
		if (value instanceof Number)
			return ((Number) value).doubleValue();
		if (value instanceof integer)
			return (double) ((integer) value).getInt();
		if (value instanceof decimal)
			return ((decimal) value).getDouble();
		try {
			return Double.parseDouble(value.toString().replace(",", "."));
		} catch(Exception e) {
			return .0;
		}
	}
}