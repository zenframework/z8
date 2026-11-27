package org.zenframework.z8.server.reports.poi;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.types.integer;

public class AggregatorObject {

	private final Map<String, Aggregator> accumulated = new HashMap<String, Aggregator>();

	public void accumulate(String key, Object value, Aggregation aggregation) {
		if (value == null)
			return;

		getAggregator(key, aggregation).accumulate(value);
	}

	public Object get(String key, Aggregation aggregation) {
		Aggregator aggregator = accumulated.get(key);

		if (aggregator == null)
			return aggregation == Aggregation.Count ? new integer(0) : null;

		return aggregator.get();
	}

	public void reset() {
		accumulated.clear();
	}

	private Aggregator getAggregator(String key, Aggregation aggregation) {
		Aggregator aggregator = accumulated.get(key);

		if (aggregator == null)
			accumulated.put(key, aggregator = Aggregator.newInstance(aggregation));

		return aggregator;
	}
}
