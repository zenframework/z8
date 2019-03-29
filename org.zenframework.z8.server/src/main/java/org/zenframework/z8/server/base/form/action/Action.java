package org.zenframework.z8.server.base.form.action;

import java.util.Collection;
import java.util.Collections;

import org.zenframework.z8.server.base.form.Control;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;

public class Action extends Control implements Runnable, IAction {
	static public class CLASS<T extends Action> extends Control.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Action.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Action(container);
		}
	}

	public ActionType type = ActionType.Default;

	public bool useTransaction = bool.True;

	public RCollection<Parameter.CLASS<? extends Parameter>> parameters = new RCollection<Parameter.CLASS<? extends Parameter>>();

	public Action(IObject container) {
		super(container);
	}

	@SuppressWarnings("unchecked")
	public Collection<Field> fields() {
		return Collections.EMPTY_LIST;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Parameter.CLASS<Parameter>> parameters() {
		return (Collection)parameters;
	}

	@Override
	public IParameter getParameter(String id) {
		for(Parameter.CLASS<?> cls : parameters) {
			Parameter parameter = (Parameter)cls.get();
			if(parameter.id().equals(id))
				return parameter;
		}

		return null;
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		super.writeMeta(writer, query, context);

		writer.writeProperty(Json.isAction, true);

		writer.writeProperty(Json.request, context.classId());
		writer.writeProperty(Json.type, type.toString());

		writer.startArray(Json.parameters);

		for(Parameter.CLASS<?> cls : parameters) {
			Parameter parameter = (Parameter)cls.get();
			writer.startObject();
			parameter.write(writer);
			writer.finishObject();
		}

		writer.finishArray();
	}

	@SuppressWarnings("unchecked")
	public void execute(Collection<guid> records, Query context, Collection<guid> selected, Query query) {
		z8_execute(new RCollection<guid>(records), (Query.CLASS<? extends Query>)context.getCLASS(), new RCollection<guid>(selected), (Query.CLASS<? extends Query>)query.getCLASS());
	}

	@Override
	public void run() {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_execute(RCollection records, Query.CLASS<? extends Query> context, RCollection selected, Query.CLASS<? extends Query> query) {
		for(guid record : (Collection<guid>)records)
			z8_execute(record, context, selected, query);
	}

	@SuppressWarnings("rawtypes")
	public void z8_execute(guid record, Query.CLASS<? extends Query> context, RCollection selected, Query.CLASS<? extends Query> query) {
	}
}
