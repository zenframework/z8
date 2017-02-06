package org.zenframework.z8.server.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.form.Control;
import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.form.action.IAction;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.Message;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class JsonWriter {
	StringBuilder stream;
	List<Boolean> scopes;
	boolean quoteName = false;

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

	private String comma() {
		int size = scopes.size();
		return (size > 0 && scopes.get(size - 1)) ? "," : "";
	}

	private void openScope() {
		scopes.add(false);
	}

	private void closeScope() {
		scopes.remove(scopes.size() - 1);
		startSeparate();
	}

	private void startSeparate() {
		if(scopes.size() != 0)
			scopes.set(scopes.size() - 1, true);
	}

	public void startObject(string name) {
		startObject(name != null ? name.get() : null);
	}

	public void startObject(String name) {
		if(name != null) {
			stream.append(comma() + quoteName(name) + ":{");
			openScope();
		} else
			startObject();
	}

	public void startObject() {
		stream.append(comma() + '{');
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
			stream.append(comma() + quoteName(name) + ":[");
			openScope();
		} else
			startArray();
	}

	public void startArray() {
		stream.append(comma() + '[');
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
			stream.append(comma() + (quote ? JsonObject.quote(value) : value));
			startSeparate();
		}
	}

	public void write(JsonWriter writer) {
		stream.append(comma());
		stream.append(writer.stream);
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
		else if(value instanceof integer)
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

	public void write(RCollection<primary> value) {
		startArray();
		for(primary v : value)
			write(v);
		finishArray();
	}

	public void write(JsonObject value) {
		write(value.toString(), false);
	}

	public void write(JsonArray value) {
		write(value.toString(), false);
	}

	public void writeProperty(string name, String value) {
		writeProperty(name.get(), value, true);
	}

	public void writeProperty(String name, String value) {
		writeProperty(name, value, true);
	}

	public void writeProperty(String name, String value, boolean quoteValue) {
		if(value != null) {
			value = quoteValue ? JsonObject.quote(value) : value;
			stream.append(comma() + quoteName(name) + ":" + value);
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
		else if(value instanceof integer)
			writeProperty(name, ((integer)value).get());
		else if(value instanceof decimal)
			writeProperty(name, ((decimal)value).get());
		else if(value instanceof date) {
			boolean minMax = value.equals(date.Min) || value.equals(date.Max);
			writeProperty(name, '"' + (minMax ? "" : value.toString()) + '"', false);
		} else
			writeProperty(name, value.toString(), true);
	}

	public void writeProperty(string name, double value) {
		writeProperty(name.get(), value);
	}

	public void writeProperty(String name, double value) {
		stream.append(comma() + quoteName(name) + ":" + Double.toString(value));
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

	public void writeActions(Collection<Action> actions) {
		if(actions.isEmpty())
			return;

		startArray(Json.actions);

		for(IAction action : actions) {
			startObject();
			action.write(this);
			finishObject();
		}

		finishArray();
	}

	public void startResponse(String requestId, boolean success) {
		startObject();
		writeProperty(new string(Json.request), requestId);
		writeProperty(new string(Json.success), success);
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

	public void writeInfo(Collection<Message> messages, String server, file log) {
		startObject(Json.info);

		startArray(Json.messages);

		for(Message message : messages) {
			startObject();
			message.write(this);
			finishObject();
		}

		finishArray();

		if(log != null) {
			writeProperty(new string(Json.server), server);
			writeProperty(new string(Json.log), log.path.get());
		}

		finishObject();
	}

	public void finishResponse() {
		finishObject();
	}

	@Override
	public String toString() {
		return stream.toString();
	}
}
