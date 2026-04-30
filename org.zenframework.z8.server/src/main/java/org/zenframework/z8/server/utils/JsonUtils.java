package org.zenframework.z8.server.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;

public class JsonUtils {

	public static final String CSV_NL = "\n";
	public static final String CSV_SEP = ";";

	public static String toCsv(JsonArray data, String... fields) {
		if (data.isEmpty() || fields.length == 0)
			return "";

		StringBuilder str = new StringBuilder(1024);

		for (String field : fields)
			str.append('"').append(field.replace("\"", "\"\"")).append('"').append(CSV_SEP);
		str.setLength(str.length() - CSV_SEP.length());

		for (int i = 0; i < data.size(); i++) {
			str.append(CSV_NL);
			JsonObject record = data.getJsonObject(i);
			for (String field : fields) {
				Object value = record.get(field);
				str.append('"').append(value != null ? value.toString().replace("\"", "\"\"") : "").append('"').append(CSV_SEP);
			}
			str.setLength(str.length() - CSV_SEP.length());
		}

		return str.toString();
	}

	public static JsonArray fromCsv(String csv) throws IOException {
		return fromCsv(new StringReader(csv));
	}

	public static JsonArray fromCsv(Reader reader) throws IOException {
		JsonArray data = new JsonArray();
		BufferedReader csv = new BufferedReader(reader);
		String line = csv.readLine();

		if (line == null)
			throw new RuntimeException("CSV is empty");

		String[] fields = line.split(CSV_SEP);

		for (line = csv.readLine(); line != null; line = csv.readLine()) {
			String[] values = line.split(CSV_SEP);

			if (values.length != fields.length)
				throw new RuntimeException("CSV incorrect structure");

			JsonObject json = new JsonObject();
			for (int i = 0; i < fields.length; i++)
				json.put(fields[i], values[i]);

			data.add(json);
		}

		return data;
	}
}
