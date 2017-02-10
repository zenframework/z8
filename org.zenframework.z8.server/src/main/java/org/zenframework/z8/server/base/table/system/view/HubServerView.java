package org.zenframework.z8.server.base.table.system.view;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.model.actions.RequestAction;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.engine.IServerInfo;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
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
	private StringField.CLASS<StringField> active = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> domains = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> serverId = new StringField.CLASS<StringField>(this);

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
		active.get().width = new integer(40);

		domains.setIndex("domains");
		domains.setDisplayName(displayNames.Domains);
		domains.get().width = new integer(400);

		registerControl(host);
		registerControl(port);
		registerControl(active);
		registerControl(serverId);
		registerControl(domains);

		Action.CLASS<Action> commandCls = new Action.CLASS<Action>(this);
		Action command = commandCls.get();
		command.text = new string(displayNames.Unregister);
		actions.add(commandCls);
	}

	@Override
	public JsonArray response() {
		return new JsonArray();
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		String action = getParameter(Json.action);

		if(action == null || action.equals(RequestAction.readAction)) {
			writer.writeProperty(Json.isQuery, true);
			writer.writeProperty(Json.request, classId());
			writer.writeProperty(Json.primaryKey, recordId.get().id());

			setSelectFields(fields());

			writeMeta(writer, this);
			writeData(writer, action != null);
		} else if(action.equals(RequestAction.commandAction)) {
			Collection<String> urls = new ArrayList<String>();
			JsonArray records = new JsonArray(getParameter(Json.data));
			for(int i = 0; i < records.length(); i++)
				urls.add(records.getString(i));
			unregister(urls);
		}
	}

	abstract protected IServerInfo[] getServers() throws Throwable;
	abstract protected void unregister(IServerInfo server) throws Throwable;

	private void writeData(JsonWriter writer, boolean checkAlive) throws Throwable {
		writer.startArray(Json.data);

		for(IServerInfo server : getServers()) {
			writer.startObject();
			writer.writeProperty(recordId.id(), getUrl(server));
			writer.writeProperty(serverId.id(), server.getId());
			writer.writeProperty(host.id(), getHost(server));
			writer.writeProperty(port.id(), getPort(server));
			writer.writeProperty(domains.id(), getDomains(server));
			writer.writeProperty(active.id(), checkAlive ? (server.isAlive() ? bool.trueString : bool.falseString) : "");
			writer.finishObject();
		}

		writer.finishArray();
	}

	private void unregister(Collection<String> urls) throws Throwable {
		IServerInfo[] servers = getServers();

		for(String url : urls) {
			for(int i = 0; i < servers.length; i++) {
				IServerInfo server = servers[i];
				if(getUrl(server).equals(url)) {
					unregister(server);
					return;
				}
			}
		}
	}

	private String getDomains(IServerInfo server) {
		String[] domains = server.getDomains();

		if(domains == null)
			return "";

		String result = Arrays.toString(server.getDomains());
		return result.substring(1, result.length() - 1);
	}

	private String getHost(IServerInfo server) {
		Proxy proxy = server.getProxy();
		return proxy != null ? ProxyUtils.getHost(proxy) : Rmi.localhost;
	}

	private int getPort(IServerInfo server) {
		Proxy proxy = server.getProxy();
		return proxy != null ? ProxyUtils.getPort(proxy) : 0;
	}

	private String getUrl(IServerInfo server) {
		return getHost(server) + ":" + getPort(server);
	}
}
