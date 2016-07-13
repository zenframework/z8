package org.zenframework.z8.server.base.table.system.view;

import java.util.Collection;

import org.zenframework.z8.server.base.json.Json;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IServerInfo;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class AuthorityCenterView extends Query {
	static private String className = AuthorityCenterView.class.getCanonicalName();
	
	static public class strings {
		static public String Title = "AuthorityCenterView.title";
		static public String Id = "AuthorityCenterView.id";
		static public String Host = "AuthorityCenterView.host";
		static public String Port = "AuthorityCenterView.port";
		static public String Active = "AuthorityCenterView.active";
	}

	static public class displayNames {
		static public String Title = Resources.get(strings.Title);
		static public String Id = Resources.get(strings.Id);
		static public String Host = Resources.get(strings.Host);
		static public String Port = Resources.get(strings.Port);
		static public String Active = Resources.get(strings.Active);
	}

	public static class CLASS<T extends AuthorityCenterView> extends Query.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(AuthorityCenterView.class);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new AuthorityCenterView(container);
		}
	}

	private StringField.CLASS<StringField> id = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> host = new StringField.CLASS<StringField>(this);
	private IntegerField.CLASS<IntegerField> port = new IntegerField.CLASS<IntegerField>(this);
	private BoolField.CLASS<BoolField> active = new BoolField.CLASS<BoolField>(this);
	
	public AuthorityCenterView(IObject container) {
		super(container);
	}
	
	@Override
	public void constructor2() {
		super.constructor2();
		
		readOnly = new bool(true);
		
		id.setIndex("id");
		id.setDisplayName(displayNames.Id);
		id.get().width = new integer(40);
		id.get().stretch = new bool(false);
		
		host.setIndex("host");
		host.setDisplayName(displayNames.Host);

		port.setIndex("port");
		port.setDisplayName(displayNames.Port);

		active.setIndex("active");
		active.setDisplayName(displayNames.Active);

		registerDataField(id);
		registerDataField(host);
		registerDataField(port);
		registerDataField(active);
		
		registerFormField(id);
		registerFormField(host);
		registerFormField(port);
		registerFormField(active);
	}

	@Override
	public JsonArray response() {
		return new JsonArray();
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		writer.writeProperty(Json.isQuery, true);
		writer.writeProperty(Json.requestId, className);

		Collection<Field> fields = getFormFields();
		
		writeMeta(writer, fields);
			
		writer.startObject(Json.section);
		
		writer.writeProperty(Json.isSection, true);

		writer.startArray(Json.controls);

		for(Field field : fields) {
			writer.startObject();
			writer.writeProperty(Json.id, field.id());
			writer.finishObject();
		}
		
		writer.finishArray();

		writer.finishObject();

		writer.startArray(Json.data);
		
		for(IServerInfo server : ServerConfig.authorityCenter().servers()) {
			writer.startObject();
			writer.writeProperty(id.id(), server.getId());
			writer.writeProperty(host.id(), server.getServer().toString());
			writer.writeProperty(active.id(), server.isAlive());
			writer.finishObject();
		}
		
		writer.finishArray();
	}
}
