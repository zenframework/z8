package org.zenframework.z8.server.base.table.system.view;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IServerInfo;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
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
		static public String WebAppUrl = "HubServerView.webAppUrl";

		static public String Unregister = "HubServerView.unregister";
	}

	static public class displayNames {
		static public String Host = Resources.get(strings.Host);
		static public String Port = Resources.get(strings.Port);
		static public String Active = Resources.get(strings.Active);
		static public String ServerId = Resources.get(strings.ServerId);
		static public String Domains = Resources.get(strings.Domains);
		static public String WebAppUrl = Resources.get(strings.WebAppUrl);

		static public String Unregister = Resources.get(strings.Unregister);
	}

	public static class CLASS<T extends HubServerView> extends Query.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(HubServerView.class);
			setAttribute("ui", "Z8.application.system.HubServerView");
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
	private StringField.CLASS<StringField> webAppUrl = new StringField.CLASS<StringField>(this);

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
		objects.add(webAppUrl);
		objects.add(domains);
		objects.add(unregister);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		readOnly = bool.True;
		colCount = new integer(6);

		recordId.setIndex("recordId");

		host.setIndex("host");
		host.setDisplayName(displayNames.Host);
		host.get().width = new integer(100);

		port.setIndex("port");
		port.setDisplayName(displayNames.Port);
		port.get().width = new integer(60);

		webAppUrl.setIndex("webAppUrl");
		webAppUrl.setDisplayName(displayNames.WebAppUrl);
		webAppUrl.get().width = new integer(150);

		serverId.setIndex("serverId");
		serverId.setDisplayName(displayNames.ServerId);
		serverId.get().width = new integer(150);
		serverId.get().colSpan = new integer(2);

		active.setIndex("active");
		active.setDisplayName(displayNames.Active);

		domains.setIndex("domains");
		domains.setDisplayName(displayNames.Domains);
		domains.get().colSpan = new integer(6);

		registerControl(host);
		registerControl(port);
		registerControl(active);
		registerControl(webAppUrl);
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

		JsonArray qf = parseQuickFilter();

		try {
			for(IServerInfo server : getServers()) {
				JsonObject object = new JsonObject();
				object.put(recordId.id(), getUrl(server));
				object.put(serverId.id(), server.getId());
				object.put(webAppUrl.id(), server.getWebAppUrl());
				object.put(host.id(), getHost(server));
				object.put(port.id(), getPort(server));
				object.put(domains.id(), getDomains(server));
				object.put(active.id(), server.isAlive());

				if (filtered(qf, object))
					data.add(object);
			}

			doRequestSort(data);

			return data;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private JsonArray parseQuickFilter() {
		String qf = ApplicationServer.getRequest().getParameter(new string("quickFilter"));
		if (qf == null || qf.isEmpty())
			return new JsonArray();
		return new JsonArray(qf);
	}

	private boolean filtered(JsonArray filts, JsonObject obj) {
		for (int i = 0; i < filts.size(); ++i) {
			JsonObject f = filts.getJsonObject(i);
			if (!obj.getString(f.getString("property")).toLowerCase().contains(f.getString("value").toLowerCase()))
				return false;
		}
		return true;
	}

	private void doRequestSort(JsonArray data) {
		String sort = ApplicationServer.getRequest().getParameter(new string("sort"));
		if (sort == null || sort.isEmpty())
			return;
		JsonArray sortArr = new JsonArray(sort);
		if (sortArr.length() == 0)
			return;
		JsonObject obj = sortArr.getJsonObject(0);
		data.sort(new JsonComparator(obj.getString("property"), obj.getString("direction").equals("asc")));
	}

	private class JsonComparator implements Comparator<Object> {

		private String fieldName;
		private boolean ascender;

		public JsonComparator(String field, boolean asc) {
			fieldName = field;
			ascender = asc;
		}

		@Override
		public int compare(Object o1, Object o2) {
			JsonObject j1 = (JsonObject)o1;
			JsonObject j2 = (JsonObject)o2;
			if (ascender)
				return j1.getString(fieldName).compareTo(j2.getString(fieldName));
			else
				return j2.getString(fieldName).compareTo(j1.getString(fieldName));
		}
	}

	abstract protected IServerInfo[] getServers() throws Throwable;
	abstract protected void unregister(IServerInfo server) throws Throwable;

	private String getDomains(IServerInfo server) {
		String[] domains = server.getDomains();

		if(domains == null)
			return "";

		List<String> list = new ArrayList<>(Arrays.asList(domains));
		if (list.size() > 1 && list.get(0).startsWith("System at"))
			list.remove(0);
		return String.join(", ", list);
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
