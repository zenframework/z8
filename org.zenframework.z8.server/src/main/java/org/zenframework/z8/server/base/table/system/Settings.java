package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Settings extends Table {
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

	public static class CLASS<T extends Settings> extends Table.CLASS<T> {
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
			addRecord(Version, record);
		}
	}

	static public String get(guid property) {
		Settings settings = new Settings.CLASS<Settings>().get();
		Field value = settings.value.get();
		return settings.readRecord(property, Arrays.asList(value)) ? value.string().get() : null;
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

	static public void set(guid property, primary value) {
		Settings settings = new Settings.CLASS<Settings>().get();
		settings.value.get().set(new string(value.toString()));
		settings.updateOrCreate(property);
	}

	static public void set(guid property, String value) {
		Settings.set(property, new string(value));
	}

	static public String version() {
		return get(Version);
	}

	static public string z8_get(guid property) {
		return new string(get(property));
	}

	static public string z8_get(guid property, string defaultValue) {
		return get(property, defaultValue);
	}

	static public guid z8_get(guid property, guid defaultValue) {
		return get(property, defaultValue);
	}

	static public date z8_get(guid property, date defaultValue) {
		return get(property, defaultValue);
	}

	static public integer z8_get(guid property, integer defaultValue) {
		return get(property, defaultValue);
	}

	static public decimal z8_get(guid property, decimal defaultValue) {
		return get(property, defaultValue);
	}

	static public void z8_set(guid property, primary value) {
		set(property, value);
	}
}
