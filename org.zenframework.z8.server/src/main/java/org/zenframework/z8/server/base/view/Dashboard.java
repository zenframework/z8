package org.zenframework.z8.server.base.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.simple.Runnable;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.request.RequestTarget;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.security.Component;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Dashboard extends RequestTarget {
	public static final String Id = "desktop";

	public Dashboard() {
		super(Id);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		String login = getParameter(Json.login);
		String newPassword = getParameter(Json.newPassword);
		String password = getParameter(Json.password);
		String email = getParameter(Json.email);

		if(login != null)
			writeLoginInfo(writer);
		else if(newPassword != null)
			changePassword(password, newPassword);
		else if(email != null)
			changeEmail(email);
		else {
			String id = getParameter(Json.menu);

			if(id != null) {
				Desktop desktop = (Desktop)Loader.getInstance(id);

				writer.startArray(Json.data);
				writeDesktop(writer, desktop);
				writer.finishArray();
			}
		}
	}

	private void changePassword(String password, String newPassword) {
		IUser user = ApplicationServer.getUser();

		if(!user.password().equals(password)) {
			throw new AccessDeniedException();
		}

		Users users = new Users.CLASS<Users>().get();

		users.password.get().set(new string(newPassword));
		users.update(user.id());
	}

	private void changeEmail(String email) {
		IUser user = ApplicationServer.getUser();

		if(!user.email().equalsIgnoreCase(email)) {
			Users users = new Users.CLASS<Users>().get();
			users.email.get().set(new string(email));
			users.update(user.id());
		}
	}

	@SuppressWarnings("rawtypes")
	private void writeDesktopData(JsonWriter writer, Desktop desktop, String displayName) {
		Collection<Runnable.CLASS> runnables = desktop.getRunnables();

		if(!runnables.isEmpty()) {
			writer.startObject();

			writer.writeProperty(Json.text, displayName);

			writer.startArray(Json.items);
			for(CLASS<?> cls : runnables)
				writeData(writer, cls);
			writer.finishArray();

			writer.finishObject();
		}
	}

	private void writeDesktop(JsonWriter writer, Desktop desktop) {
		writeDesktopData(writer, desktop, "");

		for(CLASS<?> cls : desktop.getSubDesktops()) {
			Desktop subDesktop = (Desktop)cls.get();
			writeDesktopData(writer, subDesktop, subDesktop.displayName());
		}
	}

	private CLASS<?>[] loadComponents(Collection<Component> components) {
		List<CLASS<?>> list = new ArrayList<CLASS<?>>();

		for(Component component : components) {
			try {
				list.add(Loader.loadClass(component.className()));
			} catch(RuntimeException e) {
				Trace.logError("Error loading entry point '" + component.className() + "'", e);
			}
		}

		return list.toArray(new CLASS[0]);
	}

	protected void writeLoginInfo(JsonWriter writer) {
		IUser user = ApplicationServer.getUser();

		writer.writeProperty(Json.sessionId, ApplicationServer.getSession().id());

		writer.startObject(Json.user);

		writer.writeProperty(Json.id, user.id());
		writer.writeProperty(Json.name, user.description());
		writer.writeProperty(Json.login, user.name());
		writer.writeProperty(Json.email, user.email());
		writer.writeProperty(Json.phone, user.phone());
		writer.writeProperty(Json.settings, user.settings());

		writer.startArray(Json.components);
		for(CLASS<?> cls : loadComponents(user.components()))
			writeData(writer, cls);
		writer.finishArray();

		writer.startObject(Json.parameters);
		Map<string, primary> parameters = user.parameters();
		for(string key : parameters.keySet())
			writer.writeProperty('"' + key.get() + '"', parameters.get(key));
		writer.finishObject();

		writer.finishObject();
	}

	private void writeData(JsonWriter writer, CLASS<?> cls) {
		writer.startObject();

		if(cls.instanceOf(Procedure.class)) {
			Procedure procedure = (Procedure)cls.get();
			procedure.write(writer);
		} else
			cls.write(writer);

		writer.finishObject();
	}
}
