package org.zenframework.z8.server.reports.poi;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zenframework.z8.server.expression.Expression;
import org.zenframework.z8.server.expression.Variable;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.integer;

public abstract class DataSource {

	private static final String Counter = "counter";

	protected final Range range;
	protected Expression expression;
	private int counter;

	protected DataSource(Range range) {
		this.range = range;
	}

	public String getIndex() {
		String contextId = range.getContext().id();
		String id = getObject().id();
		return id.startsWith(contextId + ".") ? id.substring(contextId.length() + 1) : id;
	}

	public int getCounter() {
		return counter;
	}

	public void prepare(XSSFWorkbook workbook) {
		counter = -1;

		if (expression == null)
			expression = new Expression().setVariable(new Variable(Counter) {
				@Override
				public Object getValue() {
					return new integer(counter);
				}
			}).setVariable(getIndex(), getObject());
	}

	public boolean next() {
		counter++;
		return internalNext();
	}

	public Object evaluate(String value) {
		return expression.evaluateText(value);
	}

	public abstract OBJECT getObject();

	public abstract int count();

	protected abstract boolean internalNext();
}
