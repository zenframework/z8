package org.zenframework.z8.server.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.form.Control;
import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.form.action.IAction;
import org.zenframework.z8.server.base.form.report.IReport;
import org.zenframework.z8.server.base.form.report.Report;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.geometry.parser.GeoJsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.Message;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.security.Access;
import org.zenframework.z8.server.security.Role;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.geometry;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class JsonWriter {
	private StringBuilder stream;
	private List<Boolean> scopes;
	private boolean quoteName = false;
	private org.zenframework.z8.server.base.json.JsonWriter.CLASS<? extends org.zenframework.z8.server.base.json.JsonWriter> wrapper = null;

	public JsonWriter() {
		reset();
	}

	public JsonWriter(boolean quoteName) {
		this();
		this.quoteName = quoteName;
	}

	public void reset() {
		stream = new StringBuilder();
		scopes = new ArrayList<Boolean>();
		openScope();
	}

	private String quoteName(String name) {
		return quoteName ? JsonObject.quote(name) : ('"' + name + '"');
	}

	private StringBuilder appendComma() {
		int size = scopes.size();
		return size > 0 && scopes.get(size - 1) ? stream.append(",") : stream;
	}
	
	private void openScope() {
		scopes.add(false);
	}

	private void closeScope() {
		scopes.remove(scopes.size() - 1);
		startSeparate();
	}

	// TODO This must be private
	public void startSeparate() {
		if(scopes.size() != 0)
			scopes.set(scopes.size() - 1, true);
	}

	public void startObject(string name) {
		startObject(name != null ? name.get() : null);
	}

	public void startObject(String name) {
		if(name != null) {
			appendComma().append(quoteName(name)).append(":{");
			openScope();
		} else
			startObject();
	}

	public void startObject() {
		appendComma().append('{');
		openScope();
	}

	public void finishObject() {
		stream.append('}');
		closeScope();
	}

	public void startArray(string name) {
		startArray(name != null ? name.get() : null);
	}

	public void startArray(String name) {
		if(name != null) {
			appendComma().append(quoteName(name)).append(":[");
			openScope();
		} else
			startArray();
	}

	public void startArray() {
		appendComma().append('[');
		openScope();
	}

	public void finishArray() {
		stream.append(']');
		closeScope();
	}

	public void write(String value) {
		write(value, true);
	}

	public void write(String value, boolean quote) {
		if(value != null) {
			appendComma().append(quote ? JsonObject.quote(value) : value);
			startSeparate();
		}
	}

	public void write(JsonWriter writer) {
		appendComma().append(writer.stream);
		startSeparate();
	}

	public void write(double value) {
		write(Double.toString(value), false);
	}

	public void write(BigDecimal value) {
		write(value.toString(), false);
	}

	public void write(long value) {
		write(Long.toString(value), false);
	}

	public void write(int value) {
		write(Integer.toString(value), false);
	}

	public void write(boolean value) {
		write(Boolean.toString(value), false);
	}

	public void write(primary value) {
		if(value == null)
			write("null", false);
		else if(value instanceof bool)
			write(((bool)value).get());
		else if(value instanceof geometry) {
			geometry geometry = (geometry)value;
			if(!geometry.isEmpty())
				writeProperty(GeoJsonWriter.write(geometry), true);
		} else if(value instanceof integer)
			write(((integer)value).get());
		else if(value instanceof decimal)
			write(((decimal)value).get());
		else if(value instanceof date) {
			date dt = (date)value;
			boolean minMax = dt.equals(date.Min) || dt.equals(date.Max);
			write(minMax ? "" : value.toString());
		} else
			write(value.toString());
	}

	public void write(OBJECT value) {
		startObject();
		value.z8_write(getWrapper());
		finishObject();
	}

	@SuppressWarnings("rawtypes")
	public void write(RCollection value) {
		startArray();
		for(Object v : value)
			write(v);
		finishArray();
	}

	public void write(JsonObject value) {
		write(value.toString(), false);
	}

	public void write(JsonArray value) {
		write(value.toString(), false);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void write(Object value) {
		if (value instanceof OBJECT) {
			write((OBJECT) value);
		} else if (value instanceof OBJECT.CLASS) {
			write(((OBJECT.CLASS<? extends OBJECT>) value).get());
		} else if (value instanceof primary) {
			write((primary) value);
		} else if (value instanceof RCollection) {
			write((RCollection) value);
		} else if (value instanceof String) {
			write((String) value);
		} else if (value instanceof BigDecimal) {
			write((BigDecimal) value);
		} else if (value instanceof JsonWriter) {
			write((JsonWriter) value);
		} else if (value instanceof JsonObject) {
			write((JsonObject) value);
		} else if (value instanceof JsonArray) {
			write((JsonArray) value);
		} else
			throw new IllegalArgumentException(value.getClass().getName());
	}

	public void writeProperty(string name, String value) {
		writeProperty(name.get(), value, true);
	}

	public void writeProperty(String name, String value) {
		writeProperty(name, value, true);
	}

	public void writeProperty(String name, String value, boolean quoteValue) {
		if(value != null) {
			appendComma().append(quoteName(name)).append(":").append(quoteValue ? JsonObject.quote(value) : value);
			startSeparate();
		}
	}

	public void writeProperty(string name, primary value) {
		writeProperty(name.get(), value);
	}

	public void writeProperty(string name, primary value, primary defaultValue) {
		writeProperty(name.get(), value != null ? value : defaultValue);
	}

	public void writeProperty(String name, primary value) {
		if(value == null)
			return;

		if(value instanceof bool)
			writeProperty(name, ((bool)value).get());
		else if(value instanceof geometry) {
			geometry geometry = (geometry)value;
			if(!geometry.isEmpty())
				writeProperty(name, GeoJsonWriter.write(geometry), true);
		} else if(value instanceof integer)
			writeProperty(name, ((integer)value).get());
		else if(value instanceof decimal)
			writeProperty(name, ((decimal)value).get());
		else if(value instanceof date) {
			boolean minMax = value.equals(date.Min) || value.equals(date.Max);
			writeProperty(name, '"' + (minMax ? "" : value.toString()) + '"', false);
		} else
			writeProperty(name, value.toString(), true);
	}

	public void writeProperty(String name, OBJECT value) {
		startObject(name);
		value.z8_write(getWrapper());
		finishObject();
	}

	@SuppressWarnings("rawtypes")
	public void writeProperty(String name, RCollection value) {
		startArray(name);
		for(Object v : value)
			write(v);
		finishArray();
	}

	public void writeProperty(string name, double value) {
		writeProperty(name.get(), value);
	}

	public void writeProperty(String name, double value) {
		appendComma().append(quoteName(name)).append(":").append(Double.toString(value));
		startSeparate();
	}

	public void writeProperty(string name, BigDecimal value) {
		writeProperty(name.get(), value);
	}

	public void writeProperty(String name, BigDecimal value) {
		writeProperty(name, value.toString(), false);
	}

	public void writeProperty(string name, long value) {
		writeProperty(name.get(), value);
	}

	public void writeProperty(String name, long value) {
		writeProperty(name, Long.toString(value), false);
	}

	public void writeProperty(string name, int value) {
		writeProperty(name.get(), value);
	}

	public void writeProperty(String name, int value) {
		writeProperty(name, Integer.toString(value), false);
	}

	public void writeProperty(string name, boolean value) {
		writeProperty(name.get(), value);
	}

	public void writeProperty(String name, boolean value) {
		writeProperty(name, Boolean.toString(value), false);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void writeProperty(String name, Object value) {
		if (value instanceof OBJECT) {
			writeProperty(name, (OBJECT) value);
		} else if (value instanceof OBJECT.CLASS) {
			writeProperty(name, ((OBJECT.CLASS<? extends OBJECT>) value).get());
		} else if (value instanceof primary) {
			writeProperty(name, (primary) value);
		} else if (value instanceof RCollection) {
			writeProperty(name, (RCollection) value);
		} else if (value instanceof String) {
			writeProperty(name, (String) value);
		} else if (value instanceof BigDecimal) {
			writeProperty(name, (BigDecimal) value);
		} else if (value instanceof JsonWriter) {
			writeProperty(name, (JsonWriter) value);
		} else if (value instanceof JsonObject) {
			writeProperty(name, (JsonObject) value);
		} else if (value instanceof JsonArray) {
			writeProperty(name, (JsonArray) value);
		} else
			throw new IllegalArgumentException(value.getClass().getName());
	}

	public void writeNull(String name) {
		writeProperty(name, "null", false);
	}

	public void writeProperty(string name, JsonObject value) {
		writeProperty(name.get(), value);
	}

	public void writeProperty(String name, JsonObject value) {
		writeProperty(name, value.toString(), false);
	}

	public void writeProperty(string name, JsonArray value) {
		writeProperty(name.get(), value);
	}

	public void writeProperty(String name, JsonArray value) {
		writeProperty(name, value.toString(), false);
	}

	public void writeJsonProperty(String name, String json) {
		appendComma().append(quoteName(name)).append(":").append(json);
		startSeparate();
	}

	public void writeControls(string property, Collection<? extends Control> controls, Query query, Query context) {
		if(controls.isEmpty())
			return;

		startArray(property);
		for(Control control : controls) {
			startObject();
			control.writeMeta(this, query, context);
			finishObject();
		}
		finishArray();
	}

	public void writeSort(Collection<Field> sortFields) {
		if(sortFields.isEmpty())
			return;

		startArray(Json.sort);
		for(Field field : sortFields) {
			startObject();
			writeProperty(Json.property, field.id());
			writeProperty(Json.direction, field.sortDirection.toString());
			finishObject();
		}
		finishArray();
	}

	public void writeGroup(Collection<Field> groupFields) {
		if(groupFields.isEmpty())
			return;

		startArray(Json.group);
		for(Field field : groupFields)
			write(field.id());
		finishArray();
	}

	public void writeActions(Collection<Action> actions, Query query, Query context) {
		if(actions.isEmpty())
			return;

		startArray(Json.actions);

		for(IAction action : actions) {
			startObject();
			action.writeMeta(this, query, context);
			finishObject();
		}

		finishArray();
	}

	public void writeReports(Collection<Report> reports) {
		if(reports.isEmpty())
			return;

		startArray(Json.reports);

		for(IReport report : reports) {
			startObject();
			report.write(this);
			finishObject();
		}

		finishArray();
	}

	public void writeQueryAccess(Access access) {
		startObject(Json.access);
		writeProperty(Json.read, access.getRead());
		writeProperty(Json.write, access.getWrite());
		writeProperty(Json.create, access.getCreate());
		writeProperty(Json.copy, access.getCopy());
		writeProperty(Json.destroy, access.getDestroy());
		finishObject();
	}

	public void writeRoles(Collection<Role> roles) {
		startArray(Json.roles);
		for(Role role : roles) {
			startObject();
			writeProperty(Json.id, role.getId());
			writeProperty(Json.name, role.getName());
			finishObject();
		}
		finishArray();
	}

	public void writeAccess(Access access) {
		startArray(Json.access);
		for(guid accessKey : access.getKeys())
			write(accessKey);
		finishArray();
	}

	public void writeParameters(Map<string, primary> parameters) {
		startObject(Json.parameters);
		for(string key : parameters.keySet())
			writeProperty(key.get(), parameters.get(key));
		finishObject();
	}

	public void startResponse(String requestId, boolean success) {
		startObject();
		writeProperty(Json.request, requestId);
		writeProperty(Json.success, success);
	}

	public void startResponse(String requestId, boolean success, int status) {
		startResponse(requestId, success);
		writeProperty(Json.status, status);
	}

	public void writeInfo() {
		startObject(Json.info);
		startArray(Json.messages);
		finishArray();
		finishObject();
	}

	public void writeInfo(Collection<Message> messages, Collection<file> files, String server) {
		startObject(Json.info);

		startArray(Json.messages);
		for(Message message : messages) {
			startObject();
			message.write(this);
			finishObject();
		}
		finishArray();

		startArray(Json.files);
		for(file file : files) {
			startObject();
			file.write(this);
			finishObject();
		}
		finishArray();

		if(!files.isEmpty())
			writeProperty(new string(Json.server), server);

		finishObject();
	}

	public void finishResponse() {
		finishObject();
	}

	@Override
	public String toString() {
		return stream.toString();
	}

	private org.zenframework.z8.server.base.json.JsonWriter.CLASS<? extends org.zenframework.z8.server.base.json.JsonWriter> getWrapper() {
		if (wrapper == null) {
			wrapper = new org.zenframework.z8.server.base.json.JsonWriter.CLASS<org.zenframework.z8.server.base.json.JsonWriter>(null);
			wrapper.get().set(this);
		}
		return wrapper;
	}

}
