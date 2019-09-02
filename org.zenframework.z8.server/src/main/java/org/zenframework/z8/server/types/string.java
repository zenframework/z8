package org.zenframework.z8.server.types;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.sql.sql_string;

public final class string extends primary {
	private static final long serialVersionUID = 8678133849134310611L;

	static public string Empty = new string();

	private String value;

	private Pattern pattern;
	private Matcher matcher;

	public string() {
	}

	public string(string str) {
		set(str);
	}

	public string(String str) {
		set(str);
	}

	public string(int number) {
		set(Integer.toString(number, 10));
	}

	public string(int number, int radix) {
		set(Integer.toString(number, radix));
	}

	public string(BigInteger number) {
		set(number.toString(10));
	}

	public string(BigInteger number, int radix) {
		set(number.toString(radix));
	}

	public string(byte[] str) {
		this(str, encoding.UTF8);
	}

	public string(byte[] str, encoding charset) {
		try {
			if(str != null) {
				set(new String(str, charset.toString()));
			}
		} catch(UnsupportedEncodingException e) {
			throw new exception(e);
		}
	}

	public string(char ch) {
		set(((Character)ch).toString());
	}

	public String get() {
		return value != null ? value : "";
	}

	public byte[] getBytes(encoding charset) {
		try {
			return value != null ? value.getBytes(charset.toString()) : null;
		} catch(UnsupportedEncodingException e) {
			throw new exception(e);
		}
	}

	public byte[] getBytes() {
		return getBytes(encoding.UTF8);
	}

	private void set(string str) {
		set(str != null ? str.value : null);
	}

	private void set(String str) {
		value = (str != null ? fromResources(str) : null);
	}

	@Override
	public String toString() {
		return get();
	}

	@Override
	public FieldType type() {
		return FieldType.String;
	}

	@Override
	public String toDbConstant(DatabaseVendor vendor) {
		String string = get().replaceAll("'", "''");

		if(vendor == DatabaseVendor.SqlServer || vendor == DatabaseVendor.Oracle)
			return "N'" + string + "'";

		return "'" + string + "'";
	}

	public boolean isEmpty() {
		return value == null || value.isEmpty();
	}

	@Override
	public int hashCode() {
		return get().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if(value != null && value.equals(object))
			return true;

		if(object instanceof string) {
			string string = (string)object;

			if(value == null || value.isEmpty())
				return string.value == null || string.value.isEmpty();

			return value.equals(string.value);
		}

		return false;
	}

	@Override
	public int compareTo(primary primary) {
		if(primary instanceof string) {
			string string = (string)primary;
			return value != null ? value.compareTo(string.value) : -1;
		}

		return -1;
	}

	public boolean equalsIgnoreCase(String string) {
		return value == string ? true : (value != null ? value.equalsIgnoreCase(string) : false);
	}

	public String trim() {
		return get().trim();
	}

	public String trimLeft() {
		int pos = 0;

		String value = get();

		while(pos < value.length() && value.charAt(pos) <= ' ')
			pos++;

		return pos > 0 ? value.substring(pos) : value;
	}

	public String trimRight() {
		String value = get();

		int length = value.length();

		while(0 < length && value.charAt(length - 1) <= ' ')
			length--;

		return length < value.length() ? value.substring(0, length) : value;
	}

	public binary binary() {
		return new binary(this);
	}

	public exception exception() {
		return new exception(this);
	}

	public sql_string sql_string() {
		return new sql_string(this);
	}

	public string operatorAdd(string x) {
		return x == null ? new string(get()) : new string(get() + x.get());
	}

	public bool operatorEqu(string x) {
		return new bool(get().compareTo(x == null ? "" : x.get()) == 0);
	}

	public bool operatorNotEqu(string x) {
		return new bool(get().compareTo(x == null ? "" : x.get()) != 0);
	}

	public bool operatorLess(string x) {
		return new bool(get().compareTo(x == null ? "" : x.get()) < 0);
	}

	public bool operatorMore(string x) {
		return new bool(get().compareTo(x == null ? "" : x.get()) > 0);
	}

	public bool operatorLessEqu(string x) {
		return new bool(get().compareTo(x == null ? "" : x.get()) <= 0);
	}

	public bool operatorMoreEqu(string x) {
		return new bool(get().compareTo(x == null ? "" : x.get()) >= 0);
	}

	public integer z8_length() {
		return new integer(get().length());
	}

	public bool z8_isEmpty() {
		return new bool(get().length() == 0);
	}

	public integer z8_compare(string anotherString) {
		return new integer(get().compareTo(anotherString.get()));
	}

	public integer z8_compareNoCase(string anotherString) {
		return new integer(get().compareToIgnoreCase(anotherString.get()));
	}

	public integer z8_indexOf(string subString) {
		return new integer(get().indexOf(subString.get()));
	}

	public integer z8_indexOf(string subString, integer offset) {
		return new integer(get().indexOf(subString.get(), offset.getInt()));
	}

	public integer z8_lastIndexOf(string subString) {
		return new integer(get().lastIndexOf(subString.get()));
	}

	public integer z8_lastIndexOf(string subString, integer offset) {
		return new integer(get().lastIndexOf(subString.get(), offset.getInt()));
	}

	public bool z8_startsWith(string prefix) {
		return new bool(get().startsWith(prefix.get()));
	}

	public bool z8_startsWith(string prefix, integer offset) {
		return new bool(get().startsWith(prefix.get(), offset.getInt()));
	}

	public bool z8_endsWith(string suffix) {
		return new bool(get().endsWith(suffix.get()));
	}

	public string z8_charAt(integer index) {
		try {
			return new string(get().charAt(index.getInt()));
		} catch(IndexOutOfBoundsException e) {
			throw new exception(e);
		}
	}

	public string z8_substring(integer index) {
		try {
			return new string(get().substring(index.getInt()));
		} catch(IndexOutOfBoundsException e) {
			throw new exception(e);
		}
	}

	public string z8_substring(integer index, integer count) {
		try {
			String value = get();

			int end = (index.getInt() + count.getInt());

			if(end >= value.length())
				end = value.length();

			return new string(value.substring(index.getInt(), end));
		} catch(IndexOutOfBoundsException e) {
			throw new exception(e);
		}
	}

	public string z8_left(integer count) {
		try {
			return z8_substring(new integer(0), count);
		} catch(IndexOutOfBoundsException e) {
			throw new exception(e);
		}
	}

	public string z8_right(integer count) {
		try {
			int start = get().length() - count.getInt();
			if(start < 0)
				start = 0;
			return z8_substring(new integer(start));
		} catch(IndexOutOfBoundsException e) {
			throw new exception(e);
		}
	}

	public string z8_trim() {
		try {
			return new string(get().trim());
		} catch(IndexOutOfBoundsException e) {
			throw new exception(e);
		}
	}

	public string z8_trimLeft() {
		try {
			return new string(trimLeft());
		} catch(IndexOutOfBoundsException e) {
			throw new exception(e);
		}
	}

	public string z8_trimRight() {
		try {
			return new string(trimRight());
		} catch(IndexOutOfBoundsException e) {
			throw new exception(e);
		}
	}

	public string padLeft(int length, String padding) {
		return new string(StringUtils.leftPad(get(), length, padding));
	}

	public string z8_padLeft(integer length) {
		return padLeft(length.getInt(), " ");
	}

	public string z8_padLeft(integer length, string padding) {
		return padLeft(length.getInt(), padding.get());
	}

	public string padRight(int length, String padding) {
		return new string(StringUtils.rightPad(get(), length, padding));
	}

	public string z8_padRight(integer length) {
		return padRight(length.getInt(), " ");
	}

	public string z8_padRight(integer length, string padding) {
		return padRight(length.getInt(), padding.get());
	}

	public string z8_insert(integer index, string what) {
		try {
			int i = index.getInt();
			String value = get();
			return new string(value.substring(0, i) + what.get() + value.substring(i));
		} catch(IndexOutOfBoundsException e) {
			throw new exception(e);
		}
	}

	public string z8_replace(integer index, integer length, string replacement) {
		try {
			int i = index.getInt();
			String value = get();
			return new string(value.substring(0, i) + replacement.get() + value.substring(i + length.getInt()));
		} catch(IndexOutOfBoundsException e) {
			throw new exception(e);
		}
	}

	public bool z8_matches(string regex) {
		try {
			return new bool(get().matches(regex.get()));
		} catch(PatternSyntaxException e) {
			throw new exception(e);
		}
	}

	public void z8_setupMatcher(string regex) {
		try {
			if(regex != null) {
				pattern = Pattern.compile(regex.get());
				matcher = pattern.matcher(get());
			} else {
				pattern = null;
				matcher = null;
			}
		} catch(PatternSyntaxException e) {
			throw new exception(e);
		}
	}

	public bool z8_next() {
		try {
			if(matcher == null)
				return bool.False;

			return new bool(matcher.find());
		} catch(PatternSyntaxException e) {
			throw new exception(e);
		}
	}

	public string z8_getGroup(integer groupNumber) {
		try {
			if(matcher == null)
				throw new exception("Matcher is null");

			if(matcher.group(groupNumber.getInt()) != null)
				return new string(matcher.group(groupNumber.getInt()));

			throw new exception("No such group");
		} catch(PatternSyntaxException e) {
			throw new exception(e);
		}
	}

	public string z8_replaceFirst(string regex, string replacement) {
		try {
			return new string(get().replaceFirst(regex.get(), replacement.get()));
		} catch(PatternSyntaxException e) {
			throw new exception(e);
		}
	}

	public string z8_replaceAll(string regex, string replacement) {
		try {
			return new string(get().replaceAll(regex.get(), replacement.get()));
		} catch(PatternSyntaxException e) {
			throw new exception(e);
		}
	}

	public RCollection<string> z8_split(string regex) {
		return z8_split(regex, new integer(0));
	}

	public RCollection<string> z8_split(string regex, integer limit) {
		try {
			RCollection<string> result = new RCollection<string>();

			String[] parts = get().split(regex.get(), limit.getInt());

			for(String s : parts)
				result.add(new string(s));

			return result;
		} catch(PatternSyntaxException e) {
			throw new exception(e);
		}
	}

	public string z8_toLowerCase() {
		return new string(get().toLowerCase());
	}

	public string z8_toUpperCase() {
		return new string(get().toUpperCase());
	}

	public string z8_format(RCollection<primary> format) {
		Object[] args = new Object[format.size()];
		for (int i = 0; i < args.length; i++)
			args[i] = format.get(i).getValue();
		MessageFormat form = new MessageFormat(get());
		return new string(form.format(args));
	}

	static public string z8_replicate(string str, integer count) {
		return new string(replicate(str.get(), count.getInt()));
	}

	static public string z8_concat(RCollection<string> array, string delimeter) {
		StringBuilder str = new StringBuilder();
		for (string s : array)
			str.append(s.get()).append(delimeter.get());
		if (str.length() > 0)
			str.setLength(str.length() - delimeter.get().length());
		return new string(str.toString());
	}

	public static string[] wrap(String... strings) {
		string[] result = new string[strings.length];

		for(int i = 0; i < strings.length; i++)
			result[i] = new string(strings[i]);

		return result;
	}

	public static String[] unwrap(string... strings) {
		String[] result = new String[strings.length];

		for(int i = 0; i < strings.length; i++)
			result[i] = strings[i].get();

		return result;
	}

	public static Collection<string> wrap(Collection<String> strings) {
		Collection<string> result = new ArrayList<string>(strings.size());

		for(String str : strings)
			result.add(new string(str));

		return result;
	}

	public static Collection<String> unwrap(Collection<string> strings) {
		Collection<String> result = new ArrayList<String>(strings.size());

		for(string str : strings)
			result.add(str.get());

		return result;
	}

	static public String replicate(String str, int count) {
		StringBuilder s = new StringBuilder(str.length() * count);
		for (int i = 0; i < count; i++)
			s.append(str);
		return s.toString();
	}

	private String fromResources(String str) {
		if(str.length() > 2 && str.charAt(0) == '$' && str.charAt(str.length() - 1) == '$')
			return Resources.get(str.substring(1, str.length() - 1));
		return str;
	}
}
