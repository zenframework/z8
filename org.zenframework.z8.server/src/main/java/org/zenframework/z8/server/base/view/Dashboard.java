package org.zenframework.z8.server.base.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.request.RequestTarget;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.security.Component;
import org.zenframework.z8.server.security.IForm;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Dashboard extends RequestTarget {
    public static final String Id = "desktop";

    public Dashboard() {
        super(Id);
    }

    @Override
    public void writeResponse(JsonObject writer) {
        Map<String, String> parameters = getParameters();

        String login = parameters.get(Json.login);
        String newPassword = parameters.get(Json.newPassword);
        String password = parameters.get(Json.password);

        if(login != null) {
            writeLoginInfo(writer);
        }
        else if(newPassword != null) {
            changePassword(password, newPassword);
        }
        else {
            String id = parameters.get("menu");

            if(id != null) {
                Desktop desktop = (Desktop)Loader.getInstance(id);

                JsonObject dataObj = new JsonObject();
                writeDesktop(dataObj, desktop);
                writer.put(Json.data, dataObj);
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

    private void writeDesktopData(JsonObject writer, Desktop desktop, String displayName) {
        Map<String, IForm> forms = ApplicationServer.getUser().forms();

        Collection<CLASS<?>> runnables = new ArrayList<CLASS<?>>();

        for(CLASS<?> cls : desktop.getRunnables()) {
            IForm form = forms.get(cls.classId());

            if((form != null && form.getAccess().getRead()) || forms.isEmpty()) {
                runnables.add(cls);
            }
        }

        if(!runnables.isEmpty()) {
            JsonArray runsArr = new JsonArray();

            for(CLASS<?> cls : runnables) {
                writeData(runsArr, cls);
            }

            writer.put(displayName, runsArr);
        }
    }

    private void writeDesktop(JsonObject writer, Desktop desktop) {
        writeDesktopData(writer, desktop, "");

        for(CLASS<?> cls : desktop.getSubDesktops()) {
            Desktop subDesktop = (Desktop)cls.get();
            writeDesktopData(writer, subDesktop, subDesktop.displayName());
        }
    }

    private CLASS<?>[] loadComponents(Component[] components) {
        List<CLASS<?>> list = new ArrayList<CLASS<?>>();

        for(Component component : components) {
            try {
                list.add(Loader.loadClass(component.className()));
            }
            catch(RuntimeException e) {
                Trace.logError("Error loading entry point '" + component.className() + "'", e);
            }
        }

        return list.toArray(new CLASS[0]);
    }

    protected void writeLoginInfo(JsonObject writer) {
        IUser user = ApplicationServer.getUser();

        writer.put(Json.sessionId, ApplicationServer.getSession().id());

        JsonObject userObj = new JsonObject();

        userObj.put(Json.id, user.id());
        userObj.put(Json.name, user.description());
        userObj.put(Json.login, user.name());
        userObj.put(Json.settings, user.settings());

        JsonArray compsArr = new JsonArray();
        for(CLASS<?> cls : loadComponents(user.components())) {
            writeData(compsArr, cls);
        }
        userObj.put(Json.components, compsArr);

        JsonObject paramsObj = new JsonObject();
        Map<string, primary> parameters = user.parameters();
        for(string key : parameters.keySet()) {
            paramsObj.put(key.get(), parameters.get(key));
        }
        userObj.put(Json.parameters, paramsObj);
    
        writer.put(Json.user, userObj);
    }

    private void writeData(JsonArray writer, CLASS<?> cls) {
        JsonObject dataObj = new JsonObject();

        if(cls.instanceOf(Procedure.class)) {
            Procedure procedure = (Procedure)cls.newInstance();
            procedure.write(dataObj);
        } else {
            cls.write(dataObj);
        }

        writer.put(dataObj);
    }
}
