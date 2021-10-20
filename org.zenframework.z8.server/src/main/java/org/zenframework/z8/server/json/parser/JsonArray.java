package org.zenframework.z8.server.json.parser;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.zenframework.z8.server.types.guid;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>
 * &nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as
 * well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0x-</code> <small>(hex)</small> prefix.</li>
 * </ul>
 * 
 * @author JSON.org
 * @version 2010-12-28
 */
public class JsonArray extends ArrayList<Object> {

	private static final long serialVersionUID = -1023358096617596153L;

	/**
	 * Construct an empty JSONArray.
	 */
	public JsonArray() {
	}

	/**
	 * Construct a JSONArray from a JSONTokener.
	 * 
	 * @param x
	 *            A JSONTokener
	 * @throws JsonException
	 *             If there is a syntax error.
	 */
	public JsonArray(JsonTokener x) throws JsonException {
		this();
		if(x.nextClean() != '[') {
			throw x.syntaxError("A JSONArray text must start with '['");
		}
		if(x.nextClean() != ']') {
			x.back();
			for(;;) {
				if(x.nextClean() == ',') {
					x.back();
					super.add(JsonObject.NULL);
				} else {
					x.back();
					super.add(x.nextValue());
				}
				switch(x.nextClean()) {
				case ';':
				case ',':
					if(x.nextClean() == ']') {
						return;
					}
					x.back();
					break;
				case ']':
					return;
				default:
					throw x.syntaxError("Expected a ',' or ']'");
				}
			}
		}
	}

	/**
	 * Construct a JSONArray from a source JSON text.
	 * 
	 * @param source
	 *            A string that begins with <code>[</code>&nbsp;<small>(left
	 *            bracket)</small> and ends with <code>]</code>
	 *            &nbsp;<small>(right bracket)</small>.
	 * @throws JsonException
	 *             If there is a syntax error.
	 */

	public JsonArray(String source) throws JsonException {
		this(new JsonTokener(source == null || source.isEmpty() ? "[]" : (source.charAt(0) != '[' ? '[' + source + ']' : source)));
	}

	/**
	 * Construct a JSONArray from a Collection.
	 * 
	 * @param collection
	 *            A Collection.
	 */
	public JsonArray(Collection<?> collection) {
		if(collection != null) {
			Iterator<?> iter = collection.iterator();
			while(iter.hasNext()) {
				super.add(JsonObject.wrap(iter.next()));
			}
		}
	}

	/**
	 * Construct a JSONArray from an array
	 * 
	 * @throws JsonException
	 *             If not an array.
	 */
	public JsonArray(Object array) throws JsonException {
		if(array != null) {
			if(array.getClass().isArray()) {
				int length = Array.getLength(array);
				for(int i = 0; i < length; i += 1) {
					this.put(JsonObject.wrap(Array.get(array, i)));
				}
			} else {
				throw new JsonException("JSONArray initial value should be a string or collection or array.");
			}
		}
	}

	/**
	 * Get the object value associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return An object value.
	 * @throws JsonException
	 *             If there is no value for the index.
	 */
	@Override
	public Object get(int index) throws JsonException {
		Object object = opt(index);
		if(object == null) {
			throw new JsonException("JSONArray[" + index + "] not found.");
		}
		return object;
	}

	/**
	 * Get the boolean value associated with an index. The string values "true"
	 * and "false" are converted to boolean.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The truth.
	 * @throws JsonException
	 *             If there is no value for the index or if the value is not
	 *             convertible to boolean.
	 */
	public boolean getBoolean(int index) throws JsonException {
		Object object = get(index);
		if(object.equals(Boolean.FALSE) || (object instanceof String && ((String)object).equalsIgnoreCase("false"))) {
			return false;
		} else if(object.equals(Boolean.TRUE) || (object instanceof String && ((String)object).equalsIgnoreCase("true"))) {
			return true;
		}
		throw new JsonException("JSONArray[" + index + "] is not a boolean.");
	}

	/**
	 * Get the double value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JsonException
	 *             If the key is not found or if the value cannot be converted
	 *             to a number.
	 */
	public double getDouble(int index) throws JsonException {
		Object object = get(index);
		try {
			return object instanceof Number ? ((Number)object).doubleValue() : Double.parseDouble((String)object);
		} catch(Exception e) {
			throw new JsonException("JSONArray[" + index + "] is not a number.");
		}
	}

	/**
	 * Get the int value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JsonException
	 *             If the key is not found or if the value is not a number.
	 */
	public int getInt(int index) throws JsonException {
		Object object = get(index);
		try {
			return object instanceof Number ? ((Number)object).intValue() : Integer.parseInt((String)object);
		} catch(Exception e) {
			throw new JsonException("JSONArray[" + index + "] is not a number.");
		}
	}

	public guid getGuid(int index) throws JsonException {
		Object object = get(index);
		return object instanceof guid ? (guid)object : new guid(object.toString());
	}

	/**
	 * Get the JSONArray associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A JSONArray value.
	 * @throws JsonException
	 *             If there is no value for the index. or if the value is not a
	 *             JSONArray
	 */
	public JsonArray getJsonArray(int index) throws JsonException {
		Object object = get(index);
		if(object instanceof JsonArray) {
			return (JsonArray)object;
		}
		throw new JsonException("JSONArray[" + index + "] is not a JSONArray.");
	}

	/**
	 * Get the JSONObject associated with an index.
	 * 
	 * @param index
	 *            subscript
	 * @return A JSONObject value.
	 * @throws JsonException
	 *             If there is no value for the index or if the value is not a
	 *             JSONObject
	 */
	public JsonObject getJsonObject(int index) throws JsonException {
		Object object = get(index);
		if(object instanceof JsonObject) {
			return (JsonObject)object;
		}
		throw new JsonException("JSONArray[" + index + "] is not a JSONObject.");
	}

	/**
	 * Get the long value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JsonException
	 *             If the key is not found or if the value cannot be converted
	 *             to a number.
	 */
	public long getLong(int index) throws JsonException {
		Object object = get(index);
		try {
			return object instanceof Number ? ((Number)object).longValue() : Long.parseLong((String)object);
		} catch(Exception e) {
			throw new JsonException("JSONArray[" + index + "] is not a number.");
		}
	}

	/**
	 * Get the string associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A string value.
	 * @throws JsonException
	 *             If there is no value for the index.
	 */
	public String getString(int index) throws JsonException {
		Object object = get(index);
		return object == JsonObject.NULL ? null : object.toString();
	}

	/**
	 * Determine if the value is null.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return true if the value at the index is null, or if there is no value.
	 */
	public boolean isNull(int index) {
		return JsonObject.NULL.equals(opt(index));
	}

	/**
	 * Make a string from the contents of this JSONArray. The
	 * <code>separator</code> string is inserted between each element. Warning:
	 * This method assumes that the data structure is acyclical.
	 * 
	 * @param separator
	 *            A string that will be inserted between the elements.
	 * @return a string.
	 * @throws JsonException
	 *             If the array contains an invalid number.
	 */
	public String join(String separator) throws JsonException {
		int len = length();
		StringBuilder sb = new StringBuilder(1024);

		for(int i = 0; i < len; i += 1) {
			if(i > 0) {
				sb.append(separator);
			}
			sb.append(JsonObject.valueToString(super.get(i)));
		}
		return sb.toString();
	}

	/**
	 * Get the number of elements in the JSONArray, included nulls.
	 *
	 * @return The length (or size).
	 */
	public int length() {
		return super.size();
	}

	/**
	 * Get the optional object value associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return An object value, or null if there is no object at that index.
	 */
	public Object opt(int index) {
		return (index < 0 || index >= length()) ? null : super.get(index);
	}

	/**
	 * Get the optional boolean value associated with an index. It returns false
	 * if there is no value at that index, or if the value is not Boolean.TRUE
	 * or the String "true".
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The truth.
	 */
	public boolean optBoolean(int index) {
		return optBoolean(index, false);
	}

	/**
	 * Get the optional boolean value associated with an index. It returns the
	 * defaultValue if there is no value at that index or if it is not a Boolean
	 * or the String "true" or "false" (case insensitive).
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            A boolean default.
	 * @return The truth.
	 */
	public boolean optBoolean(int index, boolean defaultValue) {
		try {
			return getBoolean(index);
		} catch(Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional double value associated with an index. NaN is returned
	 * if there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 */
	public double optDouble(int index) {
		return optDouble(index, Double.NaN);
	}

	/**
	 * Get the optional double value associated with an index. The defaultValue
	 * is returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 *
	 * @param index
	 *            subscript
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public double optDouble(int index, double defaultValue) {
		try {
			return getDouble(index);
		} catch(Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional int value associated with an index. Zero is returned if
	 * there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 */
	public int optInt(int index) {
		return optInt(index, 0);
	}

	/**
	 * Get the optional int value associated with an index. The defaultValue is
	 * returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public int optInt(int index, int defaultValue) {
		try {
			return getInt(index);
		} catch(Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional JSONArray associated with an index.
	 * 
	 * @param index
	 *            subscript
	 * @return A JSONArray value, or null if the index has no value, or if the
	 *         value is not a JSONArray.
	 */
	public JsonArray optJSONArray(int index) {
		Object o = opt(index);
		return o instanceof JsonArray ? (JsonArray)o : null;
	}

	/**
	 * Get the optional JSONObject associated with an index. Null is returned if
	 * the key is not found, or null if the index has no value, or if the value
	 * is not a JSONObject.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A JSONObject value.
	 */
	public JsonObject optJSONObject(int index) {
		Object o = opt(index);
		return o instanceof JsonObject ? (JsonObject)o : null;
	}

	/**
	 * Get the optional long value associated with an index. Zero is returned if
	 * there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 */
	public long optLong(int index) {
		return optLong(index, 0);
	}

	/**
	 * Get the optional long value associated with an index. The defaultValue is
	 * returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public long optLong(int index, long defaultValue) {
		try {
			return getLong(index);
		} catch(Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional string value associated with an index. It returns an
	 * empty string if there is no value at that index. If the value is not a
	 * string and is not null, then it is coverted to a string.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A String value.
	 */
	public String optString(int index) {
		return optString(index, "");
	}

	/**
	 * Get the optional string associated with an index. The defaultValue is
	 * returned if the key is not found.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return A String value.
	 */
	public String optString(int index, String defaultValue) {
		Object object = opt(index);
		return object != null ? object.toString() : defaultValue;
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONArray which
	 * is produced from a Collection.
	 * 
	 * @param value
	 *            A Collection value.
	 * @return this.
	 */
	public JsonArray put(Collection<Object> value) {
		return put((Object)(value instanceof JsonArray ? value : new JsonArray(value)));
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONObject which
	 * is produced from a Map.
	 * 
	 * @param value
	 *            A Map value.
	 * @return this.
	 */
	public JsonArray put(Map<?, ?> value) {
		return put((Object)(value instanceof JsonObject ? value : new JsonObject(value)));
	}

	/**
	 * Append an object value. This increases the array's length by one.
	 * 
	 * @param value
	 *            An object value. The value should be a Boolean, Double,
	 *            Integer, JSONArray, JSONObject, Long, or String, or the
	 *            JSONObject.NULL object.
	 * @return this.
	 */
	public JsonArray put(Object value) {
		JsonObject.testValidity(value);
		super.add(JsonObject.wrap(value));
		return this;
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONArray which
	 * is produced from a Collection.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A Collection value.
	 * @return this.
	 * @throws JsonException
	 *             If the index is negative or if the value is not finite.
	 */
	public JsonArray put(int index, Collection<Object> value) throws JsonException {
		return put(index, (Object)(value instanceof JsonArray ? value : new JsonArray(value)));
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONObject which
	 * is produced from a Map.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            The Map value.
	 * @return this.
	 * @throws JsonException
	 *             If the index is negative or if the the value is an invalid
	 *             number.
	 */
	public JsonArray put(int index, Map<?, ?> value) throws JsonException {
		return put(index, (Object)(value instanceof JsonObject ? value : new JsonObject(value)));
	}

	/**
	 * Put or replace an object value in the JSONArray. If the index is greater
	 * than the length of the JSONArray, then null elements will be added as
	 * necessary to pad it out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            The value to put into the array. The value should be a
	 *            Boolean, Double, Integer, JSONArray, JSONObject, Long, or
	 *            String, or the JSONObject.NULL object.
	 * @return this.
	 * @throws JsonException
	 *             If the index is negative or if the the value is an invalid
	 *             number.
	 */

	public JsonArray put(int index, Object value) throws JsonException {
		JsonObject.testValidity(value);
		if(index < 0) {
			throw new JsonException("JSONArray[" + index + "] not found.");
		}
		if(index < length()) {
			super.set(index, value);
		} else {
			while(index != length()) {
				super.add(JsonObject.NULL);
			}
			super.add(JsonObject.wrap(value));
		}
		return this;
	}

	/**
	 * Insert a value to the JSONArray, before selected position. This increases
	 * the array's length by one.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A Collection value.
	 * @return this.
	 * @throws JsonException
	 *             If the index is negative or if the value is not finite.
	 */
	public JsonArray insert(int index, Object value) {
		JsonObject.testValidity(value);
		super.add(index, JsonObject.wrap(value));
		return this;
	}

	/**
	 * Remove an index and close the hole.
	 * 
	 * @param index
	 *            The index of the element to be removed.
	 * @return The value that was associated with the index, or null if there
	 *         was no value.
	 */
	@Override
	public Object remove(int index) {
		Object o = opt(index);
		super.remove(index);
		return o;
	}

	/**
	 * Produce a JSONObject by combining a JSONArray of names with the values of
	 * this JSONArray.
	 * 
	 * @param names
	 *            A JSONArray containing a list of key strings. These will be
	 *            paired with the values.
	 * @return A JSONObject, or null if there are no names or if this JSONArray
	 *         has no values.
	 * @throws JsonException
	 *             If any of the names are null.
	 */
	public JsonObject toJSONObject(JsonArray names) throws JsonException {
		if(names == null || names.length() == 0 || length() == 0) {
			return null;
		}
		JsonObject jo = new JsonObject();
		for(int i = 0; i < names.length(); i += 1) {
			jo.put(names.getString(i), this.opt(i));
		}
		return jo;
	}

	/**
	 * Make a JSON text of this JSONArray. For compactness, no unnecessary
	 * whitespace is added. If it is not possible to produce a syntactically
	 * correct JSON text then null will be returned instead. This could occur if
	 * the array contains an invalid number.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return a printable, displayable, transmittable representation of the
	 *         array.
	 */
	@Override
	public String toString() {
		try {
			return '[' + join(",") + ']';
		} catch(Exception e) {
			return null;
		}
	}

	/**
	 * Make a prettyprinted JSON text of this JSONArray. Warning: This method
	 * assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @return a printable, displayable, transmittable representation of the
	 *         object, beginning with <code>[</code>&nbsp;<small>(left
	 *         bracket)</small> and ending with <code>]</code>
	 *         &nbsp;<small>(right bracket)</small>.
	 * @throws JsonException
	 */
	public String toString(int indentFactor) throws JsonException {
		return toString(indentFactor, 0);
	}

	/**
	 * Make a prettyprinted JSON text of this JSONArray. Warning: This method
	 * assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @param indent
	 *            The indention of the top level.
	 * @return a printable, displayable, transmittable representation of the
	 *         array.
	 * @throws JsonException
	 */
	String toString(int indentFactor, int indent) throws JsonException {
		int len = length();
		if(len == 0) {
			return "[]";
		}
		int i;
		StringBuilder sb = new StringBuilder(1024).append("[");
		if(len == 1) {
			sb.append(JsonObject.valueToString(super.get(0), indentFactor, indent));
		} else {
			int newindent = indent + indentFactor;
			sb.append('\n');
			for(i = 0; i < len; i += 1) {
				if(i > 0) {
					sb.append(",\n");
				}
				for(int j = 0; j < newindent; j += 1) {
					sb.append(' ');
				}
				sb.append(JsonObject.valueToString(super.get(i), indentFactor, newindent));
			}
			sb.append('\n');
			for(i = 0; i < indent; i += 1) {
				sb.append(' ');
			}
		}
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Write the contents of the JSONArray as JSON text to a writer. For
	 * compactness, no whitespace is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return The writer.
	 * @throws JsonException
	 */
	public Writer write(Writer writer) throws JsonException {
		try {
			boolean b = false;
			int len = length();

			writer.write('[');

			for(int i = 0; i < len; i += 1) {
				if(b) {
					writer.write(',');
				}
				Object v = super.get(i);
				if(v instanceof JsonObject) {
					((JsonObject)v).write(writer);
				} else if(v instanceof JsonArray) {
					((JsonArray)v).write(writer);
				} else {
					writer.write(JsonObject.valueToString(v));
				}
				b = true;
			}
			writer.write(']');
			return writer;
		} catch(IOException e) {
			throw new JsonException(e);
		}
	}

}
