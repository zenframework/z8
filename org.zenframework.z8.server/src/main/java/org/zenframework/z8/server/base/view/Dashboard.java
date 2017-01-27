package org.zenframework.z8.server.base.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.Procedure;
import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.request.RequestTarget;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.security.Entry;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Dashboard extends RequestTarget {
	public Dashboard() {
		super();
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

		Users users = Users.newInstance();

		users.password.get().set(new string(newPassword));
		users.update(user.id());
	}

	private void changeEmail(String email) {
		IUser user = ApplicationServer.getUser();

		if(!user.email().equalsIgnoreCase(email)) {
			Users users = Users.newInstance();
			users.email.get().set(new string(email));
			users.update(user.id());
		}
	}

	private void writeDesktopData(JsonWriter writer, Desktop desktop, String displayName) {
		Collection<OBJECT.CLASS<OBJECT>> runnables = desktop.getRunnables();

//		if(!runnables.isEmpty()) {
			writer.startObject();

			writer.writeProperty(Json.text, displayName);
			writer.writeProperty(Json.icon, desktop.icon());

			writer.startArray(Json.items);
			for(CLASS<?> cls : runnables)
				writeData(writer, cls);
			writer.finishArray();

			writer.finishObject();
//		}
	}

	private void writeDesktop(JsonWriter writer, Desktop desktop) {
		writeDesktopData(writer, desktop, "");

		for(CLASS<?> cls : desktop.getSubDesktops()) {
			Desktop subDesktop = (Desktop)cls.get();
			writeDesktopData(writer, subDesktop, subDesktop.displayName());
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<Desktop.CLASS<Desktop>> loadEntries(Collection<Entry> entries) {
		List<Desktop.CLASS<Desktop>> result = new ArrayList<Desktop.CLASS<Desktop>>();

		for(Entry entry : entries) {
			try {
				result.add((Desktop.CLASS<Desktop>)Loader.loadClass(entry.className()));
			} catch(RuntimeException e) {
				Trace.logError("Error loading entry point '" + entry.className() + "'", e);
			}
		}

		return result;
	}

	protected void writeLoginInfo(JsonWriter writer) {
		IUser user = ApplicationServer.getUser();

		writer.writeProperty(Json.session, ApplicationServer.getSession().id());
		writer.writeProperty(Json.maxUploadSize, ServerConfig.webServerUploadMax());
		writer.writeProperty(Json.session, ApplicationServer.getSession().id());

		writer.startObject(Json.user);

		writer.writeProperty(Json.id, user.id());
		writer.writeProperty(Json.login, user.login());
		writer.writeProperty(Json.firstName, user.firstName());
		writer.writeProperty(Json.middleName, user.middleName());
		writer.writeProperty(Json.lastName, user.lastName());
		writer.writeProperty(Json.description, user.description());
		writer.writeProperty(Json.email, user.email());
		writer.writeProperty(Json.phone, user.phone());
		writer.writeProperty(Json.settings, user.settings());

		Collection<Desktop.CLASS<Desktop>> desktops = loadEntries(user.entries());
		writer.startArray(Json.entries);
		for(CLASS<?> cls : desktops)
			writeData(writer, cls);
		writer.finishArray();

		if(getParameter(Json.experimental) != null) {
			writer.startArray(Json.data);
			for(Desktop.CLASS<Desktop> cls : desktops)
				writeDesktopData(writer, cls.newInstance(), cls.displayName());
			writer.finishArray();
		}

		writer.startObject(Json.parameters);
		Map<string, primary> parameters = user.parameters();
		for(string key : parameters.keySet())
			writer.writeProperty(key.get(), parameters.get(key));
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
