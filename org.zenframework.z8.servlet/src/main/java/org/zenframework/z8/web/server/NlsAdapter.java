package org.zenframework.z8.web.server;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.server.base.json.Json;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.file;

public class NlsAdapter extends Adapter {
	static private final String NlsPath = "/nls.json";

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().endsWith(NlsPath);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters, List<file> files, ISession session) throws IOException {
		JsonArray languages = new JsonArray(parameters.get("languages"));
		String language = null;
		Properties clientBundle = null;

		for (int i = 0; i < languages.size(); i++) {
			language = languages.getString(i);
			clientBundle = Resources.getCliendBundle(language);
			if (clientBundle != null)
				break;
		}

		if (clientBundle == null) {
			language = ServerConfig.language();
			clientBundle = Resources.getCliendBundle(language);
		}

		JsonObject data = new JsonObject(clientBundle);

		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, true);
		writer.writeInfo(Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
		writer.writeProperty("language", language);
		writer.writeProperty(Json.data, data);
		writer.finishResponse();

		writeResponse(response, writer.toString());
	}
}
