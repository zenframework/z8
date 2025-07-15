package org.zenframework.z8.server.base.form;

import java.util.Collection;

import org.zenframework.z8.server.base.json.parser.JsonObject;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.engine.ApplicationServer;
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

	public bool readOnly;
	public bool required;
	public bool editable;
	public bool important;

	public integer colSpan;
	public integer flex;

	public integer height;

	/*
	 * В ситуации зависимых компонентов, т.е. listbox или combobox, 
	 * свойство dependency обозначает по какому полю будет фильтроваться содержимое.
	 * Зависимость строится помещением зависимого компонента в массив dependencies
	 * компонента, от которого он зависит:
	 *
	 * ....
	 * Регион регион;
	 * Город город;
	 * Улица улица;
	 * 
	 * город.название.dependency = город.регион;
	 * регион.dependencies = { город.название };
	 * 
	 * улица.название.dependency = улица.город;
	 * улица.название.dependsOn = город.recordId;
	 * город.dependencies = { улица.название };
	 * .....
	 * 
	 * Здесь регион фильтрует список городов, который, в свою очередь, фильтрует улицы.
	 **/
	public GuidField.CLASS<? extends GuidField> dependency;
	public GuidField.CLASS<? extends GuidField> dependsOn;
	public RCollection<Control.CLASS<? extends Control>> dependencies = new RCollection<Control.CLASS<? extends Control>>();

	public Source.CLASS<? extends Source> source = new Source.CLASS<Source>(this);

	public boolean readOnly() {
		return readOnly != null ? readOnly.get() : false;
	}

	public boolean required() {
		return required != null ? required.get() : false;
	}

	public Collection<Field> fields() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		writer.writeProperty(Json.name, id());
		writer.writeProperty(Json.ui, ui());
		writer.writeProperty(Json.header, displayName());
		writer.writeProperty(Json.columnHeader, columnHeader());
		writer.writeProperty(Json.description, description());
		writer.writeProperty(Json.icon, icon());

		writer.writeProperty(Json.height, height);
		writer.writeProperty(Json.colSpan, colSpan);
		writer.writeProperty(Json.flex, flex);

		writer.writeProperty(Json.readOnly, readOnly());
		writer.writeProperty(Json.required, required());
		writer.writeProperty(Json.editable, editable);
		writer.writeProperty(Json.important, important);

		if(source.get().query != null && ApplicationServer.getUser().privileges().getRequestAccess(source.get().query.classIdKey()).execute()) {
			writer.startObject(Json.source);
			source.get().writeMeta(writer, query, context);
			writer.finishObject();
		}

		writeDependencies(writer);
		z8_writeMeta(writer.getWrapper(), query != null ? (Query.CLASS<? extends Query>) query.getCLASS() : null, context != null ? (Query.CLASS<? extends Query>) context.getCLASS() : null);
	}

	protected void writeDependencies(JsonWriter writer) {
		if(dependency != null)
			writer.writeProperty(Json.dependency, dependency.id());

		if(dependsOn != null)
			writer.writeProperty(Json.dependsOn, dependsOn.id());

		if(!dependencies.isEmpty()) {
			writer.startArray(Json.dependencies);
			for(Control.CLASS<? extends Control> control : dependencies)
				writer.write(control.id());
			writer.finishArray();
		}
	}

	public void z8_writeMeta(org.zenframework.z8.server.base.json.JsonWriter.CLASS<? extends org.zenframework.z8.server.base.json.JsonWriter> writer, Query.CLASS<? extends Query> query, Query.CLASS<? extends Query> context) { }
}
