package org.zenframework.z8.server.runtime;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.request.INamedObject;
import org.zenframework.z8.server.request.RequestTarget;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.UserInfo;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class OBJECT extends RequestTarget implements IObject {
    public static class CLASS<T extends OBJECT> extends org.zenframework.z8.server.runtime.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(OBJECT.class);
            setAttribute(Native, OBJECT.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new OBJECT(container);
        }
    }

    private String id;

    private IObject container;
    private CLASS<? extends OBJECT> cls = null;

    private Map<String, String> attributes = new HashMap<String, String>();

    public bool accessible = new bool(true);

    public OBJECT() {
        this(null);
        setAttribute(IObject.ObjectId, guid.NULL.toString());
    }

    public OBJECT(IObject container) {
        super(null);
        this.container = container;
        setAttribute(IObject.ObjectId, guid.NULL.toString());
    }

    @Override
    public int compareTo(INamedObject object) {
        return id().hashCode() - object.id().hashCode();
    }

    @Override
    public String classId() {
        assert (cls != null);
        return cls.classId();
    }

    @Override
    public String id() {
        if(id == null) {
            id = "";

            IObject container = getContainer();

            if(container != null) {
                id = container.id();
                id = id + (id.isEmpty() ? "" : ".") + getIndex();
            }
        }

        return id;
    }

    public guid objectId() {
        return new guid(getAttribute(ObjectId));
    }

    @Override
    public String name() {
        return getAttribute(Name);
    }

    @Override
    public void setName(String name) {
        setAttribute(Name, name);
    }

    @Override
    public String displayName() {
        return getAttribute(DisplayName);
    }

    @Override
    public void setDisplayName(String name) {
        setAttribute(DisplayName, name);
    }
    
    public boolean exportable() {
        String exportable = getAttribute(Exportable);
        return exportable == null || Boolean.parseBoolean(exportable);
    }
    
    @Override
    public void setForeignKey(boolean foreignKey) {
        setAttribute(ForeignKey, Boolean.toString(foreignKey));
    }

    @Override
    public boolean foreignKey() {
        String foreignKey = getAttribute(ForeignKey);
        return foreignKey == null || Boolean.parseBoolean(foreignKey);
    }
    
    public void setExportable(boolean exportable) {
        setAttribute(Exportable, Boolean.toString(exportable));
    }

    public boolean gendb_updatable() {
        String gendb_updatable = getAttribute(GenDbUpdatable);
        return gendb_updatable == null || Boolean.parseBoolean(gendb_updatable);
    }
    
    public void setGendb_updatable(boolean gendb_updatable) {
        setAttribute(GenDbUpdatable, Boolean.toString(gendb_updatable));
    }

    @Override
    public String description() {
        return getAttribute(Description);
    }

    @Override
    public void setDescription(String description) {
        setAttribute(Description, description);
    }

    @Override
    public String label() {
        return getAttribute(Label);
    }

    @Override
    public void setLabel(String description) {
        setAttribute(Label, description);
    }

    public String icon() {
        return getAttribute(Icon);
    }

    public void setIcon(String icon) {
        setAttribute(Icon, icon);
    }

    @Override
    public IObject getContainer() {
        return container;
    }

    @Override
    public void setContainer(IObject container) {
        this.container = container;
    }

    @Override
    public CLASS<? extends OBJECT> getCLASS() {
        return cls;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setCLASS(IClass<? extends IObject> cls) {
        this.cls = (CLASS<? extends OBJECT>) cls;
    }

    @Override
    public String getIndex() {
        if(container == null) {
            return "";
        }

        String index = getAttribute(Index);

        if(index == null) {
            return "";
            //throw new NullPointerException("OBJECT.index == null");
        }

        return index;
    }

    @Override
    public void setIndex(String index) {
        setAttribute(Index, index);
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    @Override
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public void constructor1() {}

    @Override
    public void constructor2() {}

    @Override
    public void onInitialized() {}

    @Override
    public String toDebugString() {
        return null;
    }

    static public IUser getUser() {
        return ApplicationServer.getUser();
    }

    @Override
    public void constructor() {
        z8_constructor();
    }

    protected void z8_constructor() {}

    public string z8_id() {
        return new string(id());
    }

    public guid z8_objectId() {
        return objectId();
    }

    public string z8_index() {
        return new string(getIndex());
    }

    public string z8_name() {
        return new string(name());
    }

    public string z8_displayName() {
        return new string(displayName());
    }

    public string z8_className() {
        return new string(classId());
    }

    static public bool z8_isNull(IObject object) {
        return new bool(object == null);
    }

    static public UserInfo.CLASS<? extends UserInfo> z8_user() {
        UserInfo.CLASS<UserInfo> cls = new UserInfo.CLASS<UserInfo>(null);
        UserInfo user = cls.get();
        user.initialize(getUser());
        return cls;
    }

    public string z8_toString() {
        return new string("");
    }

    static public void log(String text) {
        System.out.println(text);
        ApplicationServer.getMonitor().log(text);
    }

    static public void z8_log(string text) {
        log(text.get());
    }

    static public void z8_wait(integer milliseconds) {
        try {
            Object obj = new Object();
            ;

            synchronized(obj) {
                obj.wait(milliseconds.get());
            }
        } catch(InterruptedException e) {}
    }

    static public integer z8_currentTimeMillis() {
        return new integer(System.currentTimeMillis());
    }

    static public void showMessage(String text) {
        System.out.println(text);
        ApplicationServer.getMonitor().print(text);
    }

    public void z8_showMessage(string text) {
        showMessage(text.get());
    }

    public void z8_sendFile(file file) {
        ApplicationServer.getMonitor().print(file);
    }

    public bool z8_equals(OBJECT.CLASS<? extends OBJECT> x) {
        return new bool(equals(x.get()));
    }

    public bool operatorEqu(OBJECT.CLASS<? extends OBJECT> object) {
        return new bool(this == object.get());
    }

    public bool operatorNotEqu(OBJECT.CLASS<? extends OBJECT> object) {
        return new bool(this != object.get());
    }

    public void write(org.zenframework.z8.server.json.parser.JsonObject writer) {
        writer.put(Json.text, displayName());
        writer.put(Json.description, description());
        writer.put(Json.icon, icon());
        writer.put(Json.id, classId());
    }
    
    @Override
    public void writeResponse(org.zenframework.z8.server.json.parser.JsonObject writer) throws Throwable {
        org.zenframework.z8.server.json.parser.JsonArray response = response();
        if (response != null) {
            writer.put(Json.data, response);
        }
    }

    static private RLinkedHashMap<string, string> convertParameters(Map<String, String> parameters) {
        RLinkedHashMap<string, string> result = new RLinkedHashMap<string, string>();
        
        for(String key : parameters.keySet()) {
            result.put(new string(key), new string(parameters.get(key)));
        }
        
        return result;
    }
    
    static public RLinkedHashMap<string, string> z8_requestParameters() {
        return convertParameters(getParameters());
    }
    
    static public org.zenframework.z8.server.base.json.parser.JsonObject.CLASS<? extends org.zenframework.z8.server.base.json.parser.JsonObject> z8_getWriter() {
        org.zenframework.z8.server.base.json.parser.JsonObject.CLASS<org.zenframework.z8.server.base.json.parser.JsonObject> writer = new org.zenframework.z8.server.base.json.parser.JsonObject.CLASS<org.zenframework.z8.server.base.json.parser.JsonObject>(null);    
        writer.get().set(ApplicationServer.getRequest().getResponse().getWriter());
        return writer;
    }
    
    public org.zenframework.z8.server.json.parser.JsonArray response() {
        RLinkedHashMap<string, string> parameters = convertParameters(getParameters());
        JsonArray.CLASS<? extends JsonArray> response = z8_response(parameters);
        if (parameters.isModified()) {
            Map<String, String> baseParams = getParameters();
            for (Map.Entry<string, string> entry : parameters.entrySet()) {
                baseParams.put(entry.getKey().get(), entry.getValue().get());
            }
        }
        return response != null ? response.get().getInternalArray() : null;
    }
    
    public JsonArray.CLASS<? extends JsonArray> z8_response(RLinkedHashMap<string, string> parameters) {
        return null;
    }

    public void z8_setAttribute(string attribute, primary value) {
        setAttribute(attribute.get(), value.toString());
    }

    public void z8_removeAttribute(string attribute) {
        removeAttribute(attribute.get());
    }
}
