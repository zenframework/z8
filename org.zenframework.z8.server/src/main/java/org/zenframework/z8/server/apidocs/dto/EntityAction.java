package org.zenframework.z8.server.apidocs.dto;

import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class EntityAction extends BaseInfo {
	private List<FieldDescription> parameters;

	public EntityAction(String name, String description) {
		super(name, description);
		parameters = new ArrayList<>();
	}

	public List<FieldDescription> getParameters() {
		return parameters;
	}

	public String getJson() {
		JsonArray parameters = new JsonArray();
		for(FieldDescription parameter : getParameters()) {
			JsonObject obj = new JsonObject();
			obj.put("id", parameter.name);
			obj.put("value", "");
			parameters.add(obj);
		}
		return parameters.toString();
	}
}
