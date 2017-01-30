package org.zenframework.z8.server.base.form.action;

import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class Action extends OBJECT implements Runnable, IAction {
	static public class CLASS<T extends Action> extends OBJECT.CLASS<T> {
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

	public string id = guid.create().string();
	public string text = new string();
	public string description = new string();
	public string icon = new string();
	public ActionType type = ActionType.Default;

	public bool useTransaction = bool.True;

	public RCollection<Parameter.CLASS<? extends Parameter>> parameters = new RCollection<Parameter.CLASS<? extends Parameter>>();

	public Action(IObject container) {
		super(container);
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
	public void write(JsonWriter writer) {
		writer.writeProperty(Json.id, id());
		writer.writeProperty(Json.text, displayName());
		writer.writeProperty(Json.description, description());
		writer.writeProperty(Json.icon, icon());
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
	public void execute(guid recordId, Query context, Collection<guid> selected, Query query) {
		z8_execute(recordId, (Query.CLASS<? extends Query>)context.getCLASS(), new RCollection<guid>(selected), (Query.CLASS<? extends Query>)query.getCLASS());
	}

	@Override
	public void run() {
	}

	@SuppressWarnings("rawtypes")
	public void z8_execute(guid recordId, Query.CLASS<? extends Query> context, RCollection selected, Query.CLASS<? extends Query> query) {
	}

	static public Action.CLASS<? extends Action> z8_create(string id, string text) {
		return z8_create(id, text, new string());
	}

	static public Action.CLASS<? extends Action> z8_create(string id, string text, Parameter.CLASS<? extends Parameter> parameter) {
		Action.CLASS<? extends Action> action = z8_create(id, text);
		action.get().parameters.add(parameter);
		return action;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public Action.CLASS<? extends Action> z8_create(string id, string text, RCollection parameters) {
		Action.CLASS<? extends Action> action = z8_create(id, text);
		action.get().parameters.addAll(parameters);
		return action;
	}

	static public Action.CLASS<? extends Action> z8_create(string id, string text, string icon) {
		Action.CLASS<Action> action = new Action.CLASS<Action>();
		action.get().id.set(id);
		action.get().text.set(text);
		action.get().icon.set(icon);
		return action;
	}
}
