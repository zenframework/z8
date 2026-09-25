package org.zenframework.z8.server.reports.poi;

import java.util.Collection;
import java.util.Collections;

import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.expression.Expression;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.integer;

public abstract class DataSource {
	public static final String Index = "index";
	public static final String Sheet = "sheet";

	protected Range range;
	protected Wrapper<integer> index;
	protected Wrapper<integer> sheet;
	private boolean initialized = false;

	private org.zenframework.z8.server.base.form.report.DataSource metadata;

	public DataSource setRange(Range range) {
		this.range = range;
		return this;
	}

	public Range getRange() {
		return range;
	}

	public DataSource setMetadata(org.zenframework.z8.server.base.form.report.DataSource metadata) {
		this.metadata = metadata;
		return this;
	}

	public Aggregation getAggregation(String id, Aggregation defaultAggregation) {
		return metadata != null ? metadata.getAggregation(id, defaultAggregation) : defaultAggregation;
	}

	public Collection<String> getAggregatedIds() {
		return metadata != null ? metadata.getMetadataIds() : Collections.<String>emptyList();
	}

	public String getId() {
		return getObject().id();
	}

	public int getIndex() {
		return index.get().getInt();
	}

	public Expression getExpression() {
		return range.getReport().getExpression();
	}

	public void prepare(SheetModifier sheet) {
		if (!initialized)
			initialize();
	}

	public void open() {
		index.set(new integer(-1));
		sheet.set(new integer(range.getSheet()));
	}

	public boolean next() {
		index.set(new integer(index.get().getInt() + 1));
		return false;
	}

	public void close() {}

	public Object evaluate(String value) {
		return getExpression().evaluateText(value);
	}

	public abstract OBJECT getObject();

	public abstract int count();

	public Object getCurrentValue(String id) {
		return null;
	}

	public Collection<String> getCurrentValueIds() {
		return Collections.emptyList();
	}

	public void accumulate(AggregatorObject aggregator) {
		for (String id : getAggregatedIds())
			aggregator.accumulate(aggregatorKey(id), getCurrentValue(id), getAggregation(id, Aggregation.None));
	}

	public Object getAggregatedValue(AggregatorObject aggregator, String id) {
		return aggregator.get(aggregatorKey(id), getAggregation(id, Aggregation.None));
	}

	public String normalize(String id) {
		String sourceId = getId();
		return sourceId == null || sourceId.isEmpty() ? id : sourceId + '.' + id;
	}

	protected String relative(String id) {
		String sourceId = getId();
		return sourceId != null && id.startsWith(sourceId + '.') ? id.substring(sourceId.length() + 1) : id;
	}

	private String aggregatorKey(String id) {
		return range.key(normalize(id));
	}

	public DataSource aggregated(AggregatorObject aggregator, int count) {
		OBJECT aggregatedObject = range.getReport().getAggregatedObject(getObject());

		if (aggregatedObject == null)
			return this;

		return new AggregatedSource(this, aggregator, aggregatedObject, count).setMetadata(metadata);
	}

	protected void fillAggregated(AggregatedSource source) {}

	protected static OBJECT getMember(OBJECT object, String id) {
		for (String index : id.split("\\.")) {
			IClass<? extends IObject> member = object.getMember(index);
			IObject value = member != null ? member.get() : null;

			if (!(value instanceof OBJECT))
				return null;

			object = (OBJECT) value;
		}

		return object;
	}

	protected void initialize() {
		initialized = true;
		index = getObjectProperty(Index);
		sheet = getObjectProperty(Sheet);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <V> Wrapper<V> getObjectProperty(String id) {
		CLASS value = (CLASS) getObject().getMember(id);
		return value != null ? (Wrapper<V>) value.get() : addObject(getObject(), id);
	}

	private static <V> Wrapper<V> addObject(OBJECT container, String index) {
		Wrapper.CLASS<Wrapper<V>> instance = new Wrapper.CLASS<Wrapper<V>>(container);
		instance.setIndex(index);
		if (container != null)
			container.objects.add(instance);
		return instance.get();
	}

}
