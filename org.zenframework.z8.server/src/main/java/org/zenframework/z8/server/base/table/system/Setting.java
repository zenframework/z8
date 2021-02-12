package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Setting extends OBJECT {

	public static class CLASS<T extends Setting> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Setting.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Setting(container);
		}
	}

	private guid id;
	private guid parentId;
	private string name;
	private string description;
	private primary defaultValue;
	private integer lock;

	public Setting(IObject container) {
		super(container);
	}

	public guid getId() {
		return id;
	}

	public guid getParentId() {
		return parentId;
	}

	public String getName() {
		return name.get();
	}

	public String getDescription() {
		return description.get();
	}

	public primary getDefaultValue() {
		return defaultValue;
	}

	public int getLock() {
		return lock.getInt();
	}

	static public Setting setting(guid id, guid parentId, String name, String description, primary value, int lock) {
		return z8_setting(id, parentId, new string(name), new string(description), value, new integer(lock)).get();
	}

	static public Setting.CLASS<Setting> z8_setting(guid id, guid parentId, string name, string description, primary value, integer lock) {
		Setting.CLASS<Setting> setting = new Setting.CLASS<>(null);
		setting.get().id = id;
		setting.get().parentId = parentId;
		setting.get().name = name;
		setting.get().description = description;
		setting.get().defaultValue = value;
		return setting;
	}

}
