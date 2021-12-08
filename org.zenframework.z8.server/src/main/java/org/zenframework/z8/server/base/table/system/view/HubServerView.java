package org.zenframework.z8.server.base.table.system.view;

import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.engine.ServerInfo;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.utils.ProxyUtils;

abstract public class HubServerView extends Query {
	static public class strings {
		static public String Host = "HubServerView.host";
		static public String Port = "HubServerView.port";
		static public String Active = "HubServerView.active";
		static public String ServerId = "HubServerView.serverId";
		static public String Domains = "HubServerView.domains";

		static public String Unregister = "HubServerView.unregister";
	}

	static public class displayNames {
		static public String Host = Resources.get(strings.Host);
		static public String Port = Resources.get(strings.Port);
		static public String Active = Resources.get(strings.Active);
		static public String ServerId = Resources.get(strings.ServerId);
		static public String Domains = Resources.get(strings.Domains);

		static public String Unregister = Resources.get(strings.Unregister);
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

	private StringField.CLASS<StringField> recordId = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> host = new StringField.CLASS<StringField>(this);
	private IntegerField.CLASS<IntegerField> port = new IntegerField.CLASS<IntegerField>(this);
	private BoolField.CLASS<BoolField> active = new BoolField.CLASS<BoolField>(this);
	private StringField.CLASS<StringField> domains = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> serverId = new StringField.CLASS<StringField>(this);

	private Action.CLASS<Action> unregister = new Action.CLASS<Action>(this);

	public HubServerView(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(host);
		objects.add(port);
		objects.add(active);
		objects.add(serverId);
		objects.add(domains);
		objects.add(unregister);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		readOnly = bool.True;

		recordId.setIndex("recordId");

		host.setIndex("host");
		host.setDisplayName(displayNames.Host);
		host.get().width = new integer(100);

		port.setIndex("port");
		port.setDisplayName(displayNames.Port);
		port.get().width = new integer(60);

		serverId.setIndex("serverId");
		serverId.setDisplayName(displayNames.ServerId);
		serverId.get().width = new integer(150);

		active.setIndex("active");
		active.setDisplayName(displayNames.Active);

		domains.setIndex("domains");
		domains.setDisplayName(displayNames.Domains);
		domains.get().colSpan = new integer(4);

		registerControl(host);
		registerControl(port);
		registerControl(active);
		registerControl(serverId);
		registerControl(domains);

		unregister.setDisplayName(displayNames.Unregister);
/*
		actions.add(unregister);
*/
		names.add(host);
		names.add(port);
		names.add(active);
	}

	@Override
	public JsonArray getData() {
		JsonArray data = new JsonArray();

		try {
			for(ServerInfo server : getServers()) {
				JsonObject object = new JsonObject();
				object.put(recordId.id(), getUrl(server));
				object.put(serverId.id(), server.getId());
				object.put(host.id(), getHost(server));
				object.put(port.id(), getPort(server));
				object.put(domains.id(), getDomains(server));
				object.put(active.id(), server.isAlive());
				data.add(object);
			}

			return data;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	abstract protected ServerInfo[] getServers() throws Throwable;
	abstract protected void unregister(ServerInfo server) throws Throwable;

	private String getDomains(ServerInfo server) {
		String[] domains = server.getDomains();

		if(domains == null)
			return "";

		String result = Arrays.toString(server.getDomains());
		return result.substring(1, result.length() - 1);
	}

	private String getHost(ServerInfo server) {
		Proxy proxy = server.getProxy();
		return proxy != null ? ProxyUtils.getHost(proxy) : Rmi.localhost;
	}

	private int getPort(ServerInfo server) {
		Proxy proxy = server.getProxy();
		return proxy != null ? ProxyUtils.getPort(proxy) : 0;
	}

	private String getUrl(ServerInfo server) {
		return getHost(server) + ":" + getPort(server);
	}
}
