package org.zenframework.z8.server.runtime;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	private int ordinal = 0;

	private IObject container = null;
	private IObject owner = null;
	private CLASS<? extends OBJECT> cls = null;

	private Map<String, String> attributes = new HashMap<String, String>();

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

	@Override
	public void resetId() {
		id = null;
	}

	@Override
	public guid key() {
		if(key == null) {
			String ownerName = owner != null ? owner.name() : null;
			String name = name();

			String value = (ownerName != null ? ownerName + "." : "") + (name != null ? name : classId());
			key = new guid(UUID.nameUUIDFromBytes(value.getBytes()));
		}

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
	public String form() {
		return getAttribute(Form);
	}

	@Override
	public void setForm(String form) {
		setAttribute(Form, form);
	}

	@Override
	public String label() {
		return getAttribute(Label);
	}

	@Override
	public void setLabel(String label) {
		setAttribute(Label, label);
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
		if(this.container != null) {
			this.setIndex(null);
			this.resetId();
		}
		this.container = container;
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
	public String getIndex() {
		if(container == null)
			return "";

		String index = getAttribute(Index);

		if(index == null)
			return "";
			// throw new NullPointerException("OBJECT.index == null");

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
	public void constructor1() {
	}

	@Override
	public void constructor2() {
	}

	@Override
	public void onInitialized() {
	}

	@Override
	public String toDebugString() {
		return null;
	}

	@Override
	public void constructor() {
		z8_constructor();
	}

	public bool operatorEqu(OBJECT.CLASS<? extends OBJECT> object) {
		return new bool(this == object.get());
	}

	public bool operatorNotEqu(OBJECT.CLASS<? extends OBJECT> object) {
		return new bool(this != object.get());
	}

	public void write(JsonWriter writer) {
		writer.writeProperty(Json.text, displayName());
		writer.writeProperty(Json.description, description());
		writer.writeProperty(Json.icon, icon());
		writer.writeProperty(Json.id, classId());
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		org.zenframework.z8.server.json.parser.JsonArray response = response();
		if(response != null)
			writer.writeProperty(Json.data, response);
	}

	@Override
	public void serialize(ObjectOutputStream stream) throws IOException {
	}

	@Override
	public void deserialize(ObjectInputStream stream) throws IOException, ClassNotFoundException {
	}

	public org.zenframework.z8.server.json.parser.JsonArray response() {
		RLinkedHashMap<string, string> parameters = (RLinkedHashMap<string, string>)getParameters();

		JsonArray.CLASS<? extends JsonArray> response = z8_response(parameters);

		return response != null ? response.get().get() : null;
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

	public string z8_toString() {
		return new string("");
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
