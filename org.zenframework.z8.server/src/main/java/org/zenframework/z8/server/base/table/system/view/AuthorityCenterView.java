package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IServerInfo;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;

public class AuthorityCenterView extends HubServerView {
	static public class strings {
		static public String Title = "AuthorityCenterView.title";
	}

	static public class displayNames {
		static public String Title = Resources.get(strings.Title);
	}

	public static class CLASS<T extends AuthorityCenterView> extends HubServerView.CLASS<T> {
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

	public AuthorityCenterView(IObject container) {
		super(container);
	}
	
	@Override
	protected IServerInfo[] getServers() throws Throwable {
		return ServerConfig.authorityCenter().servers();
	}
	
	@Override
	protected void unregister(IServerInfo server) throws Throwable {
		ServerConfig.authorityCenter().unregister(server.getServer());
	}
}
