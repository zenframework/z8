package org.zenframework.z8.server.engine;

import java.io.Serializable;

public class ServerInfo implements Serializable {

	private static final long serialVersionUID = 5011706173964296365L;

	private final IServer server;
	private final String id;
	private final String url;

	public ServerInfo(IServer server, String id, String url) {
		this.server = server;
		this.id = id;
		this.url = url;
	}

	public IServer getServer() {
		return server;
	}

	public IApplicationServer getApplicationServer() {
		return (IApplicationServer) server;
	}

	public String getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

}
