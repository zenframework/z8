package org.zenframework.z8.server.base.form;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;

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

	public RCollection<Tab.CLASS<? extends Tab>> tabs = new RCollection<Tab.CLASS<? extends Tab>>();

	public TabControl(IObject container) {
		super(container);
	}

	public Collection<Field> fields() {
		Collection<Field> result = new LinkedHashSet<Field>();

		for(Tab tab : CLASS.asList(tabs))
			result.addAll(tab.fields());

		return result;
	}

	public Collection<Tab> getTabs() {
		return CLASS.asList(tabs);
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		super.writeMeta(writer, query, context);

		writer.writeProperty(Json.isTabControl, true);
		writer.writeControls(Json.tabs, getTabs(), query, context);
	}
}
