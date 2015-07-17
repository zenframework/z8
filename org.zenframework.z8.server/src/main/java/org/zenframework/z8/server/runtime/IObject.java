package org.zenframework.z8.server.runtime;

import java.util.Map;

import org.zenframework.z8.server.request.INamedObject;

public interface IObject extends INamedObject {
    static final public String DisplayName = "displayName";
    static final public String Description = "description";
    static final public String Label = "label";
    static final public String Name = "name";
    static final public String Native = "native";
    static final public String Icon = "icon";
    static final public String Help = "help";
    static final public String PrimaryKey = "primaryKey";
    static final public String ParentKey = "parentKey";
    static final public String Job = "job";
    static final public String Index = "index";
    static final public String Exportable = "exportable";
    static final public String GenDbUpdatable = "gendb_updatable";
    static final public String ForeignKey = "foreignKey";
    static final public String SearchIndex = "searchIndex";
    static final public String UniqueField = "uniqueField";

    public String classId();

    public String name();

    @Override
    public String displayName();

    public void setName(String name);

    public void setDisplayName(String name);

    public String description();

    public void setDescription(String name);

    public String label();

    public void setLabel(String label);

    public boolean foreignKey();
    public void setForeignKey(boolean foreignKey);

    public IObject getContainer();

    public void setContainer(IObject container);

    public IClass<? extends IObject> getCLASS();

    public void setCLASS(IClass<? extends IObject> cls);

    public String getIndex();
    public void setIndex(String index);

    public Map<String, String> getAttributes();

    public void setAttributes(Map<String, String> attributes);

    public boolean hasAttribute(String key);

    public String getAttribute(String key);

    public void setAttribute(String key, String value);

    public void removeAttribute(String key);

    public void constructor1();

    public void constructor2();

    public void onInitialized();

    public String toDebugString();

    public void constructor();
}
