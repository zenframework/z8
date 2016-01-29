package org.zenframework.z8.server.base.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class FileInfo extends OBJECT implements Serializable {
    private static final long serialVersionUID = -4474455212423780540L;

    public string name = new string();
    public string path = new string();
    public string type = new string();
    public datetime time = new datetime();
    public guid id = new guid();
    
    public JsonObject json;

    public FileItem file;

    public static class CLASS<T extends FileInfo> extends OBJECT.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(FileInfo.class);
            setAttribute(Native, FileInfo.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new FileInfo(container);
        }
    }

    public FileInfo() {
        super();
    }

    public FileInfo(String id) {
        super();
        this.id = new guid(id);
    }

    public FileInfo(IObject container) {
        super(container);
    }

    public FileInfo(FileItem file) throws IOException {
        this(file, null);
    }

    public FileInfo(FileItem file, String path) throws IOException {
        super();
        this.path = new string(path);
        this.name = new string(file.getName());
        this.file = file;
    }

    protected FileInfo(JsonObject json) {
        super();
        set(json);
    }

    public void set(FileInfo fileInfo) {
        this.path = fileInfo.path;
        this.name = fileInfo.name;
        this.time = fileInfo.time;
        this.type = fileInfo.type;
        this.id = fileInfo.id;

        this.file = fileInfo.file;
    }

    protected void set(JsonObject json) {
        path = new string(json.getString(json.has(Json.file) ? Json.file : Json.path));
        name = new string(json.has(Json.name) ? json.getString(Json.name) : "");
        time = new datetime(json.has(Json.time) ? json.getString(Json.time) : "");
        type = new string(json.has(Json.type) ? json.getString(Json.type) : "");
        id = new guid(json.has(Json.id) ? json.getString(Json.id) : "");
        
        this.json = json;
    }

    public static List<FileInfo> parseArray(String json) {
        List<FileInfo> result = new ArrayList<FileInfo>();

        if (!json.isEmpty()) {
            JsonArray array = new JsonArray(json);

            for (int i = 0; i < array.length(); i++)
                result.add(parse(array.getJsonObject(i)));
        }
        return result;
    }

    public static String toJson(Collection<FileInfo> fileInfos) {
        JsonArray array = new JsonArray();
       
        for (FileInfo file : fileInfos)
            array.add(file.toJsonObject());

        return array.toString();
    }

    public static FileInfo parse(JsonObject json) {
        return new FileInfo(json);
    }

    public JsonObject toJsonObject() {
        if(json == null) {
            json = new JsonObject();
            json.put(Json.name, name);
//            json.put(Json.time, time);
            json.put(Json.type, type);
            json.put(Json.path, path);
            json.put(Json.id, id);
        }
        return json;
    }
    
    public static RCollection<FileInfo.CLASS<? extends FileInfo>> z8_parse(string json) {
        RCollection<FileInfo.CLASS<? extends FileInfo>> result = new RCollection<FileInfo.CLASS<? extends FileInfo>>();

        JsonArray array = new JsonArray(json.get());

        for (int index = 0; index < array.length(); index++) {
            JsonObject object = array.getJsonObject(index);

            FileInfo.CLASS<FileInfo> fileInfo = new FileInfo.CLASS<FileInfo>();
            fileInfo.get().set(object);

            result.add(fileInfo);
        }
        return result;
    }

    static public string z8_toJson(RCollection<FileInfo.CLASS<? extends FileInfo>> classes) {
        return new string(toJson(CLASS.asList(classes)));
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof FileInfo && id != null && id.equals(((FileInfo) object).id);
    }

    public InputStream getInputStream() {
        try {
            return file.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OutputStream getOutputStream() {
        try {
            return file.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
