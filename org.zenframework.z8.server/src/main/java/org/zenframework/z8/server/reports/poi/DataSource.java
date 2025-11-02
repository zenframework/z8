package org.zenframework.z8.server.reports.poi;

import org.apache.poi.ss.usermodel.Sheet;
import org.zenframework.z8.server.expression.Expression;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.OBJECT;

public abstract class DataSource {

	protected final Range range;

	protected Counter counter;
	private boolean initialized = false;

	protected DataSource(Range range) {
		this.range = range;
	}

	public String getId() {
		return getObject().id();
	}

	public int getCounter() {
		return counter.getInt();
	}

	public Expression getExpression() {
		return range.getReport().getExpression();
	}

	public void prepare(Sheet sheet) {
		if (!initialized)
			initialize();
	}

	public void open() {
		counter.reset();
	}

	public boolean next() {
		counter.inc();
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

		counter = getObjectMember(Counter.Index);

		if (counter == null) {
			OBJECT source = getObject();
			counter = new Counter.CLASS<Counter>(source).get();
			source.objects.add(counter.getCLASS());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <T extends OBJECT> T getObjectMember(String id) {
		CLASS value = (CLASS) getObject().getMember(id);
		return value != null ? (T) value.get() : null;
	}
}
