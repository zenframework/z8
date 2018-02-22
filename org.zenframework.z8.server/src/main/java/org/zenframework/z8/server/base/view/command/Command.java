package org.zenframework.z8.server.base.view.command;

import java.util.Collection;

import org.zenframework.z8.server.base.model.command.ICommand;
import org.zenframework.z8.server.base.model.command.IParameter;
import org.zenframework.z8.server.base.simple.Runnable;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.INamedObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class Command extends Runnable implements ICommand {
	static public class CLASS<T extends Command> extends Runnable.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Command.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Command(container);
		}
	}

	public string id = guid.create().string();
	public string text = new string();
	public string description = new string();
	public string icon = new string();

	public bool useTransaction = new bool(true);

	public RCollection<Parameter.CLASS<? extends Parameter>> parameters = new RCollection<Parameter.CLASS<? extends Parameter>>();

	public Command(IObject container) {
		super(container);
	}

	@Override
	public String id() {
		return id.get();
	}

	@Override
	public String displayName() {
		return text.get();
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof INamedObject ? id().equals(((INamedObject)object).id()) : false;
	}

	@Override
	public int compareTo(INamedObject object) {
		return id().hashCode() - object.id().hashCode();
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
		writer.writeProperty(Json.id, id);
		writer.writeProperty(Json.text, text);
		writer.writeProperty(Json.description, description);
		writer.writeProperty(Json.icon, icon);

		writer.startArray(Json.parameters);

		for(Parameter.CLASS<?> cls : parameters) {
			Parameter parameter = (Parameter)cls.get();
			writer.startObject();
			parameter.write(writer);
			writer.finishObject();
		}

		writer.finishArray();
	}

	static public Command.CLASS<? extends Command> z8_create(string id, string text) {
		return z8_create(id, text, new string());
	}

	static public Command.CLASS<? extends Command> z8_create(string id, string text, Parameter.CLASS<? extends Parameter> parameter) {
		Command.CLASS<? extends Command> command = z8_create(id, text);
		command.get().parameters.add(parameter);
		return command;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public Command.CLASS<? extends Command> z8_create(string id, string text, RCollection parameters) {
		Command.CLASS<? extends Command> command = z8_create(id, text);
		command.get().parameters = parameters;
		return command;
	}

	static public Command.CLASS<? extends Command> z8_create(string id, string text, string icon) {
		Command.CLASS<Command> command = new Command.CLASS<Command>();
		command.get().id.set(id);
		command.get().text.set(text);
		command.get().icon.set(icon);
		return command;
	}
}
