package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
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

	private static final String Lock = "lock";

	private guid settingId;
	private guid parentId;
	private primary defaultValue;

	public Setting(IObject container) {
		super(container);
	}

	public guid settingId() {
		return settingId;
	}

	public guid parentId() {
		return parentId;
	}

	public primary defaultValue() {
		return defaultValue;
	}

	public int lock() {
		return hasAttribute(Lock) ? Integer.parseInt(getAttribute(Lock)) : RecordLock.None.getInt();
	}

	public guid z8_settingId() {
		return settingId();
	}

	public guid z8_parentId() {
		return parentId();
	}

	public primary z8_defaultValue() {
		return defaultValue();
	}

	public integer z8_lock() {
		return new integer(lock());
	}

	static public Setting setting(guid settingId, primary defaultValue) {
		return setting(settingId, guid.Null, defaultValue);
	}

	static public Setting setting(guid settingId, guid parentId, primary defaultValue) {
		Setting setting = new Setting.CLASS<>(null).get();
		setting.settingId = settingId;
		setting.parentId = parentId;
		setting.defaultValue = defaultValue;
		return setting;
	}

	@SuppressWarnings("unchecked")
	static public Setting.CLASS<Setting> z8_setting(guid settingId, primary defaultValue) {
		return (Setting.CLASS<Setting>) setting(settingId, defaultValue).getCLASS();
	}

	@SuppressWarnings("unchecked")
	static public Setting.CLASS<Setting> z8_setting(guid settingId, guid parentId, primary defaultValue) {
		return (Setting.CLASS<Setting>) setting(settingId, parentId, defaultValue).getCLASS();
	}

}
