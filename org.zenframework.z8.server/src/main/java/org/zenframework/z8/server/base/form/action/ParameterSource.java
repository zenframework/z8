package org.zenframework.z8.server.base.form.action;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class ParameterSource extends OBJECT {
	public static class CLASS<T extends ParameterSource> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(ParameterSource.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new ParameterSource(container);
		}
	}
	
	public ParameterSource(IObject container) {
		super(container);
	}

	public Query.CLASS<? extends Query> query = null;
	public Field.CLASS<? extends Field> displayField = null;
	public Field.CLASS<? extends Field> valueField = null;
	
	public void writeMeta(JsonWriter writer) {
		if(query == null)
			return;
		
		Query query = this.query.get();
		Query owner = query.owner();
		
		writer.writeProperty(Json.isCombobox, true);
		writer.writeProperty(Json.name, displayField.id());
		
		writer.startObject(Json.query);
		writer.writeProperty(Json.lockKey, query.lockKey().id());
		writer.writeProperty(Json.primaryKey, query.primaryKey().id());
		writer.writeProperty(Json.request, owner == null ? query.classId() : owner.classId());
		
		if (valueField != null) {
			writer.startArray(Json.fields);
			writeField(writer, displayField.get());
			writeField(writer, valueField.get());
			writer.finishArray();
		}
		
		if(owner != null)
			writer.writeProperty(Json.name, query.id());
		writer.finishObject();
	}
	
	private void writeField(JsonWriter writer, Field field) {
		writer.startObject();
		writer.writeProperty(Json.displayName, field.displayName());
		writer.writeProperty(Json.name, field.id());
		writer.writeProperty(Json.type, field.type().toString());
		writer.finishObject();
	}
	
	static public ParameterSource.CLASS<? extends ParameterSource> z8_create(Query.CLASS<? extends Query> query, Field.CLASS<? extends Field> displayField) {
		return z8_create(query, displayField, null);
	}
	
	static public ParameterSource.CLASS<? extends ParameterSource> z8_create(Query.CLASS<? extends Query> query, Field.CLASS<? extends Field> displayField, Field.CLASS<? extends Field> valueField) {
		ParameterSource.CLASS<ParameterSource> source = new ParameterSource.CLASS<ParameterSource>(null);
		source.get().query = query;
		source.get().displayField = displayField;
		source.get().valueField = valueField;
		return source;
	}

}
