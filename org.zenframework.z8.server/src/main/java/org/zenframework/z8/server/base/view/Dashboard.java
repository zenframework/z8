package org.zenframework.z8.server.base.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.request.RequestTarget;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.security.Entry;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.string;

public class Dashboard extends RequestTarget {
	public Dashboard() {
		super();
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		String newPassword = getParameter(Json.newPassword);
		String password = getParameter(Json.password);
		String email = getParameter(Json.email);

		if(newPassword != null)
			changePassword(password, newPassword);
		else if(email != null)
			changeEmail(email);
		else
			writeLoginInfo(writer);
	}

	private void changePassword(String password, String newPassword) {
		User user = ApplicationServer.getUser();

		if(!user.getPassword().equals(password))
			throw new AccessDeniedException();

		Users.changePassword(user.getId(), newPassword);
	}

	private void changeEmail(String email) {
		User user = ApplicationServer.getUser();

		if(!user.getEmail().equalsIgnoreCase(email)) {
			Users users = Users.newInstance();
			users.email.get().set(new string(email));
			users.update(user.getId());
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<OBJECT.CLASS<OBJECT>> loadEntries(Collection<Entry> entries) {
		List<OBJECT.CLASS<OBJECT>> result = new ArrayList<OBJECT.CLASS<OBJECT>>();

		for(Entry entry : entries) {
			try {
				result.add((OBJECT.CLASS<OBJECT>) Loader.loadClass(entry.className()));
			} catch (Throwable e) {
				Trace.logError(e);
			}
		}

		return result;
	}

	protected void writeLoginInfo(JsonWriter writer) {
		User user = ApplicationServer.getUser();

		writer.writeProperty(Json.session, ApplicationServer.getSession().getId());
		writer.writeProperty(Json.maxUploadSize, ServerConfig.webServerUploadMax());

		writer.startObject(Json.user);

		writer.writeProperty(Json.id, user.getId());
		writer.writeProperty(Json.login, user.getLogin());
		writer.writeProperty(Json.administrator, user.getId().equals(BuiltinUsers.Administrator.guid()));
		writer.writeProperty(Json.firstName, user.getFirstName());
		writer.writeProperty(Json.middleName, user.getMiddleName());
		writer.writeProperty(Json.lastName, user.getLastName());
		writer.writeProperty(Json.description, user.getDescription());
		writer.writeProperty(Json.email, user.getEmail());
		writer.writeProperty(Json.phone, user.getPhone());
		writer.writeProperty(Json.changePassword, user.getChangePassword());
		writer.writeProperty(Json.settings, user.getSettings());

		writer.startArray(Json.entries);
		for(OBJECT.CLASS<OBJECT> cls : loadEntries(user.getEntries()))
			cls.writeObject(writer);
		writer.finishArray();

		writer.writeRoles(user.getRoles());
		writer.writeParameters(user.getParameters());
		writer.writeAccess(user.getAccess());

		writer.finishObject();
	}
}
