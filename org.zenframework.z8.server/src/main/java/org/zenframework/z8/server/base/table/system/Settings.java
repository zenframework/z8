package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.StringField;
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
		public final static String Name = "Name";
		public final static String Description = "Description";
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

	public StringField.CLASS<? extends StringField> name = new StringField.CLASS<StringField>(this);
	public TextField.CLASS<? extends StringField> description = new TextField.CLASS<TextField>(this);
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

		objects.add(name);
		objects.add(description);
		objects.add(value);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.setName(fieldNames.Name);
		name.setIndex("name");
		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(256);

		description.setName(fieldNames.Description);
		description.setIndex("description");

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

	static public String get(guid setting, String defaultValue) {
		String value = get(setting);
		return value != null ? value : defaultValue;
	}

	static public string get(guid setting, string defaultValue) {
		String value = get(setting);
		return value != null ? new string(value) : defaultValue;
	}

	static public guid get(guid setting, guid defaultValue) {
		String value = get(setting);
		return value != null ? new guid(value) : defaultValue;
	}

	static public date get(guid setting, date defaultValue) {
		String value = get(setting);
		return value != null ? new date(value) : defaultValue;
	}

	static public integer get(guid setting, integer defaultValue) {
		String value = get(setting);
		return value != null ? new integer(value) : defaultValue;
	}

	static public decimal get(guid setting, decimal defaultValue) {
		String value = get(setting);
		return value != null ? new decimal(value) : defaultValue;
	}

	static public bool get(guid setting, bool defaultValue) {
		String value = get(setting);
		return value != null ? new bool(value) : defaultValue;
	}

	static public string string(guid setting) {
		return get(setting, (string) null);
	}

	static public guid guid(guid setting) {
		return get(setting, (guid) null);
	}

	static public date date(guid setting) {
		return get(setting, (date) null);
	}

	static public integer integer(guid setting) {
		return get(setting, (integer) null);
	}

	static public decimal decimal(guid setting) {
		return get(setting, (decimal) null);
	}

	static public bool bool(guid setting) {
		return get(setting, (bool) null);
	}

	static public void set(guid setting, String value) {
		save(setting, null, null, null, value, -1, true);
	}

	static public void set(guid setting, primary value) {
		save(setting, null, null, null, value.toString(), -1, true);
	}

	static public void register(guid setting, guid parent, String name, String description, String defaultValue) {
		save(setting, parent, name, description, defaultValue, -1, false);
	}

	static public void register(guid setting, guid parent, String name, String description, primary defaultValue) {
		save(setting, parent, name, description, defaultValue.toString(), -1, false);
	}

	static public void register(guid setting, guid parent, String name, String description, primary defaultValue, int lock) {
		save(setting, parent, name, description, defaultValue.toString(), lock, false);
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
			settings.value.get().set(value != null ? value : "");
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

	static public string z8_get(guid setting, string defaultValue) {
		return get(setting, defaultValue);
	}

	static public guid z8_get(guid setting, guid defaultValue) {
		return get(setting, defaultValue);
	}

	static public date z8_get(guid setting, date defaultValue) {
		return get(setting, defaultValue);
	}

	static public integer z8_get(guid setting, integer defaultValue) {
		return get(setting, defaultValue);
	}

	static public decimal z8_get(guid setting, decimal defaultValue) {
		return get(setting, defaultValue);
	}

	static public bool z8_get(guid setting, bool defaultValue) {
		return get(setting, defaultValue);
	}

	static public string z8_string(guid setting) {
		return string(setting);
	}

	static public guid z8_guid(guid setting) {
		return guid(setting);
	}

	static public date z8_date(guid setting) {
		return date(setting);
	}

	static public integer z8_int(guid setting) {
		return integer(setting);
	}

	static public decimal z8_decimal(guid setting) {
		return decimal(setting);
	}

	static public bool z8_bool(guid setting) {
		return bool(setting);
	}

	static public void z8_set(guid setting, primary value) {
		set(setting, value);
	}

	static public void z8_register(guid setting, string name, string description, primary defaultValue) {
		register(setting, guid.Null, name.get(), description.get(), defaultValue);
	}

	static public void z8_register(guid setting, string name, string description, primary defaultValue, integer lock) {
		register(setting, guid.Null, name.get(), description.get(), defaultValue);
	}

	static public void z8_register(guid setting, guid parent, string name, string description, primary defaultValue) {
		register(setting, parent, name.get(), description.get(), defaultValue);
	}

	static public void z8_register(guid setting, guid parent, string name, string description, primary defaultValue, integer lock) {
		register(setting, parent, name.get(), description.get(), defaultValue, lock.getInt());
	}

}
