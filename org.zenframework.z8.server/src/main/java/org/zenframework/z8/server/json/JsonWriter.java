package org.zenframework.z8.server.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.security.IAccess;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.geometry;
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

	private JsonWriter comma() {
		int size = scopes.size();
		if(size > 0 && scopes.get(size - 1)) {
			scopes.set(scopes.size() - 1, false);
			stream.append(",");
		}
		return this;
	}

	private JsonWriter append(char value) {
		stream.append(value);
		return this;
	}

	private JsonWriter append(String value) {
		stream.append(value);
		return this;
	}

	private JsonWriter openScope() {
		scopes.add(false);
		return this;
	}

	private JsonWriter closeScope() {
		scopes.remove(scopes.size() - 1);
		return separate();
	}

	private JsonWriter separate() {
		if(scopes.size() != 0)
			scopes.set(scopes.size() - 1, true);
		return this;
	}

	public JsonWriter startObject(string name) {
		return startObject(name != null ? name.get() : null);
	}

	public JsonWriter startObject(String name) {
		return name != null ? comma().append(quoteName(name)).append(":{").openScope() : startObject();
	}

	public JsonWriter startObject() {
		return comma().append('{').openScope();
	}

	public JsonWriter finishObject() {
		stream.append('}');
		return closeScope();
	}

	public JsonWriter startArray(string name) {
		return startArray(name != null ? name.get() : null);
	}

	public JsonWriter startArray(String name) {
		return name != null ? comma().append(quoteName(name)).append(":[").openScope() : startArray();
	}

	public JsonWriter startArray() {
		return comma().append('[').openScope();
	}

	public JsonWriter finishArray() {
		stream.append(']');
		return closeScope();
	}

	public JsonWriter write(String value, boolean quote) {
		return comma().append(quote ? JsonObject.quote(value) : value).separate();
	}

	public JsonWriter write(String value) {
		return write(value, true);
	}

	public JsonWriter write(JsonWriter writer) {
		return write(writer.stream.toString(), false);
	}

	public JsonWriter write(double value) {
		return write(Double.toString(value), false);
	}

	public JsonWriter write(BigDecimal value) {
		return write(value.toString(), false);
	}

	public JsonWriter write(long value) {
		return write(Long.toString(value), false);
	}

	public JsonWriter write(int value) {
		return write(Integer.toString(value), false);
	}

	public JsonWriter write(boolean value) {
		return write(Boolean.toString(value), false);
	}

	public JsonWriter write(geometry value) {
		return !value.isEmpty() ? write(GeoJsonWriter.write(value), false) : this;
	}

	private JsonWriter write(date value) {
		return value.equals(date.Min) || value.equals(date.Max) ? write((primary) null) : write(value.getTicks());
	}

	private JsonWriter write(decimal value) {
		return value.isNaN() ? write("NaN") : value.equals(decimal.Min) || value.equals(decimal.Max) ? write((primary) null) : write(value.get());
	}

	private JsonWriter write(integer value) {
		return value.equals(integer.Min) || value.equals(integer.Max) ? write((primary) null) : write(value.get());
	}

	public JsonWriter write(primary value) {
		if(value == null)
			return write((String)null);
		if(value instanceof bool)
			return write(((bool)value).get());
		if(value instanceof geometry)
			return write((geometry)value);
		if(value instanceof integer)
			return write((integer)value);
		if(value instanceof decimal)
			return write((decimal)value);
		if(value instanceof date)
			return write((date)value);
		return write(value.toString());
	}

	@SuppressWarnings("rawtypes")
	public JsonWriter write(RCollection value) {
		startArray();
		for(Object v : value)
			write(v);
		return finishArray();
	}

	public JsonWriter write(JsonObject value) {
		return write(value.toString(), false);
	}

	public JsonWriter write(JsonArray value) {
		return write(value.toString(), false);
	}

	@SuppressWarnings({ "rawtypes" })
	public JsonWriter write(Object value) {
		if(value instanceof primary)
			return write((primary)value);
		if(value instanceof RCollection)
			return write((RCollection)value);
		if(value instanceof String)
			return write((String)value);
		if(value instanceof BigDecimal)
			return write((BigDecimal)value);
		if(value instanceof JsonWriter)
			return write((JsonWriter)value);
		if(value instanceof JsonObject)
			return write((JsonObject)value);
		if(value instanceof JsonArray)
			return write((JsonArray)value);
		throw new IllegalArgumentException(value.getClass().getName());
	}

	private JsonWriter property(String name) {
		return comma().append(quoteName(name)).append(":");
	}

	public JsonWriter writeProperty(String name, String value, boolean quote) {
		return property(name).write(value, quote);
	}

	public JsonWriter writeProperty(string name, String value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(String name, String value) {
		return property(name).write(value);
	}

	public JsonWriter writeProperty(string name, primary value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(string name, primary value, primary defaultValue) {
		return writeProperty(name, value != null ? value : defaultValue);
	}

	public JsonWriter writeProperty(String name, primary value) {
		return property(name).write(value);
	}

	@SuppressWarnings("rawtypes")
	public JsonWriter writeProperty(String name, RCollection value) {
		startArray(name);
		for(Object v : value)
			write(v);
		return finishArray();
	}

	public JsonWriter writeProperty(string name, double value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(String name, double value) {
		return property(name).write(value);
	}

	public JsonWriter writeProperty(string name, BigDecimal value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(String name, BigDecimal value) {
		return property(name).write(value);
	}

	public JsonWriter writeProperty(string name, long value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(String name, long value) {
		return property(name).write(value);
	}

	public JsonWriter writeProperty(string name, int value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(String name, int value) {
		return property(name).write(value);
	}

	public JsonWriter writeProperty(string name, boolean value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(String name, boolean value) {
		return property(name).write(value);
	}

	@SuppressWarnings({ "rawtypes" })
	public JsonWriter writeProperty(String name, Object value) {
		if(value instanceof primary)
			return writeProperty(name, (primary)value);
		if(value instanceof RCollection)
			return writeProperty(name, (RCollection)value);
		if(value instanceof String)
			return writeProperty(name, (String)value);
		if(value instanceof BigDecimal)
			return writeProperty(name, (BigDecimal)value);
		if(value instanceof JsonWriter)
			return writeProperty(name, (JsonWriter)value);
		if(value instanceof JsonObject)
			return writeProperty(name, (JsonObject)value);
		if(value instanceof JsonArray)
			return writeProperty(name, (JsonArray)value);
		throw new IllegalArgumentException(value.getClass().getName());
	}

	public JsonWriter writeProperty(string name, JsonObject value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(String name, JsonObject value) {
		return writeProperty(name, value.toString(), false);
	}

	public JsonWriter writeProperty(string name, JsonArray value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(String name, JsonArray value) {
		return writeProperty(name, value.toString(), false);
	}

	public JsonWriter writeNull(String name) {
		return writeProperty(name, "null", false);
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

	public void writeAccess(IAccess access) {
		startObject(Json.access);
		writeProperty(Json.read, access.read());
		writeProperty(Json.write, access.write());
		writeProperty(Json.create, access.create());
		writeProperty(Json.copy, access.copy());
		writeProperty(Json.destroy, access.destroy());
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

	public org.zenframework.z8.server.base.json.JsonWriter.CLASS<? extends org.zenframework.z8.server.base.json.JsonWriter> getWrapper() {
		if (wrapper == null) {
			wrapper = new org.zenframework.z8.server.base.json.JsonWriter.CLASS<org.zenframework.z8.server.base.json.JsonWriter>(null);
			wrapper.get().set(this);
		}
		return wrapper;
	}

}
