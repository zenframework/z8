package org.zenframework.z8.server.base.form;

import java.util.Collection;
import java.util.LinkedHashSet;

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

	public integer columns = new integer(3);

	public RCollection<Control.CLASS<? extends Control>> controls = new RCollection<Control.CLASS<? extends Control>>(true);

	public Section(IObject container) {
		super(container);
	}

	@SuppressWarnings("unchecked")
	public Collection<Field.CLASS<Field>> fields() {
		Collection<Field.CLASS<Field>> result = new LinkedHashSet<Field.CLASS<Field>>();

		for(Control.CLASS<? extends Control> control : controls) {
			if(control instanceof Field.CLASS)
				result.add((Field.CLASS<Field>)control);
			else if(control instanceof Section.CLASS)
				result.addAll(((Section)control.get()).fields());
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
	public void writeMeta(JsonWriter writer) {
		super.writeMeta(writer);

		writer.writeProperty(Json.readOnly, readOnly);
		writer.writeProperty(Json.columns, columns);
		writer.writeProperty(Json.isSection, true);
	}
}
