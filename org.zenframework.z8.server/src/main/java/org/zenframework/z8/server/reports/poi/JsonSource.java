package org.zenframework.z8.server.reports.poi;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.runtime.OBJECT;

public class JsonSource extends DataSource {
	private static final String Item = "item";

	private final JsonArray json;

	private Wrapper<Object> item;

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
	public void open() {
		super.open();
		item.set(null);
	}

	@Override
	protected void initialize() {
		super.initialize();

		item = getObjectMember(Item);

		if (item == null)
			item = Wrapper.<Object>instance(getObject(), Item);
	}

	@Override
	protected boolean internalNext() {
		int index = getIndex();
		boolean hasNext = index < json.get().size();
		item.set(hasNext ? json.get().get(index) : null);
		return hasNext;
	}
}
