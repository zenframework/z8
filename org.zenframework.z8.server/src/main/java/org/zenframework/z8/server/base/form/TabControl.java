package org.zenframework.z8.server.base.form;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.integer;

public class TabControl extends Control {
	public static class CLASS<T extends TabControl> extends Control.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TabControl.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new TabControl(container);
		}
	}

	public integer height;
	public RCollection<Tab.CLASS<? extends Tab>> tabs = new RCollection<Tab.CLASS<? extends Tab>>();

	public TabControl(IObject container) {
		super(container);
	}

	public Collection<Field.CLASS<Field>> fields() {
		Collection<Field.CLASS<Field>> result = new LinkedHashSet<Field.CLASS<Field>>();

		for(Tab.CLASS<? extends Tab> tab : tabs)
			result.addAll(tab.get().fields());

		return result;
	}

	public Collection<Tab> getTabs() {
		return CLASS.asList(tabs);
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		super.writeMeta(writer, query, context);

		writer.writeProperty(Json.isTabControl, true);
		writer.writeProperty(Json.height, height, new integer(300));
		writer.writeControls(Json.tabs, getTabs(), query, context);
	}
}
