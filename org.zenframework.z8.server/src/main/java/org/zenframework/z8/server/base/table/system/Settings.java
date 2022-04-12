package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.IntegerField;
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

	final static public integer Hidden = new integer(1);
	final static public integer HiddenValue = new integer(2);

	static public class fieldNames {
		public final static String Name = "Name";
		public final static String Comment = "Comment";
		public final static String Value = "Value";
		public final static String Attributes = "Attributes";
	}

	static public class strings {
		public final static String Title = "Settings.title";
		public final static String Name = "Settings.name";
		public final static String Comment = "Settings.comment";
		public final static String Value = "Settings.value";

		public final static String Version = "system.version";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
		public final static String Comment = Resources.get(strings.Comment);
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

	public TextField.CLASS<? extends TextField> value = new TextField.CLASS<TextField>(this);
	public IntegerField.CLASS<? extends IntegerField> attributes = new IntegerField.CLASS<IntegerField>(this);

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
		objects.add(attributes);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		value.setIndex("value");
		value.setName(fieldNames.Value);
		value.setDisplayName(displayNames.Value);

		attributes.setIndex("attributes");
		attributes.setName(fieldNames.Attributes);
	}

	@Override
	public void initStaticRecords() {
		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(name.get(), new string(strings.Version));
			addRecord(Version, record);
		}
	}

	static private String get(guid property) {
		try {
			Settings settings = new Settings.CLASS<Settings>().get();
			Field value = settings.value.get();
			return settings.readRecord(property, Arrays.asList(value)) ? value.string().get() : null;
		} catch(Throwable e) {
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
		return get(setting, (string)null);
	}

	static public guid guid(guid setting) {
		return get(setting, (guid)null);
	}

	static public date date(guid setting) {
		return get(setting, (date)null);
	}

	static public integer integer(guid setting) {
		return get(setting, (integer)null);
	}

	static public decimal decimal(guid setting) {
		return get(setting, (decimal)null);
	}

	static public bool bool(guid setting) {
		return get(setting, (bool)null);
	}

	static public void set(guid setting, String value) {
		save(setting, null, null, null, value, -1, true);
	}

	static public void set(guid setting, primary value) {
		save(setting, null, null, null, value.toString(), -1, true);
	}

	static public void register(guid setting, guid parent, String name, String comment, String defaultValue) {
		save(setting, parent, name, comment, defaultValue, -1, false);
	}

	static public void register(guid setting, guid parent, String name, String comment, primary defaultValue) {
		save(setting, parent, name, comment, defaultValue.toString(), -1, false);
	}

	static public void register(guid setting, guid parent, String name, String comment, primary defaultValue, int attributes) {
		save(setting, parent, name, comment, defaultValue.toString(), attributes, false);
	}

	static public void save(guid setting, guid parent, String name, String comment, String value, int attributes, boolean overrideValue) {
		Settings settings = new Settings.CLASS<Settings>().get();

		settings.saveState();
		boolean exists = settings.hasRecord(setting);
		settings.restoreState();

		if(parent != null)
			settings.parentId.get().set(parent);
		if(name != null)
			settings.name.get().set(name);
		if(comment != null)
			settings.description.get().set(comment);
		if(overrideValue || !exists)
			settings.value.get().set(value != null ? value : "");
		if(attributes >= 0)
			settings.attributes.get().set(attributes);
		if(exists)
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

	static public void z8_register(guid setting, string name, string comment, primary defaultValue) {
		register(setting, guid.Null, name.get(), comment.get(), defaultValue);
	}

	static public void z8_register(guid setting, guid parent, string name, string comment, primary defaultValue) {
		register(setting, parent, name.get(), comment.get(), defaultValue);
	}

	static public void z8_register(guid setting, guid parent, string name, string comment, primary defaultValue, integer attributes) {
		register(setting, parent, name.get(), comment.get(), defaultValue, attributes.getInt());
	}

}
