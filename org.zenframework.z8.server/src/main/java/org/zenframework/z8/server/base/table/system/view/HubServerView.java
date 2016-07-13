package org.zenframework.z8.server.base.table.system.view;

import java.util.Arrays;
import java.util.Collection;

import org.zenframework.z8.server.base.json.Json;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.engine.IServerInfo;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

abstract public class HubServerView extends Query {
	static public class strings {
		static public String Host = "HubServerView.host";
		static public String Port = "HubServerView.port";
		static public String Id = "HubServerView.id";
		static public String Active = "HubServerView.active";
		static public String ServerId = "HubServerView.id";
		static public String Domains = "HubServerView.domains";
	}

	static public class displayNames {
		static public String Host = Resources.get(strings.Host);
		static public String Port = Resources.get(strings.Port);
		static public String Id = Resources.get(strings.Id);
		static public String Active = Resources.get(strings.Active);
		static public String ServerId = Resources.get(strings.ServerId);
		static public String Domains = Resources.get(strings.Domains);
	}

	public static class CLASS<T extends HubServerView> extends Query.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(HubServerView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return null;
		}
	}

	private StringField.CLASS<StringField> host = new StringField.CLASS<StringField>(this);
	private IntegerField.CLASS<IntegerField> port = new IntegerField.CLASS<IntegerField>(this);
	private IntegerField.CLASS<IntegerField> id = new IntegerField.CLASS<IntegerField>(this);
	private BoolField.CLASS<BoolField> active = new BoolField.CLASS<BoolField>(this);
	private StringField.CLASS<StringField> domains = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> serverId = new StringField.CLASS<StringField>(this);
	
	public HubServerView(IObject container) {
		super(container);
	}
	
	@Override
	public void constructor2() {
		super.constructor2();
		
		readOnly = new bool(true);
		
		host.setIndex("host");
		host.setDisplayName(displayNames.Host);
		host.get().width = new integer(25);
		host.get().stretch = new bool(false);

		port.setIndex("port");
		port.setDisplayName(displayNames.Port);

		id.setIndex("id");
		id.setDisplayName(displayNames.Id);

		active.setIndex("active");
		active.setDisplayName(displayNames.Active);

		domains.setIndex("domains");
		domains.setDisplayName(displayNames.Domains);

		serverId.setIndex("serverId");
		serverId.setDisplayName(displayNames.ServerId);
		serverId.get().width = new integer(40);
		serverId.get().stretch = new bool(false);
		
		registerFormField(host);
		registerFormField(port);
		registerFormField(id);
		registerFormField(active);
		registerFormField(serverId);
		registerFormField(domains);
	}

	@Override
	public JsonArray response() {
		return new JsonArray();
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		String action = getParameter(Json.action);
		
		if(action == null) {
			writer.writeProperty(Json.isQuery, true);
			writer.writeProperty(Json.requestId, classId());
	
			Collection<Field> fields = getFormFields();
			
			writeMeta(writer, fields);
			
			writer.writeProperty(Json.remoteSort, false);
		}
		
		writeData(writer);
	}

	abstract protected IServerInfo[] getServers() throws Throwable;
	
	private void writeData(JsonWriter writer) throws Throwable {
		writer.startArray(Json.data);
	
		for(IServerInfo server : getServers()) {
			String url = parseUrl(server);
			
			writer.startObject();
			writer.writeProperty(serverId.id(), server.getId());
			writer.writeProperty(host.id(), url.isEmpty() ? "" : url.substring(0, url.indexOf(":")));
			writer.writeProperty(port.id(), url.isEmpty() ? "" : url.substring(url.indexOf(":") +  1));
			writer.writeProperty(id.id(), parseId(server));
			writer.writeProperty(domains.id(), server.getDomains() != null ? Arrays.toString(server.getDomains()) : "");
			writer.writeProperty(active.id(), server.isAlive());
			writer.finishObject();
		}
		
		writer.finishArray();
	}

	static private String endpoint = "endpoint:[";
	static private String objID = "objID:[";
	
	private String parseUrl(IServerInfo server) {
		return parse(server, endpoint);
	}

	private String parseId(IServerInfo server) {
		return parse(server, objID);
	}

	private String parse(IServerInfo server, String pattern) {
		String url = server.toString();
		
		int index = url.indexOf(pattern);
		
		if(index == -1)
			return "";
		
		int start = index + pattern.length();
		
		return url.substring(start, url.indexOf("]", start));
	}
}
