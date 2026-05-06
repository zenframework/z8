package org.zenframework.z8.server.base.table.system.view;

import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.engine.IHubServer;
import org.zenframework.z8.server.engine.IServerInfo;
import org.zenframework.z8.server.engine.Rmi;
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
		static public String WebAppUrl = "HubServerView.webAppUrl";
		static public String DatabaseVersion = "HubServerView.databaseVersion";
		static public String RuntimeVersion = "HubServerView.runtimeVersion";
		static public String GitHash = "HubServerView.gitHash";
		static public String BuildTime = "HubServerView.buildTime";
		static public String Properties = "HubServerView.properties";

		static public String Unregister = "HubServerView.unregister";
	}

	static public class displayNames {
		static public String Host = Resources.get(strings.Host);
		static public String Port = Resources.get(strings.Port);
		static public String Active = Resources.get(strings.Active);
		static public String ServerId = Resources.get(strings.ServerId);
		static public String Domains = Resources.get(strings.Domains);
		static public String WebAppUrl = Resources.get(strings.WebAppUrl);
		static public String DatabaseVersion = Resources.get(strings.DatabaseVersion);
		static public String RuntimeVersion = Resources.get(strings.RuntimeVersion);
		static public String GitHash = Resources.get(strings.GitHash);
		static public String BuildTime = Resources.get(strings.BuildTime);
		static public String Properties = Resources.get(strings.Properties);

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
	private StringField.CLASS<StringField> databaseVersion = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> runtimeVersion = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> gitHash = new StringField.CLASS<StringField>(this);
	private DatetimeField.CLASS<DatetimeField> buildTime = new DatetimeField.CLASS<DatetimeField>(this);
	private TextField.CLASS<TextField> properties = new TextField.CLASS<TextField>(this);

//	private UnregisterServerAction.CLASS<UnregisterServerAction> unregister = new UnregisterServerAction.CLASS<UnregisterServerAction>(this);

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
		objects.add(databaseVersion);
		objects.add(runtimeVersion);
		objects.add(gitHash);
		objects.add(buildTime);
		objects.add(domains);
		objects.add(properties);
//		objects.add(unregister);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		readOnly = bool.True;
		colCount = new integer(4);

		recordId.setIndex("recordId");

		serverId.setIndex("serverId");
		serverId.setDisplayName(displayNames.ServerId);
		serverId.get().width = new integer(150);

		host.setIndex("host");
		host.setDisplayName(displayNames.Host);
		host.get().width = new integer(100);

		port.setIndex("port");
		port.setDisplayName(displayNames.Port);
		port.get().width = new integer(60);

		active.setIndex("active");
		active.setIcon("fa-heartbeat");
		active.setDisplayName(displayNames.Active);

		databaseVersion.setIndex("databaseVersion");
		databaseVersion.setDisplayName(displayNames.DatabaseVersion);
		databaseVersion.get().width = new integer(150);

		runtimeVersion.setIndex("runtimeVersion");
		runtimeVersion.setDisplayName(displayNames.RuntimeVersion);
		runtimeVersion.get().width = new integer(150);

		buildTime.setIndex("buildTime");
		buildTime.setDisplayName(displayNames.BuildTime);
		buildTime.get().width = new integer(150);

		gitHash.setIndex("gitHash");
		gitHash.setDisplayName(displayNames.GitHash);
		gitHash.get().width = new integer(150);

		webAppUrl.setIndex("webAppUrl");
		webAppUrl.setDisplayName(displayNames.WebAppUrl);
		webAppUrl.get().width = new integer(150);
		webAppUrl.get().colSpan = new integer(4);

		domains.setIndex("domains");
		domains.setDisplayName(displayNames.Domains);
		domains.get().colSpan = new integer(4);

		properties.setIndex("properties");
		properties.setDisplayName(displayNames.Properties);
		properties.get().colSpan = new integer(4);

//		unregister.setIndex("unregister");
//		unregister.setDisplayName(displayNames.Unregister);

		// 1st row
		registerControl(serverId);
		registerControl(host);
		registerControl(port);
		registerControl(active);

		// 2nd row
		registerControl(databaseVersion);
		registerControl(runtimeVersion);
		registerControl(gitHash);
		registerControl(buildTime);

		// Other rows
		registerControl(webAppUrl);
		registerControl(domains);
		registerControl(properties);

//		actions.add(unregister);

		names.add(host);
		names.add(port);
		names.add(active);
	}

	@Override
	public JsonArray getData() {
		JsonArray data = new JsonArray();

		try {
			for(IServerInfo server : getHubServer().servers()) {
				JsonObject object = new JsonObject();
				object.put(recordId.id(), getUrl(server));
				object.put(serverId.id(), server.getId());
				object.put(host.id(), getHost(server));
				object.put(port.id(), getPort(server));
				object.put(active.id(), server.isAlive());
				object.put(databaseVersion.id(), server.getDatabaseVersion());
				object.put(runtimeVersion.id(), server.getRuntimeVersion());
				object.put(gitHash.id(), server.getGitHash());
				object.put(buildTime.id(), server.getBuildTimestamp());
				object.put(webAppUrl.id(), server.getWebAppUrl());
				object.put(domains.id(), getDomains(server));
				object.put(properties.id(), server.getProperties().toString());
				data.add(object);
			}

			return data;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	abstract public IHubServer getHubServer();

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
