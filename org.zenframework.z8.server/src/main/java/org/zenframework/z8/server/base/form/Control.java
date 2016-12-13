package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class Control extends OBJECT {
	public Control(IObject container) {
		super(container);
	}

	public static class CLASS<T extends Control> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Control.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Control(container);
		}
	}

	public bool readOnly = null;

	public integer rowspan = null;
	public integer colspan = null;

	/*
	 * В ситуации зависимых компонентов, т.е. listbox или combobox, 
	 * свойство dependsOn обозначает по какому полю будет фильтроваться содержимое.
	 * Зависимость строится помещением зависимого компонента в массив dependencies
	 * компонента, от которого он зависит:
	 *
	 * ....
	 * Регион регион;
	 * Город город;
	 * Улица улица;
	 * 
	 * город.название.link = город.регион;
	 * регион.dependencies = { город.название };
	 * 
	 * улица.название.link = улица.город;
	 * город.dependencies = { улица.название };
	 * .....
	 * 
	 * Здесь регион фильтрует список городов, который, в свою очередь, фильтрует улицы.
	 **/
	public GuidField.CLASS<? extends GuidField> dependsOn = null;
	public RCollection<Control.CLASS<? extends Control>> dependencies = new RCollection<Control.CLASS<? extends Control>>();

	public boolean readOnly() {
		return readOnly != null ? readOnly.get() : false;
	}

	public void writeMeta(JsonWriter writer, Query query) {
		writer.writeProperty(Json.name, id());
		writer.writeProperty(Json.header, displayName());
		writer.writeProperty(Json.description, description());
		writer.writeProperty(Json.icon, icon());
		writer.writeProperty(Json.label, label());

		writer.writeProperty(Json.rowspan, rowspan, new integer(1));
		writer.writeProperty(Json.colspan, colspan, new integer(1));

		writeDependencies(writer);
	}

	protected void writeDependencies(JsonWriter writer) {
		if(dependsOn != null)
			writer.writeProperty(Json.dependsOn, dependsOn.id());

		if(!dependencies.isEmpty()) {
			writer.startArray(Json.dependencies);
			for(Control.CLASS<? extends Control> control : dependencies)
				writer.write(control.id());
			writer.finishArray();
		}
	}
}
