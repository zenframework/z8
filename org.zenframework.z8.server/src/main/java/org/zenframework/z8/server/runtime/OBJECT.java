package org.zenframework.z8.server.runtime;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.zenframework.z8.server.base.application.Application;
import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.security.User;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.INamedObject;
import org.zenframework.z8.server.request.RequestTarget;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class OBJECT extends RequestTarget implements IObject, RmiSerializable {
	public static class CLASS<T extends OBJECT> extends org.zenframework.z8.server.runtime.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(OBJECT.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new OBJECT(container);
		}
	}

	private guid key = null;
	private guid classIdKey = null;
	private int ordinal = 0;

	private IObject container = null;
	private IObject owner = null;
	private CLASS<? extends OBJECT> cls = null;

	private Map<String, String> attributes = new HashMap<String, String>();

	public Members objects = new Members(this);

	public OBJECT() {
		this(null);
	}

	public OBJECT(IObject container) {
		super(null);
		this.container = container;
	}

	@Override
	public int compareTo(INamedObject object) {
		return id() == object.id() ? 0 : 1;
	}

	@Override
	public String classId() {
		return cls.classId();
	}

	@Override
	public int controlSum() {
		return 0;
	}

	@Override
	public String id() {
		if(id == null) {
			id = "";

			IObject container = getContainer();

			if(container != null) {
				id = container.id();
				id = id + (id.isEmpty() ? "" : ".") + index();
			}
		}

		return id;
	}

	public void resetId() {
		id = null;
	}

	public String keyString() {
		return name();
	}

	@Override
	public guid classIdKey() {
		if(classIdKey == null)
			classIdKey = guid.create(classId());
		return classIdKey;
	}

	@Override
	public guid key() {
		if(key == null)
			key = guid.create(keyString());
		return key;
	}

	@Override
	public void setKey(guid key) {
		this.key = key;
	}

	@Override
	public int ordinal() {
		return ordinal;
	}

	@Override
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	@Override
	public String name() {
		String name = getAttribute(Name);
		return name != null ? name : classId();
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

	@Override
	public boolean system() {
		return getAttribute(System) != null;
	}

	@Override
	public void setSystem(boolean system) {
		setAttribute(System, system ? "" : null);
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

	@Override
	public String description() {
		return getAttribute(Description);
	}

	@Override
	public void setDescription(String description) {
		setAttribute(Description, description);
	}

	@Override
	public String ui() {
		return getAttribute(UI);
	}

	@Override
	public void setUi(String form) {
		setAttribute(UI, form);
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
	public void initMembers() {
	}

	@Override
	public Collection<IClass<? extends IObject>> members() {
		return objects;
	}

	@Override
	public IClass<? extends IObject> getMember(String id) {
		for(IClass<? extends IObject> member : objects) {
			if(member.index().equals(id))
				return member;
		}
		return null;
	}

	@Override
	public IObject getOwner() {
		return owner;
	}

	@Override
	public void setOwner(IObject owner) {
		this.owner = owner;
	}

	@Override
	public CLASS<? extends OBJECT> getCLASS() {
		return cls;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCLASS(IClass<? extends IObject> cls) {
		this.cls = (CLASS<? extends OBJECT>)cls;
	}

	@Override
	public String index() {
		if(container == null)
			return "";
		String index = getAttribute(Index);
		return index != null ? index : "";
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
	public void constructor1() {
	}

	@Override
	public void constructor2() {
	}

	@Override
	public void constructor() {
		z8_constructor();
	}

	@Override
	public String toDebugString() {
		return null;
	}

	public bool operatorEqu(OBJECT.CLASS<? extends OBJECT> object) {
		return new bool(this == object.get());
	}

	public bool operatorNotEqu(OBJECT.CLASS<? extends OBJECT> object) {
		return new bool(this != object.get());
	}

	public String sourceCodeLocation() {
		String id = classId();
		int index = id.indexOf(".__");
		return index != -1 ? id.substring(0, index) : id;
	}

	public void write(JsonWriter writer) {
		writer.writeProperty(Json.text, displayName());
		writer.writeProperty(Json.description, description());
		writer.writeProperty(Json.icon, icon());
		writer.writeProperty(Json.id, classId());
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		writer.writeProperty(Json.data, getData());
		writer.writeProperty(Json.ui, ui());
	}

	public org.zenframework.z8.server.json.parser.JsonArray getData() {
		RLinkedHashMap<string, string> parameters = (RLinkedHashMap<string, string>)getParameters();
		JsonArray.CLASS<? extends JsonArray> cls = z8_getData(parameters);
		return cls != null ? cls.get().get() : null;
	}

	@Override
	public void serialize(ObjectOutputStream stream) throws IOException {
	}

	@Override
	public void deserialize(ObjectInputStream stream) throws IOException, ClassNotFoundException {
	}

	static public bool z8_isNull(IObject object) {
		return new bool(object == null);
	}

	static public User.CLASS<? extends User> z8_user() {
		return Application.z8_user();
	}

	protected void z8_constructor() {
	}

	public string z8_id() {
		return new string(id());
	}

	public void z8_setId(primary value) {
		setIndex(value.toString());
		resetId();
		getCLASS().resetId();
	}

	public string z8_index() {
		return new string(index());
	}

	public string z8_name() {
		return new string(name());
	}

	public string z8_displayName() {
		return new string(displayName());
	}

	public void z8_setDisplayName(primary value) {
		setDisplayName(value.toString());
	}

	public string z8_className() {
		return new string(classId());
	}

	public string z8_toString() {
		return new string("");
	}

	public JsonArray.CLASS<? extends JsonArray> z8_getData(RLinkedHashMap<string, string> parameters) {
		return null;
	}
}
