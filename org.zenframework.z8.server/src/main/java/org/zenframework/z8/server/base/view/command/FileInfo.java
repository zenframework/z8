package org.zenframework.z8.server.base.view.command;

import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;

public class FileInfo {
    public String file;
    public String name;
    public int size;

    public FileInfo(String data) {
        JsonObject object = new JsonObject(data);

        if(object != null) {
            file = object.getString(Json.path);
            name = object.getString(Json.name);
            size = object.getInt(Json.size);
        }
    }
}
