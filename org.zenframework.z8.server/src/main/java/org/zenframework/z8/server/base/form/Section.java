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

public class Section extends Control {
	public static class CLASS<T extends Section> extends Control.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Section.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Section(container);
		}
	}

	public integer columnCount = new integer(3);

	public RCollection<Control.CLASS<? extends Control>> controls = new RCollection<Control.CLASS<? extends Control>>();

	public Section(IObject container) {
		super(container);
	}

	public Collection<Field> fields() {
		Collection<Field> result = new LinkedHashSet<Field>();

		for(Control control : CLASS.asList(controls)) {
			if(control instanceof Field)
				result.add((Field)control);
			else if(control instanceof Section)
				result.addAll(((Section)control).fields());
		}

		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Control.CLASS<Control>> controls() {
		return (Collection)controls;
	}

	public Collection<Control> getControls() {
		return CLASS.asList(controls);
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		super.writeMeta(writer, query, context);

		writer.writeProperty(Json.isSection, true);
		writer.writeProperty(Json.columnCount, columnCount);
		writer.writeControls(Json.controls, getControls(), query, context); 
	}
}
