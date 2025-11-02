package org.zenframework.z8.server.reports.poi;

import org.apache.poi.ss.usermodel.Sheet;
import org.zenframework.z8.server.expression.Expression;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.integer;

public abstract class DataSource {
	private static final String Index = "index";

	protected final Range range;

	protected Wrapper<integer> index;
	private boolean initialized = false;

	protected DataSource(Range range) {
		this.range = range;
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

	public void prepare(Sheet sheet) {
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

		index = getObjectMember(Index);

		if (index == null)
			index = Wrapper.instance(getObject(), Index);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <T extends OBJECT> T getObjectMember(String id) {
		CLASS value = (CLASS) getObject().getMember(id);
		return value != null ? (T) value.get() : null;
	}
}
