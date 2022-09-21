package org.zenframework.z8.server.base.security;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class SecurityObject extends OBJECT {

	public static class CLASS<T extends SecurityObject> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(SecurityObject.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new SecurityObject(container);
		}
	}

	public SecurityObject(IObject container) {
		super(container);
	}

	private string type;
	private guid id;
	private string name;

	public string z8_getType() {
		return type;
	}

	public guid z8_getId() {
		return id;
	}

	public string z8_getName() {
		return name;
	}

	public static SecurityObject.CLASS<SecurityObject> z8_object(string type, guid id, string name) {
		SecurityObject.CLASS<SecurityObject> object = new SecurityObject.CLASS<SecurityObject>(null);
		object.get().type = type;
		object.get().id = id;
		object.get().name = name;
		return object;
	}

	public static SecurityObject object(string type, guid id, string name) {
		return z8_object(type, id, name).get();
	}

}
