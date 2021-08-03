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
		if(owner != null)
			writer.writeProperty(Json.name, query.id());
		writer.finishObject();
	}
	
	static public ParameterSource.CLASS<? extends ParameterSource> z8_create(Query.CLASS<? extends Query> query, Field.CLASS<? extends Field> displayField) {
		ParameterSource.CLASS<ParameterSource> source = new ParameterSource.CLASS<ParameterSource>(null);
		source.get().query = query;
		source.get().displayField = displayField;
		return source;
	}

}
