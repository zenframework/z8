package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Settings extends TreeTable {
	final static public String TableName = "SystemSettings";

	final static public guid Version = guid.create(strings.Version);

	static public class fieldNames {
		public final static String Value = "Value";
	}

	static public class strings {
		public final static String Title = "Settings.title";
		public final static String Name = "Settings.name";
		public final static String Value = "Settings.value";

		public final static String Version = "system.version";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
		public final static String Value = Resources.get(strings.Value);
	}

	public static class CLASS<T extends Settings> extends TreeTable.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Settings.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Settings(container);
		}
	}

	public TextField.CLASS<TextField> value = new TextField.CLASS<TextField>(this);

	static public Settings newInstance() {
		return (Settings)Runtime.instance().getTableByName(Settings.TableName).newInstance();
	}

	public Settings() {
		this(null);
	}

	public Settings(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(value);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(256);

		value.setName(fieldNames.Value);
		value.setIndex("value");
	}

	@Override
	public void initStaticRecords() {
		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(name.get(), new string(strings.Version));
			record.put(lock.get(), RecordLock.Full);
			addRecord(Version, record);
		}
	}

	static private String get(guid property) {
		try {
			Settings settings = new Settings.CLASS<Settings>().get();
			Field value = settings.value.get();
			return settings.readRecord(property, Arrays.asList(value)) ? value.string().get() : null;
		} catch (Throwable e) {
			Trace.logError(e);
			return null;
		}
	}

	static public String get(guid property, String defaultValue) {
		String value = get(property);
		return value != null ? value : defaultValue;
	}

	static public string get(guid property, string defaultValue) {
		String value = get(property);
		return value != null ? new string(value) : defaultValue;
	}

	static public guid get(guid property, guid defaultValue) {
		String value = get(property);
		return value != null ? new guid(value) : defaultValue;
	}

	static public date get(guid property, date defaultValue) {
		String value = get(property);
		return value != null ? new date(value) : defaultValue;
	}

	static public integer get(guid property, integer defaultValue) {
		String value = get(property);
		return value != null ? new integer(value) : defaultValue;
	}

	static public decimal get(guid property, decimal defaultValue) {
		String value = get(property);
		return value != null ? new decimal(value) : defaultValue;
	}

	static public bool get(guid property, bool defaultValue) {
		String value = get(property);
		return value != null ? new bool(value) : defaultValue;
	}

	static public string string(Setting setting) {
		return get(setting.settingId(), (string) setting.defaultValue());
	}

	static public guid guid(Setting setting) {
		return get(setting.settingId(), (guid) setting.defaultValue());
	}

	static public date date(Setting setting) {
		return get(setting.settingId(), (date) setting.defaultValue());
	}

	static public integer integer(Setting setting) {
		return get(setting.settingId(), (integer) setting.defaultValue());
	}

	static public decimal decimal(Setting setting) {
		return get(setting.settingId(), (decimal) setting.defaultValue());
	}

	static public bool bool(Setting setting) {
		return get(setting.settingId(), (bool) setting.defaultValue());
	}

	static public void set(guid setting, String value) {
		save(setting, null, null, null, value, -1, true);
	}

	static public void set(guid setting, primary value) {
		save(setting, null, null, null, value.toString(), -1, true);
	}

	static public void register(Setting setting) {
		save(setting.settingId(), setting.parentId(), setting.name(), setting.description(), setting.defaultValue().toString(), setting.lock(), false);
	}

	static public void register(guid setting, guid parent, String name, String description, String value) {
		register(setting, parent, name, description, new string(value));
	}

	static public void register(guid setting, guid parent, String name, String description, primary value) {
		register(setting, parent, name, description, value, -1);
	}

	static public void register(guid setting, guid parent, String name, String description, primary value, int lock) {
		save(setting, parent, name, description, value.toString(), lock, false);
	}

	static public void save(guid setting, guid parent, String name, String description, String value, int lock, boolean overrideValue) {
		Settings settings = new Settings.CLASS<Settings>().get();
		boolean exists = settings.hasRecord(setting);
		if (parent != null)
			settings.parentId.get().set(parent);
		if (name != null)
			settings.name.get().set(name);
		if (description != null)
			settings.description.get().set(description);
		if (overrideValue || !exists)
			settings.value.get().set(value);
		if (lock >= 0)
			settings.lock.get().set(lock);
		if (exists)
			settings.update(setting);
		else
			settings.create(setting);
	}

	static public String version() {
		return get(Version);
	}

	static public string z8_string(Setting.CLASS<Setting> setting) {
		return string(setting.get());
	}

	static public guid z8_guid(Setting.CLASS<Setting> setting) {
		return guid(setting.get());
	}

	static public date z8_date(Setting.CLASS<Setting> setting) {
		return date(setting.get());
	}

	static public integer z8_int(Setting.CLASS<Setting> setting) {
		return integer(setting.get());
	}

	static public decimal z8_decimal(Setting.CLASS<Setting> setting) {
		return decimal(setting.get());
	}

	static public bool z8_bool(Setting.CLASS<Setting> setting) {
		return bool(setting.get());
	}

	static public void z8_set(guid setting, primary value) {
		set(setting, value);
	}

	static public void z8_register(Setting.CLASS<Setting> setting) {
		register(setting.get());
	}

}
