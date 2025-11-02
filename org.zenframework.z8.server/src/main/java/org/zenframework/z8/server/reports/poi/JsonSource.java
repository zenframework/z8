package org.zenframework.z8.server.reports.poi;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.runtime.OBJECT;

public class JsonSource extends DataSource {

	private final JsonArray json;

	private Item item;

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

		item = getObjectMember(Item.Index);

		if (item == null) {
			OBJECT source = getObject();
			item = new Item.CLASS<Item>(source).get();
			source.objects.add(item.getCLASS());
		}
	}

	@Override
	protected boolean internalNext() {
		int counter = this.counter.getInt();
		boolean hasNext = counter < json.get().size();
		item.set(hasNext ? json.get().get(counter) : null);
		return hasNext;
	}
}
