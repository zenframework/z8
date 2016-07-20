package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IServerInfo;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;

public class InterconnectionCenterView extends HubServerView {
	static public class strings {
		static public String Title = "InterconnectionCenterView.title";
	}

	static public class displayNames {
		static public String Title = Resources.get(strings.Title);
	}

	public static class CLASS<T extends InterconnectionCenterView> extends HubServerView.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(InterconnectionCenterView.class);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new InterconnectionCenterView(container);
		}
	}

	public InterconnectionCenterView(IObject container) {
		super(container);
	}
	
	@Override
	protected IServerInfo[] getServers() throws Throwable {
		return ServerConfig.interconnectionCenter().servers();
	}

	@Override
	protected void unregister(IServerInfo server) throws Throwable {
		ServerConfig.interconnectionCenter().unregister(server.getServer());
	}
}
