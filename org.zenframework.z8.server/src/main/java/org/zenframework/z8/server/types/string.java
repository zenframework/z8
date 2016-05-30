package org.zenframework.z8.server.types;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.sql.sql_string;

public final class string extends primary {
    private String m_value = "";

    private Pattern pattern = null;
    private Matcher matcher = null;

    public string() {
    }

    public string(string str) {
        set(str);
    }

    public string(String str) {
        set(str);
    }

    public string(byte[] str) {
        this(str, encoding.UTF8);
    }

    public string(byte[] str, encoding charset) {
        try {
            if (str != null) {
                set(new String(str, charset.toString()));
            }
        } catch (UnsupportedEncodingException e) {
            throw new exception(e);
        }
    }

    public string(char ch) {
        set(((Character) ch).toString());
    }

    @Override
    public string defaultValue() {
        return new string();
    }

    public String get() {
        return m_value;
    }

    public byte[] getBytes(encoding charset) {
        try {
            return m_value.getBytes(charset.toString());
        } catch (UnsupportedEncodingException e) {
            throw new exception(e);
        }
    }

    public byte[] getBytes() {
        return getBytes(encoding.UTF8);
    }

    public void set(string str) {
        set(str != null ? str.m_value : "");
    }

    public void set(String str) {
        m_value = (str != null ? fromResources(str) : "");
    }

    public void concat(string s) {
        concat(s.m_value);
    }

    public void concat(String s) {
        m_value += fromResources(s);
    }

    @Override
    public String toString() {
        return m_value;
    }

    @Override
    public FieldType type() {
        return FieldType.String;
    }

    @Override
    public String format() {
        return '"' + m_value + "'";
    }

    @Override
    public String toDbConstant(DatabaseVendor vendor) {
        String string = m_value.replaceAll("'", "''");

        if (vendor == DatabaseVendor.SqlServer) {
            return "N'" + string + "'";
        }

        return "'" + string + "'";
    }

    public boolean isEmpty() {
        return m_value == null || m_value.isEmpty();
    }

    @Override
    public int hashCode() {
        return m_value.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (m_value != null && m_value.equals(object)) {
            return true;
        }

        if (object instanceof string) {
            string string = (string) object;

            if (m_value == null || m_value.isEmpty()) {
                return string.m_value == null || string.m_value.isEmpty();
            }

            return m_value != null ? m_value.equals(string.m_value) : false;
        }

        return false;
    }

    public String trim() {
        return m_value.trim();
    }

    public String trimLeft() {
        int pos = 0;

        while (pos < m_value.length() && m_value.charAt(pos) <= ' ') {
            pos++;
        }
        return pos > 0 ? m_value.substring(pos) : m_value;
    }

    public String trimRight() {
        int length = m_value.length();

        while (0 < length && m_value.charAt(length - 1) <= ' ') {
            length--;
        }
        return length < m_value.length() ? m_value.substring(0, length) : m_value;
    }

    public exception exception() {
        return new exception(m_value);
    }

    public sql_string sql_string() {
        return new sql_string(this);
    }

    public void operatorAssign(string x) {
        set(x == null ? "" : x.m_value);
    }

    public string operatorAdd(string x) {
        return x == null ? new string(m_value) : new string(m_value + x.m_value);
    }

    public string operatorAddAssign(string x) {
        if (x != null)
            set(m_value + x.m_value);
        return this;
    }

    public bool operatorEqu(string x) {
        return new bool(m_value.compareTo(x == null ? "" : x.m_value) == 0);
    }

    public bool operatorNotEqu(string x) {
        return new bool(m_value.compareTo(x == null ? "" : x.m_value) != 0);
    }

    public bool operatorLess(string x) {
        return new bool(m_value.compareTo(x == null ? "" : x.m_value) < 0);
    }

    public bool operatorMore(string x) {
        return new bool(m_value.compareTo(x == null ? "" : x.m_value) > 0);
    }

    public bool operatorLessEqu(string x) {
        return new bool(m_value.compareTo(x == null ? "" : x.m_value) <= 0);
    }

    public bool operatorMoreEqu(string x) {
        return new bool(m_value.compareTo(x == null ? "" : x.m_value) >= 0);
    }

    public integer z8_length() {
        return new integer(m_value.length());
    }

    public bool z8_isEmpty() {
        return new bool(m_value.length() == 0);
    }

    public integer z8_compare(string anotherString) {
        return new integer(m_value.compareTo(anotherString.m_value));
    }

    public integer z8_compareNoCase(string anotherString) {
        return new integer(m_value.compareToIgnoreCase(anotherString.m_value));
    }

    public integer z8_indexOf(string subString) {
        return new integer(m_value.indexOf(subString.m_value));
    }

    public integer z8_indexOf(string subString, integer offset) {
        return new integer(m_value.indexOf(subString.m_value, offset.getInt()));
    }

    public integer z8_lastIndexOf(string subString) {
        return new integer(m_value.lastIndexOf(subString.m_value));
    }

    public integer z8_lastIndexOf(string subString, integer offset) {
        return new integer(m_value.lastIndexOf(subString.m_value, offset.getInt()));
    }

    public bool z8_startsWith(string prefix) {
        return new bool(m_value.startsWith(prefix.m_value));
    }

    public bool z8_startsWith(string prefix, integer offset) {
        return new bool(m_value.startsWith(prefix.m_value, offset.getInt()));
    }

    public bool z8_endsWith(string suffix) {
        return new bool(m_value.endsWith(suffix.m_value));
    }

    public string z8_charAt(integer index) {
        try {
            return new string(m_value.charAt(index.getInt()));
        } catch (IndexOutOfBoundsException e) {
            throw new exception(e);
        }
    }

    public string z8_substring(integer index) {
        try {
            return new string(m_value.substring(index.getInt()));
        } catch (IndexOutOfBoundsException e) {
            throw new exception(e);
        }
    }

    public string z8_substring(integer index, integer count) {
        try {
            int end = (index.getInt() + count.getInt());
            if (end >= m_value.length())
                end = m_value.length();
            return new string(m_value.substring(index.getInt(), end));
        } catch (IndexOutOfBoundsException e) {
            throw new exception(e);
        }
    }

    public string z8_left(integer count) {
        try {
            return z8_substring(new integer(0), count);
        } catch (IndexOutOfBoundsException e) {
            throw new exception(e);
        }
    }

    public string z8_right(integer count) {
        try {
            int start = m_value.length() - count.getInt();
            if (start < 0)
                start = 0;
            return z8_substring(new integer(start));
        } catch (IndexOutOfBoundsException e) {
            throw new exception(e);
        }
    }

    public string z8_trim() {
        try {
            return new string(m_value.trim());
        } catch (IndexOutOfBoundsException e) {
            throw new exception(e);
        }
    }

    public string z8_trimLeft() {
        try {
            return new string(trimLeft());
        } catch (IndexOutOfBoundsException e) {
            throw new exception(e);
        }
    }

    public string z8_trimRight() {
        try {
            return new string(trimRight());
        } catch (IndexOutOfBoundsException e) {
            throw new exception(e);
        }
    }

    public string z8_padLeft(integer length) {
        return z8_padLeft(length, new string(" "));
    }

    public string z8_padLeft(integer length, string padding) {
        try {
            int len = length.getInt();

            if (m_value.length() >= len) {
                return new string(m_value.substring(0, len));
            }

            String s = "";

            for (int i = 0; i < len - m_value.length(); i++) {
                s += padding.m_value;
            }

            s = s.substring(0, len - m_value.length());

            return new string(s + m_value);
        } catch (IndexOutOfBoundsException e) {
            throw new exception(e);
        }
    }

    public string z8_padRight(integer length) {
        return z8_padRight(length, new string(" "));
    }

    public string z8_padRight(integer length, string padding) {
        try {
            int len = length.getInt();

            if (m_value.length() >= len) {
                return new string(m_value.substring(m_value.length() - len, m_value.length()));
            }

            String s = m_value;

            while (s.length() < len) {
                s += padding.m_value;
            }
            s = s.substring(0, len);

            return new string(s);
        } catch (IndexOutOfBoundsException e) {
            throw new exception(e);
        }
    }

    public string z8_insert(integer index, string what) {
        try {
            int i = index.getInt();
            return new string(m_value.substring(0, i) + what.m_value + m_value.substring(i));
        } catch (IndexOutOfBoundsException e) {
            throw new exception(e);
        }
    }

    public string z8_replace(integer index, integer length, string replacement) {
        try {
            int i = index.getInt();
            return new string(m_value.substring(0, i) + replacement.m_value + m_value.substring(i + length.getInt()));
        } catch (IndexOutOfBoundsException e) {
            throw new exception(e);
        }
    }

    public bool z8_matches(string regex) {
        try {
            return new bool(m_value.matches(regex.m_value));
        } catch (PatternSyntaxException e) {
            throw new exception(e);
        }
    }

    public void z8_setupMatcher(string regex) {
        try {
            if (regex != null) {
                pattern = Pattern.compile(regex.m_value);
                matcher = pattern.matcher(m_value);
            }
            else {
                pattern = null;
                matcher = null;
            }
        } catch (PatternSyntaxException e) {
            throw new exception(e);
        }
    }

    public bool z8_next() {
        try {
            if (matcher == null)
                return new bool(false);

            return new bool(matcher.find());
        } catch (PatternSyntaxException e) {
            throw new exception(e);
        }
    }

    public string z8_getGroup(integer groupNumber) {
        try {
            if (matcher == null)
                throw new exception("Matcher is null");

            if (matcher.group(groupNumber.getInt()) != null)
                return new string(matcher.group(groupNumber.getInt()));

            throw new exception("No such group");
        } catch (PatternSyntaxException e) {
            throw new exception(e);
        }
    }

    public string z8_replaceFirst(string regex, string replacement) {
        try {
            return new string(m_value.replaceFirst(regex.m_value, replacement.m_value));
        } catch (PatternSyntaxException e) {
            throw new exception(e);
        }
    }

    public string z8_replaceAll(string regex, string replacement) {
        try {
            return new string(m_value.replaceAll(regex.m_value, replacement.m_value));
        } catch (PatternSyntaxException e) {
            throw new exception(e);
        }
    }

    public RCollection<string> z8_split(string regex) {
        return z8_split(regex, new integer(0));
    }

    public RCollection<string> z8_split(string regex, integer limit) {
        try {
            RCollection<string> result = new RCollection<string>();

            String[] parts = m_value.split(regex.m_value, limit.getInt());

            for (String s : parts) {
                result.add(new string(s));
            }
            return result;
        } catch (PatternSyntaxException e) {
            throw new exception(e);
        }
    }

    public string z8_toLowerCase() {
        return new string(m_value.toLowerCase());
    }

    public string z8_toUpperCase() {
        return new string(m_value.toUpperCase());
    }

    public string z8_format(RCollection<string> _frm) {
        MessageFormat form = new MessageFormat(m_value);
        return new string(form.format(_frm.toArray()));
    }

    static public string z8_replicate(string str, integer count) {
        String s = "";
        for (int i = 0; i < count.getInt(); i++) {
            s += str.m_value;
        }
        return new string(s);
    }

    public static string[] convertArray(String... strings) {
        string[] result = new string[strings.length];
        for (int i = 0; i < strings.length; i++) {
            result[i] = new string(strings[i]);
        }
        return result;
    }

    public static String[] convertArray(string... strings) {
        String[] result = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            result[i] = strings[i].get();
        }
        return result;
    }

    private String fromResources(String str) {
        if (str.startsWith("$") && str.endsWith("$")) {
            return Resources.get(str.substring(1, str.length() - 1));
        }
        return str;
    }
}
