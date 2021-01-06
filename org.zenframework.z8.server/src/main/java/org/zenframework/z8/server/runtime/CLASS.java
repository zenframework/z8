package org.zenframework.z8.server.runtime;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.guid;

public class CLASS<TYPE extends IObject> implements IClass<TYPE> {
	private Map<String, String> attributes = new HashMap<String, String>();

	private IObject container = null;
	private IObject owner = null;

	private guid key = null;
	private guid classIdKey = null;
	private int ordinal = 0;

	private Class<TYPE> javaClass;

	private String id;
	private String index;
	private String classId;

	private TYPE object = null;
	private int stage = IClass.Constructor;

	private Constructor<?> constructor = null;

	private Object[] closure;

	static public <T extends IObject> List<T> asList(Collection<? extends org.zenframework.z8.server.runtime.CLASS<? extends T>> collection) {
		List<T> result = new ArrayList<T>();

		if(collection != null) {
			for(org.zenframework.z8.server.runtime.CLASS<? extends T> cls : collection)
				result.add((T)(cls.get()));
		}

		return result;
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
		attributes.put(key, Resources.getByKey(value));
	}

	@Override
	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	public CLASS(IObject container) {
		this.container = container;
	}

	@Override
	public IObject getContainer() {
		return container;
	}

	@Override
	final public Class<TYPE> getJavaClass() {
		return javaClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	final public void setJavaClass(Class<?> cls) {
		javaClass = (Class<TYPE>)cls;
	}

	@Override
	public int stage() {
		return stage;
	}

	@Override
	public String index() {
		return container == null ? "" : index;
	}

	@Override
	public void setIndex(String index) {
		if(this.index == null)
			this.index = index;
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
	public int ordinal() {
		return ordinal;
	}

	@Override
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	@Override
	public String id() {
		if(id == null) {
			id = "";

			IObject container = getContainer();

			// never change !!!
			if(container != null) {
				id = container.getCLASS().get().id();
				id += (id.isEmpty() ? "" : ".") + index();
			}
		}

		return id;
	}

	@Override
	public void setId(String value) {
		setIndex(value);
		id = null;
	}

	@Override
	public Object[] getClosure() {
		return closure;
	}

	@Override
	public void setClosure(Object[] closure) {
		this.closure = closure;
	}

	@Override
	public boolean instanceOf(Class<?> cls) {
		return cls.isAssignableFrom(getJavaClass());
	}

	@Override
	public boolean hasInstance() {
		return object != null;
	}

	@Override
	public final TYPE get() {
		return get(Constructor2);
	}

	@Override
	public TYPE get(int stage) {
		IObject container = getContainer();

		if(container != null) {
			IClass<? extends IObject> containerClass = container.getCLASS();
			if(containerClass != null && containerClass.stage() < stage)
				containerClass.get(stage);
		}

		if(object != null && this.stage >= stage)
			return object;

		if(object == null)
			create(container);

		callConstructors(stage);

		return object;
	}

	public Object newObject(IObject container) {
		return null;
	}

	@SuppressWarnings("unchecked")
	private void create(IObject container) {
		try {
			object = (TYPE)newObject(container);
			object.setCLASS(this);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private void callConstructors(int stage) {
		if(this.stage < Constructor1 && stage >= Constructor1) {
			this.stage = Constructor1;
			object.constructor1();
			object.initMembers();
		}

		callConstructor2(stage);
	}

	private void callConstructor2(int stage) {
		if(this.stage < Constructor2 && stage >= Constructor2) {
			this.stage = Constructor2;
			object.constructor2();
			object.constructor();
		}
	}

	@Override
	public TYPE newInstance() {
		return newInstance(getContainer());
	}

	@Override
	@SuppressWarnings("unchecked")
	public TYPE newInstance(IObject container) {
		try {
			if(constructor == null)
				constructor = getClass().getDeclaredConstructor(IObject.class);
			org.zenframework.z8.server.runtime.CLASS<TYPE> cls = (org.zenframework.z8.server.runtime.CLASS<TYPE>)constructor.newInstance(container);
			return cls.get();
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String classId() {
		return classId == null ? (classId = javaClass.getCanonicalName()) : classId;
	}

	@Override
	public guid classIdKey() {
		if(classIdKey == null)
			classIdKey = guid.create(classId());
		return classIdKey;
	}

	@Override
	public String keyString() {
		return name();
	}

	@Override
	public guid key() {
		return key == null ? key = guid.create(keyString()) : key;
	}

	@Override
	public void setKey(guid key) {
		this.key = key;
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
	public String columnHeader() {
		return getAttribute(ColumnHeader);
	}

	@Override
	public void setColumnHeader(String name) {
		setAttribute(ColumnHeader, name);
	}

	@Override
	public boolean system() {
		return getAttribute(System) != null;
	}

	@Override
	public void setSystem(boolean system) {
		setAttribute(System, system ? "" : null);
	}

	@Override
	public boolean exportable() {
		String exportable = getAttribute(Exportable);
		return exportable == null || Boolean.parseBoolean(exportable);
	}

	@Override
	public void setExportable(boolean exportable) {
		setAttribute(Exportable, Boolean.toString(exportable));
	}

	@Override
	public boolean foreignKey() {
		String foreignKey = getAttribute(ForeignKey);
		return foreignKey == null || Boolean.parseBoolean(foreignKey);
	}

	@Override
	public void setForeignKey(boolean foreignKey) {
		setAttribute(ForeignKey, Boolean.toString(foreignKey));
	}

	@Override
	public boolean executable() {
		return getAttribute(Executable) != null;
	}

	@Override
	public void setExecutable(boolean executable) {
		setAttribute(Executable, executable ? "" : null);
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
	public void setUi(String ui) {
		setAttribute(UI, ui);
	}

	@Override
	public String presentation() {
		return getAttribute(Presentation);
	}

	@Override
	public void setPresentation(String presentation) {
		setAttribute(Presentation, presentation);
	}

	public String icon() {
		return getAttribute(Icon);
	}

	public String url() {
		return getAttribute(Url);
	}

	public void setIcon(String icon) {
		setAttribute(Icon, icon);
	}

	public void write(JsonWriter writer) {
		writer.writeProperty(Json.request, classId());
		writer.writeProperty(Json.text, displayName());
		writer.writeProperty(Json.description, description());
		writer.writeProperty(Json.icon, icon());
		writer.writeProperty(Json.url, url());
	}

}
