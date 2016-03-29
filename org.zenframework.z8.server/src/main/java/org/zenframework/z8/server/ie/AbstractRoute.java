package org.zenframework.z8.server.ie;

import org.zenframework.z8.server.types.guid;

public abstract class AbstractRoute {

	public guid getRouteId() {
		return guid.NULL;
	}

	public int getPriority() {
		return 0;
	}

	public boolean isActive() {
		return true;
	}

	public String getTransportUrl() {
		return IeUtil.getUrl(getProtocol(), getAddress());
	}

	public abstract String getReceiver();

	public abstract String getProtocol();

	public abstract String getAddress();

}
