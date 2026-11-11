package org.zenframework.z8.server.reports.poi;

import org.zenframework.z8.server.runtime.OBJECT;

public class AggregatedSource extends DataSource {

	private final DataSource origin;
	private final AggregatorObject aggregator;
	private final OBJECT object;
	private final int count;

	public AggregatedSource(DataSource origin, AggregatorObject aggregator, OBJECT object, int count) {
		this.origin = origin;
		this.aggregator = aggregator;
		this.object = object;
		this.count = count;
	}

	public DataSource getOrigin() {
		return origin;
	}

	@Override
	public OBJECT getObject() {
		return object;
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public String normalize(String id) {
		return origin.normalize(id);
	}

	@Override
	public boolean next() {
		super.next();

		if (getIndex() >= count)
			return false;

		origin.fillAggregated(this);

		return true;
	}

	public Object getValue(String id) {
		return getAggregatedValue(aggregator, id);
	}

}
