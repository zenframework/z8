package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

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
	private String name;
	private String description;
	private primary defaultValue;

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
		return name;
	}

	public String getDescription() {
		return description;
	}

	public primary getDefaultValue() {
		return defaultValue;
	}

	static public Setting.CLASS<Setting> z8_setting(guid id, guid parentId, String name, String description, primary value) {
		Setting.CLASS<Setting> setting = new Setting.CLASS<>(null);
		setting.get().id = id;
		setting.get().parentId = parentId;
		setting.get().name = name;
		setting.get().description = description;
		setting.get().defaultValue = value;
		return setting;
	}

}
