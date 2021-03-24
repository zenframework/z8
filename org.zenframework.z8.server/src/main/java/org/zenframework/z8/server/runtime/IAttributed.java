package org.zenframework.z8.server.runtime;

import java.util.Map;

import org.zenframework.z8.server.types.guid;

public interface IAttributed {
	static final public String System = "system";
	static final public String DisplayName = "displayName";
	static final public String Description = "description";
	static final public String ColumnHeader = "columnHeader";
	static final public String UI = "ui";
	static final public String Presentation = "presentation";
	static final public String Name = "name";
	static final public String Native = "native";
	static final public String Icon = "icon";
	static final public String Url = "url";
	static final public String Help = "help";
	static final public String PrimaryKey = "primaryKey";
	static final public String ParentKey = "parentKey";
	static final public String LockKey = "lockKey";
	static final public String Job = "job";
	static final public String Exportable = "exportable";
	static final public String ForeignKey = "foreignKey";
	static final public String Executable = "executable";
	static final public String SystemTool = "systemTool";

	public String classId();
	public guid classIdKey();

	public String index();
	public void setIndex(String index);

	public String id();
	public void setId(String id);

	public guid key();
	public void setKey(guid key);
	public String keyString();

	public int ordinal();
	public void setOrdinal(int ordinal);

	public Map<String, String> getAttributes();
	public void setAttributes(Map<String, String> attributes);

	public boolean hasAttribute(String key);

	public String getAttribute(String key);
	public void setAttribute(String key, String value);
	public void removeAttribute(String key);

	public String name();
	public void setName(String name);

	public String displayName();
	public void setDisplayName(String name);

	public String columnHeader();
	public void setColumnHeader(String name);

	public String description();
	public void setDescription(String name);

	public boolean system();
	public void setSystem(boolean system);

	public String ui();
	public void setUi(String ui);

	public String presentation();
	public void setPresentation(String presentation);

	public boolean foreignKey();
	public void setForeignKey(boolean foreignKey);

	public boolean exportable();
	public void setExportable(boolean exportable);

	public boolean executable();
	public void setExecutable(boolean executable);
}
