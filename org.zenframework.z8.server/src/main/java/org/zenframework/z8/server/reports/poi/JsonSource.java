package org.zenframework.z8.server.reports.poi;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.expression.Variable;
import org.zenframework.z8.server.runtime.OBJECT;

public class JsonSource extends DataSource {

	private static final String Item = "item";

	private final JsonArray json;

	public JsonSource(Range range, JsonArray json) {
		super(range);
		this.json = json;
	}

	@Override
	public OBJECT getObject() {
		return json;
	}

	@Override
	public int count() {
		return json.get().size();
	}

	@Override
	public void prepare(XSSFWorkbook workbook) {
		super.prepare(workbook);
		expression.setVariable(new Variable(Item) {
			@Override
			public Object getValue() {
				return json.get().get(getCounter());
			}
		});
	}

	@Override
	protected boolean internalNext() {
		return getCounter() < json.get().size();
	}
}
