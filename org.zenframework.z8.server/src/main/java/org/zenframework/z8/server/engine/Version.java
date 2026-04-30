package org.zenframework.z8.server.engine;

import java.util.List;

import org.zenframework.z8.server.base.table.system.Settings;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IRuntime;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.utils.StringUtils;

public class Version {
	static public final String RecordId = "recordId";
	static public final String ClassName = "className";
	static public final String Name = "name";
	static public final String DisplayName = "displayName";
	static public final String ControlSum = "controlSum";
	static public final String NewName = "newName";
	static public final String NewDisplayName = "newDisplayName";
	static public final String NewControlSum = "newControlSum";
	static public final String Changed = "changed";

	static public final String Version = "Version";

	private final String version;
	private final JsonArray details;

	private Version(String version, JsonArray details) {
		this.version = version;
		this.details = details;
	}

	public String getVersion() {
		return version;
	}

	public String getDetails() {
		return details.toString();
	}

	static public Version getVersion(IRuntime runtime) {
		return getVersion(runtime.getControlObjects());
	}

	static public Version getVersion(List<OBJECT> objects) {
		JsonArray details = new JsonArray();

		int controlSum = 0;

		for(OBJECT object : objects) {
			JsonObject json = new JsonObject();
			json.set(RecordId, object.getCLASS().key());
			json.set(ClassName, object.classId());
			json.set(Name, object.name());
			json.set(DisplayName, object.displayName());
			json.set(ControlSum, Integer.toString(object.controlSum()));
			details.add(json);
			controlSum += object.controlSum();
		}

		String version = formatVersion(controlSum);

		JsonObject json = new JsonObject();
		json.set(RecordId, Settings.Version);
		json.set(ClassName, Version);
		json.set(Name, Version);
		json.set(DisplayName, "Database version");
		json.set(ControlSum, version);
		details.insert(0, json);

		return new Version(version, details);
	}

	static public JsonArray getVersionDiff(Version v1, Version v2) {
		JsonArray data = new JsonArray(v1.getDetails());
		JsonArray newData = new JsonArray(v2.getDetails());

		for (int i = 0; i < data.size(); i++) {
			JsonObject record = data.getJsonObject(i);
			JsonObject newRecord = remove(newData, ClassName, record.getString(ClassName));
			if (newRecord != null) {
				record.set(NewName, newRecord.getString(Name));
				record.set(NewDisplayName, newRecord.getString(DisplayName));
				record.set(NewControlSum, newRecord.getString(ControlSum));
				record.set(Changed, !equals(record.getString(ControlSum), record.getString(NewControlSum)));
			}
		}

		for (int i = 0; i < newData.size(); i++) {
			JsonObject record = newData.getJsonObject(i);
			record.set(NewName, record.getString(Name));
			record.set(Name, "");
			record.set(NewDisplayName, record.getString(DisplayName));
			record.set(DisplayName, "");
			record.set(NewControlSum, record.getString(ControlSum));
			record.set(ControlSum, "");
			record.set(Changed, true);
			data.add(record);
		}

		return data;
	}

	static public Version readVersion(String schema) {
		JsonArray details = new JsonArray(Settings.get(Settings.VersionDetails, "[]"));
		JsonObject version = find(details, ClassName, Version);
		return new Version(version != null ? version.getString(ControlSum) : formatVersion(0), details);
	}

	static private JsonObject find(JsonArray data, String field, String value) {
		return findRemove(data, field, value, false);
	}

	static private JsonObject remove(JsonArray data, String field, String value) {
		return findRemove(data, field, value, true);
	}

	static private JsonObject findRemove(JsonArray data, String field, String value, boolean remove) {
		for (int i = 0; i < data.size(); i++) {
			JsonObject record = data.getJsonObject(i);
			if (value.equals(record.getString(field))) {
				if (remove)
					data.remove(i);
				return record;
			}
		}
		return null;
	}

	static private boolean equals(Object o1, Object o2) {
		if (o1 == o2)
			return true;
		if (o1 == null || o2 == null)
			return false;
		return o1.equals(o2);
	}

	static private String formatVersion(int controlSum) {
		String version = StringUtils.padLeft("" + Math.abs(controlSum), 10, '0');
		version = version.substring(0, 1) + "." + version.substring(1, 4) + "." + version.substring(4, 7) + "." + version.substring(7);
		return version;
	}
}
