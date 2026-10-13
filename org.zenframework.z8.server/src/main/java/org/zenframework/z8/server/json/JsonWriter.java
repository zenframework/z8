package org.zenframework.z8.server.json;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.zenframework.z8.server.runtime.OBJECT;
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

public class JsonWriter  implements Closeable {
	private Writer stream;
	private List<Boolean> scopes;
	private boolean quoteName = false;
	private org.zenframework.z8.server.base.json.JsonWriter.CLASS<? extends org.zenframework.z8.server.base.json.JsonWriter> wrapper = null;

	public JsonWriter() {
		init(new StringWriter(), false);
	}

	public JsonWriter(boolean quoteName) {
		init(new StringWriter(), quoteName);
	}

	public JsonWriter(Writer customWriter) {
		init(customWriter, false);
	}

	public JsonWriter(File file) {
		this(file.toPath());
	}

	public JsonWriter(Path path) {
		init(openWriterFromPath(path), false);
	}

	private void init(Writer writer, boolean quoteName) {
		if (writer == null) {
			throw new IllegalArgumentException("Target stream writer cannot be null");
		}
		this.stream = writer instanceof StringWriter || writer instanceof BufferedWriter ? writer : new BufferedWriter(writer);
		this.quoteName = quoteName;
		scopes = new ArrayList<Boolean>();
		openScope();
	}

	@Override
	public void close() throws IOException {
		stream.flush();
		stream.close();
	}

	//////////////////////////////////////////////////////////////////

	private String quoteName(String name) {
		return quoteName ? JsonObject.quote(name) : ('"' + name + '"');
	}

	private JsonWriter appendComma() {
		int size = scopes.size();
		return size > 0 && scopes.get(size - 1) ? appendStream(",") : this;
	}
	
	private JsonWriter openScope() {
		scopes.add(false);
		return this;
	}

	private JsonWriter closeScope() {
		scopes.remove(scopes.size() - 1);
		return startSeparate();
	}

	// TODO This must be private
	public JsonWriter startSeparate() {
		int size = scopes.size();
		if(size != 0)
			scopes.set(size - 1, true);
		return this;
	}

	public JsonWriter startObject(string name) {
		return startObject(name != null ? name.get() : null);
	}

	public JsonWriter startObject(String name) {
		return name != null ? appendComma().appendStream(quoteName(name)).appendStream(":{").openScope() 
							: startObject();
	}

	public JsonWriter startObject() {
		return appendComma().appendStream("{").openScope();
	}

	public JsonWriter finishObject() {
		return appendStream("}").closeScope();
	}

	public JsonWriter startArray(string name) {
		return startArray(name != null ? name.get() : null);
	}

	public JsonWriter startArray(String name) {
		return name != null ? appendComma().appendStream(quoteName(name)).appendStream(":[").openScope()
							: startArray();
	}

	public JsonWriter startArray() {
		return appendComma().appendStream("[").openScope();
	}

	public JsonWriter finishArray() {
		return appendStream("]").closeScope();
	}

	public JsonWriter write(String value) {
		return write(value, true);
	}

	public JsonWriter write(String value, boolean quote) {
		return value != null ? appendComma().appendStream(quote ? JsonObject.quote(value) : value).startSeparate() : this;
	}

	public JsonWriter write(JsonWriter writer) {
		return appendComma().appendStream(writer.getStringContent()).startSeparate();
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

	public JsonWriter write(primary value) {
		if(value == null)
			return write("null", false);
		if(value instanceof bool)
			return write(((bool)value).get());
		if(value instanceof geometry) {
			geometry geometry = (geometry)value;
			if(!geometry.isEmpty())
				return writeProperty(GeoJsonWriter.write(geometry), true);
		}
		if(value instanceof integer)
			return write(((integer)value).get());
		if(value instanceof decimal)
			return write(((decimal)value).get());
		if(value instanceof date) {
			date dt = (date)value;
			boolean minMax = dt.equals(date.Min) || dt.equals(date.Max);
			return write(minMax ? "" : value.toString());
		}
		return write(value.toString());
	}

	public JsonWriter write(OBJECT value) {
		startObject();
		value.z8_write(getWrapper());
		return finishObject();
	}

	@SuppressWarnings("rawtypes")
	public JsonWriter write(RCollection value) {
		startArray();
		for (Object v : value)
			write(v);
		return finishArray();
	}

	public JsonWriter write(JsonObject value) {
		return write(value.toString(), false);
	}

	public JsonWriter write(JsonArray value) {
		return write(value.toString(), false);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JsonWriter write(Object value) {
		if (value instanceof OBJECT)
			return write((OBJECT) value);
		if (value instanceof OBJECT.CLASS)
			return write(((OBJECT.CLASS<? extends OBJECT>) value).get());
		if (value instanceof primary)
			return write((primary) value);
		if (value instanceof RCollection)
			return write((RCollection) value);
		if (value instanceof String)
			return write((String) value);
		if (value instanceof BigDecimal)
			return write((BigDecimal) value);
		if (value instanceof JsonWriter)
			return write((JsonWriter) value);
		if (value instanceof JsonObject)
			return write((JsonObject) value);
		if (value instanceof JsonArray)
			return write((JsonArray) value);

		throw new IllegalArgumentException(value.getClass().getName());
	}

	public JsonWriter writeProperty(string name, String value) {
		return writeProperty(name.get(), value, true);
	}

	public JsonWriter writeProperty(String name, String value) {
		return writeProperty(name, value, true);
	}

	public JsonWriter writeProperty(String name, String value, boolean quoteValue) {
		return value != null ? appendComma().appendStream(quoteName(name)).appendStream(":").appendStream(quoteValue ? JsonObject.quote(value)
																													 : value).startSeparate()
							 : this;
	}

	public JsonWriter writeProperty(string name, primary value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(string name, primary value, primary defaultValue) {
		return writeProperty(name.get(), value != null ? value : defaultValue);
	}

	public JsonWriter writeProperty(String name, primary value) {
		if(value == null)
			return this;

		if(value instanceof bool)
			return writeProperty(name, ((bool)value).get());
		if(value instanceof geometry) {
			geometry geometry = (geometry)value;
			if(!geometry.isEmpty())
				return writeProperty(name, GeoJsonWriter.write(geometry), true);
		}
		if(value instanceof integer)
			return writeProperty(name, ((integer)value).get());
		if(value instanceof decimal)
			return writeProperty(name, ((decimal)value).get());
		if(value instanceof date) {
			boolean minMax = value.equals(date.Min) || value.equals(date.Max);
			return writeProperty(name, '"' + (minMax ? "" : value.toString()) + '"', false);
		}
		return writeProperty(name, value.toString(), true);
	}

	public JsonWriter writeProperty(String name, OBJECT value) {
		startObject(name);
		value.z8_write(getWrapper());
		return finishObject();
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
		return appendComma().appendStream(quoteName(name)).appendStream(":").appendStream(Double.toString(value)).startSeparate();
	}

	public JsonWriter writeProperty(string name, BigDecimal value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(String name, BigDecimal value) {
		return writeProperty(name, value.toString(), false);
	}

	public JsonWriter writeProperty(string name, long value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(String name, long value) {
		return writeProperty(name, Long.toString(value), false);
	}

	public JsonWriter writeProperty(string name, int value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(String name, int value) {
		return writeProperty(name, Integer.toString(value), false);
	}

	public JsonWriter writeProperty(string name, boolean value) {
		return writeProperty(name.get(), value);
	}

	public JsonWriter writeProperty(String name, boolean value) {
		return writeProperty(name, Boolean.toString(value), false);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JsonWriter writeProperty(String name, Object value) {
		if (value instanceof OBJECT)
			return writeProperty(name, (OBJECT) value);
		if (value instanceof OBJECT.CLASS)
			return writeProperty(name, ((OBJECT.CLASS<? extends OBJECT>) value).get());
		if (value instanceof primary)
			return writeProperty(name, (primary) value);
		if (value instanceof RCollection)
			return writeProperty(name, (RCollection) value);
		if (value instanceof String)
			return writeProperty(name, (String) value);
		if (value instanceof BigDecimal)
			return writeProperty(name, (BigDecimal) value);
		if (value instanceof JsonWriter)
			return writeProperty(name, (JsonWriter) value);
		if (value instanceof JsonObject)
			return writeProperty(name, (JsonObject) value);
		if (value instanceof JsonArray)
			return writeProperty(name, (JsonArray) value);

		throw new IllegalArgumentException(value.getClass().getName());
	}

	public JsonWriter writeNull(String name) {
		return writeProperty(name, "null", false);
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

	public JsonWriter writeJsonProperty(String name, String json) {
		return appendComma().appendStream(quoteName(name)).appendStream(":").appendStream(json).startSeparate();
	}

	public JsonWriter writeControls(string property, Collection<? extends Control> controls, Query query, Query context) {
		if(controls.isEmpty())
			return this;

		startArray(property);
		for(Control control : controls) {
			startObject();
			control.writeMeta(this, query, context);
			finishObject();
		}
		return finishArray();
	}

	public JsonWriter writeSort(Collection<Field> sortFields) {
		if(sortFields.isEmpty())
			return this;

		startArray(Json.sort);
		for(Field field : sortFields) {
			startObject();
			writeProperty(Json.property, field.id());
			writeProperty(Json.direction, field.sortDirection.toString());
			finishObject();
		}
		return finishArray();
	}

	public JsonWriter writeGroup(Collection<Field> groupFields) {
		if(groupFields.isEmpty())
			return this;

		startArray(Json.group);
		for(Field field : groupFields)
			write(field.id());
		return finishArray();
	}

	public JsonWriter writeActions(Collection<Action> actions, Query query, Query context) {
		if(actions.isEmpty())
			return this;

		startArray(Json.actions);

		for(IAction action : actions) {
			startObject();
			action.writeMeta(this, query, context);
			finishObject();
		}

		return finishArray();
	}

	public JsonWriter writeReports(Collection<Report> reports) {
		if(reports.isEmpty())
			return this;

		startArray(Json.reports);

		for(IReport report : reports) {
			startObject();
			report.write(this);
			finishObject();
		}

		return finishArray();
	}

	public JsonWriter writeAccess(IAccess access) {
		return startObject(Json.access)
			  .writeProperty(Json.read, access.read())
			  .writeProperty(Json.write, access.write())
			  .writeProperty(Json.create, access.create())
			  .writeProperty(Json.copy, access.copy())
			  .writeProperty(Json.destroy, access.destroy())
			  .finishObject();
	}

	public JsonWriter startResponse(String requestId, boolean success) {
		return startObject().writeProperty(Json.request, requestId).writeProperty(Json.success, success);
	}

	public JsonWriter startResponse(String requestId, boolean success, int status) {
		return startResponse(requestId, success).writeProperty(Json.status, status);
	}

	public JsonWriter writeInfo() {
		return startObject(Json.info).startArray(Json.messages).finishArray().finishObject();
	}

	public JsonWriter writeInfo(Collection<Message> messages, Collection<file> files, String server) {
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

		return finishObject();
	}

	public JsonWriter finishResponse() {
		return finishObject();
	}

	@Override
	public String toString() {
		return getStringContent();
	}

	public org.zenframework.z8.server.base.json.JsonWriter.CLASS<? extends org.zenframework.z8.server.base.json.JsonWriter> getWrapper() {
		if (wrapper == null) {
			wrapper = new org.zenframework.z8.server.base.json.JsonWriter.CLASS<org.zenframework.z8.server.base.json.JsonWriter>(null);
			wrapper.get().set(this);
		}
		return wrapper;
	}

	private JsonWriter appendStream(String value) {
		try {
			if (value != null) {
				this.stream.write(value);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to write JSON chunk to the stream", e);
		}
		return this;
	}
	
	private String getStringContent() {
		if (stream instanceof StringWriter) {
			return stream.toString();
		}
		return  resolveDescriptor();
	}

	private static Writer openWriterFromPath(Path path) {
		if (path == null) throw new IllegalArgumentException("Path reference cannot be null");
		try {
			return Files.newBufferedWriter(path, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Failed to open path channel for: " + path.toAbsolutePath(), e);
		}
	}

	private String resolveDescriptor() {
		if (stream == null || stream instanceof java.io.StringWriter) {
			return null;
		}
		return "[" + stream.getClass().getSimpleName() + " Custom Stream Target]";
	}

}
