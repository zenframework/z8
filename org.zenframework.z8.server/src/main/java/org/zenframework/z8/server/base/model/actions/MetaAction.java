package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.base.form.Control;
import org.zenframework.z8.server.base.form.FieldGroup;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class MetaAction extends ReadAction {
	static public final string StartValue = new string("0");
	static public final string LimitValue = new string("50");

	public MetaAction(ActionParameters actionParameters) {
		super(actionParameters);
	}

	@Override
	protected void initialize() {
		Map<string, string> requestParameters = requestParameters();

		Query query = getQuery();
		
		if(query.showAsTree())
			requestParameters.put(Json.parentId, new string(guid.NULL.toString()));
		
		super.initialize();
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		ActionParameters actionParameters = actionParameters();
		Map<string, string> requestParameters = requestParameters();
		
		Query query = getQuery();

		writer.writeProperty(Json.isQuery, true);

		writer.writeProperty(Json.queryId, getRequestParameter(Json.queryId));

		if(actionParameters.link != null) {
			writer.writeProperty(Json.fieldId, getRequestParameter(Json.fieldId));
			writer.writeProperty(Json.linkId, actionParameters.link.id());
		}

		Collection<Field> fields = getSelectFields();
		query.writeMeta(writer, fields);

		writeSortFields(writer, actionParameters.sortFields);
		writeGroupFields(writer, actionParameters.groupFields);

		writeSections(writer, fields);

		requestParameters.put(Json.start, StartValue);
		requestParameters.put(Json.limit, LimitValue);

		requestParameters.put(Json.limit, LimitValue);

		super.writeResponse(writer);
	}

	private void writeSortFields(JsonWriter writer, Collection<Field> sortFields) {
		if(!sortFields.isEmpty()) {
			Field field = sortFields.iterator().next();

			writer.writeProperty(Json.sort, field.id());
			writer.writeProperty(Json.direction, field.sortDirection.toString());
		}
	}

	private void writeGroupFields(JsonWriter writer, Collection<Field> groupFields) {
		if(!groupFields.isEmpty()) {
			writer.startArray(Json.groupBy);

			for(Field field : groupFields)
				writer.write(field.id());

			writer.finishArray();
		}
	}

	class Section {
		FieldGroup group = null;
		Collection<Object> controls = new ArrayList<Object>();

		Section(FieldGroup group) {
			this.group = group;
		}

		void add(Object control) {
			controls.add(control);
		}

		boolean isEmpty() {
			return controls.isEmpty();
		}
	}

	private Section getSections(FieldGroup group, Collection<Field> fields) {
		Section result = new Section(group);

		Collection<Control> controls = group == null ? collectControls() : group.getControls();

		for(Control control : controls) {
			if(control instanceof Field) {
				if(fields.contains(control)) {
					result.add(control);
				}
			} else if(control instanceof FieldGroup) {
				Section section = getSections((FieldGroup)control, fields);

				if(section != null) {
					result.add(section);
				}
			}
		}

		return result.isEmpty() ? null : result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<Control> collectControls() {
		Query query = getQuery();

		if(actionParameters().link != null) {
			return (Collection)getSelectFields();
		}

		Collection<Control> controls = new ArrayList<Control>();

		Query context = query.getContext();

		if(context != null) {
			controls = context.getControls();
		}

		if(controls.isEmpty()) {
			controls = query.getControls();
		}

		Query rootQuery = query.getRootQuery();

		if(controls.isEmpty() && rootQuery != query) {
			controls = rootQuery.getControls();
		}

		return controls;
	}

	private void writeSections(JsonWriter writer, Collection<Field> fields) {
		Section section = getSections(null, fields);

		if(section == null)
			return;

		writer.startObject(Json.section);
		writeSection(writer, section);
		writer.finishObject();
	}

	private void writeSection(JsonWriter writer, Section section) {
		if(section.group != null)
			section.group.writeMeta(writer);

		writer.writeProperty(Json.isSection, true);

		writer.startArray(Json.controls);

		for(Object control : section.controls) {
			if(control instanceof Field) {
				Field field = (Field)control;
				if(!field.system()) {
					writer.startObject();
					writer.writeProperty(Json.id, field.id());
					writer.finishObject();
				}
			} else if(control instanceof Section) {
				writer.startObject();
				writeSection(writer, (Section)control);
				writer.finishObject();
			}
		}

		writer.finishArray();
	}
}
