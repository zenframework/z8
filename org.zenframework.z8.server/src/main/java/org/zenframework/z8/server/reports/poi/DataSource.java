package org.zenframework.z8.server.reports.poi;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.expression.Expression;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.integer;

public abstract class DataSource {
	private static final String Index = "index";

	protected Range range;
	protected Wrapper<integer> index;
	private boolean initialized = false;

	public static DataSource toDataSource(OBJECT source) {
		if (source instanceof Query)
			return new QuerySource((Query) source);
		if (source instanceof JsonArray)
			return new JsonSource((JsonArray) source);
		return new SimpleSource(source);
	}

	public DataSource setRange(Range range) {
		this.range = range;
		return this;
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
	}

	public boolean next() {
		index.set(new integer(index.get().getInt() + 1));
		return internalNext();
	}

	public void close() {}

	protected abstract boolean internalNext();

	public Object evaluate(String value) {
		return getExpression().evaluateText(value);
	}

	public abstract OBJECT getObject();

	public abstract int count();

	protected void initialize() {
		initialized = true;
		index = getObjectProperty(Index);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <V> Wrapper<V> getObjectProperty(String id) {
		CLASS value = (CLASS) getObject().getMember(id);
		return value != null ? (Wrapper<V>) value.get() : Wrapper.instance(getObject(), id);
	}

	public Object getCurrentValue(String fieldId) {
		return null;
	}

}
