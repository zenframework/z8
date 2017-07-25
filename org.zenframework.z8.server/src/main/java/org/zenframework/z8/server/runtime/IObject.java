package org.zenframework.z8.server.runtime;

import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.request.INamedObject;
import org.zenframework.z8.server.types.guid;

public interface IObject extends INamedObject {
	static final public String System = "system";
	static final public String DisplayName = "displayName";
	static final public String Description = "description";
	static final public String UI = "ui";
	static final public String Name = "name";
	static final public String Native = "native";
	static final public String Icon = "icon";
	static final public String Help = "help";
	static final public String PrimaryKey = "primaryKey";
	static final public String ParentKey = "parentKey";
	static final public String Job = "job";
	static final public String Index = "index";
	static final public String Exportable = "exportable";
	static final public String ForeignKey = "foreignKey";

	public String classId();
	public guid classIdKey();

	public int controlSum();

	public String id();

	public guid key();
	public void setKey(guid key);

	public int ordinal();
	public void setOrdinal(int ordinal);

	public String name();
	public void setName(String name);

	@Override
	public String displayName();
	public void setDisplayName(String name);

	public String description();
	public void setDescription(String name);

	public boolean system();
	public void setSystem(boolean system);

	public String ui();
	public void setUi(String form);

	public boolean foreignKey();
	public void setForeignKey(boolean foreignKey);

	public IObject getContainer();

	public void initMembers();
	public Collection<IClass<? extends IObject>> members();
	public IClass<? extends IObject> getMember(String name);

	public IObject getOwner();
	public void setOwner(IObject owner);

	public IClass<? extends IObject> getCLASS();
	public void setCLASS(IClass<? extends IObject> cls);

	public String index();
	public void setIndex(String index);

	public Map<String, String> getAttributes();
	public void setAttributes(Map<String, String> attributes);

	public boolean hasAttribute(String key);
	
	public String getAttribute(String key);
	public void setAttribute(String key, String value);
	public void removeAttribute(String key);

	public void constructor1();
	public void constructor2();
	public void constructor();

	public String toDebugString();
}
