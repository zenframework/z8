package org.zenframework.z8.server.base.json.parser;

import java.io.StringWriter;

import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

public class JsonUtil {

	static public String prettyPrint(org.zenframework.z8.server.json.parser.JsonObject json, int indent) {
		Gson gson = new Gson();
		JsonElement je = new JsonParser().parse(json.toString());

		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(stringWriter);
		jsonWriter.setIndent(StringUtils.padLeft("", indent, ' '));
		jsonWriter.setLenient(true);

		gson.toJson(je, jsonWriter);
		IOUtils.closeQuietly(jsonWriter);

		return stringWriter.toString();
	}

	static public string z8_prettyPrint(JsonObject.CLASS<? extends JsonObject> json, integer indent) {
		return new string(prettyPrint(json.get().get(), indent.getInt()));
	}
}
