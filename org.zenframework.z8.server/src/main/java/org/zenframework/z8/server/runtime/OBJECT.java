package org.zenframework.z8.server.runtime;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.base.application.Application;
import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.security.User;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.request.RequestTarget;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.file;
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

	private CLASS<? extends OBJECT> cls = null;
	public Members objects = new Members(this);

	public OBJECT() {
		this(null);
	}

	public OBJECT(IObject container) {
		super(null);
	}

	@Override
	public String classId() {
		return getCLASS().classId();
	}

	@Override
	public int controlSum() {
		return 0;
	}

	@Override
	public String id() {
		return getCLASS().id();
	}

	@Override
	public void setId(String id) {
		getCLASS().setId(id);
	}

	@Override
	public guid classIdKey() {
		return getCLASS().classIdKey();
	}

	@Override
	public String keyString() {
		return getCLASS().keyString();
	}

	@Override
	public guid key() {
		return getCLASS().key();
	}

	@Override
	public void setKey(guid key) {
		getCLASS().setKey(key);
	}

	@Override
	public int ordinal() {
		return getCLASS().ordinal();
	}

	@Override
	public void setOrdinal(int ordinal) {
		getCLASS().setOrdinal(ordinal);
	}

	@Override
	public String name() {
		return getCLASS().name();
	}

	@Override
	public void setName(String name) {
		getCLASS().setName(name);
	}

	@Override
	public String displayName() {
		return getCLASS().displayName();
	}

	@Override
	public void setDisplayName(String name) {
		getCLASS().setDisplayName(name);
	}

	@Override
	public String columnHeader() {
		return getCLASS().columnHeader();
	}

	@Override
	public void setColumnHeader(String name) {
		getCLASS().setColumnHeader(name);
	}

	@Override
	public String description() {
		return getCLASS().description();
	}

	@Override
	public void setDescription(String description) {
		getCLASS().setDescription(description);
	}

	@Override
	public String ui() {
		return getCLASS().ui();
	}

	@Override
	public void setUi(String ui) {
		getCLASS().setUi(ui);
	}

	@Override
	public String presentation() {
		return getCLASS().presentation();
	}

	@Override
	public void setPresentation(String presentation) {
		getCLASS().setPresentation(presentation);
	}

	public String icon() {
		return getCLASS().icon();
	}

	public void setIcon(String icon) {
		getCLASS().setIcon(icon);
	}

	@Override
	public boolean system() {
		return getCLASS().system();
	}

	@Override
	public void setSystem(boolean system) {
		getCLASS().setSystem(system);
	}

	@Override
	public boolean foreignKey() {
		return getCLASS().foreignKey();
	}

	@Override
	public void setForeignKey(boolean foreignKey) {
		getCLASS().setForeignKey(foreignKey);
	}

	@Override
	public boolean exportable() {
		return getCLASS().exportable();
	}

	@Override
	public void setExportable(boolean exportable) {
		getCLASS().setExportable(exportable);
	}

	@Override
	public IObject getContainer() {
		return getCLASS().getContainer();
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
		return getCLASS().getOwner();
	}

	@Override
	public void setOwner(IObject owner) {
		getCLASS().setOwner(owner);
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
		return getCLASS().index();
	}

	@Override
	public void setIndex(String index) {
		getCLASS().setIndex(index);
	}

	@Override
	public Map<String, String> getAttributes() {
		return getCLASS().getAttributes();
	}

	@Override
	public void setAttributes(Map<String, String> attributes) {
		getCLASS().setAttributes(attributes);
	}

	@Override
	public boolean hasAttribute(String key) {
		return getCLASS().hasAttribute(key);
	}

	@Override
	public String getAttribute(String key) {
		return getCLASS().getAttribute(key);
	}

	@Override
	public void setAttribute(String key, String value) {
		getCLASS().setAttribute(key, value);
	}

	@Override
	public void removeAttribute(String key) {
		getCLASS().removeAttribute(key);
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
		getCLASS().write(writer);
		writer.writeProperty(Json.id, id());
	}

	@Override
	public void processRequest(IResponse response) throws Throwable {
		if(Json.content.equals(getParameters().get(Json.action))) {
			binary binary = getContent();
			response.setInputStream(binary != null ? binary.get() : null);
		} else
			super.processRequest(response);
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		writer.writeProperty(Json.data, getData());
		writer.writeProperty(Json.ui, ui());
	}

	public binary getContent() {
		RLinkedHashMap<string, string> parameters = (RLinkedHashMap<string, string>)getParameters();
		RCollection<file> files = (RCollection<file>)getFiles();
		return z8_getContent(parameters, files);
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

	static public User.CLASS<? extends User> z8_user() {
		return Application.z8_user();
	}

	protected void z8_constructor() {
	}

	public string z8_getAttribute(string attribute) {
		return new string(getAttribute(attribute.get()));
	}

	public void z8_setAttribute(string attribute, primary value) {
		setAttribute(attribute.get(), value.toString());
	}

	public string z8_id() {
		return new string(id());
	}

	public void z8_setId(primary value) {
		getCLASS().setId(value.toString());
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
		getCLASS().setDisplayName(value.toString());
	}

	public string z8_className() {
		return new string(classId());
	}

	public string z8_toString() {
		return new string("");
	}

	public binary z8_getContent(RLinkedHashMap<string, string> parameters, RCollection<file> files) {
		return null;
	}

	public JsonArray.CLASS<? extends JsonArray> z8_getData(RLinkedHashMap<string, string> parameters) {
		return null;
	}
}
